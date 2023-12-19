/*
 *  Copyright (C) 2023 sberg it-systeme GmbH
 *
 *  Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the
 *  European Commission - subsequent versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package net.sberg.elbook.verzeichnisdienstcmpts;

import de.gematik.vzd.model.DirectoryEntry;
import de.gematik.vzd.model.FAD1;
import de.gematik.vzd.model.Fachdaten;
import lombok.Data;
import net.sberg.elbook.common.DateTimeUtils;
import net.sberg.elbook.common.StringUtils;
import net.sberg.elbook.mandantcmpts.Mandant;
import net.sberg.elbook.vzdclientcmpts.command.EnumEntryType;
import net.sberg.elbook.vzdclientcmpts.command.EnumStateOrProvinceName;
import net.sberg.elbook.vzdclientcmpts.command.EnumTriValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DirectoryEntryContainer {

    private static final Logger log = LoggerFactory.getLogger(DirectoryEntryContainer.class);

    private DirectoryEntry directoryEntry;
    private boolean editable = false;
    private boolean fillSuccess = false;
    private boolean checkSuccess = false;
    private boolean notAvailable = false;
    private List<String> kimMails = new ArrayList<>();
    private List<String> logEntries = new ArrayList<>();
    private String changedDateTime;
    private EnumStateOrProvinceName stateOrProvinceName = EnumStateOrProvinceName.UNKNOWN;
    private EnumTriValue personalEntry = EnumTriValue.UNDEFINED;
    private EnumTriValue dataFromAuthority = EnumTriValue.UNDEFINED;
    private EnumTriValue active = EnumTriValue.UNDEFINED;
    private EnumEntryType entryType = EnumEntryType.UNKNOWN;

    private DirectoryEntryContainer() {}

    public DirectoryEntryContainer(DirectoryEntry directoryEntry, List kuerzelL, Mandant mandant) throws Exception {
        this.directoryEntry = directoryEntry;
        this.fillSuccess = fill(directoryEntry, kuerzelL, mandant);
        this.notAvailable = false;
    }

    public DirectoryEntryContainer(boolean notAvailble, String telematikId, List kuerzelL, Mandant mandant) {
        this.notAvailable = notAvailble;
        checkSuccess = check(telematikId, kuerzelL, mandant);
    }

    private boolean check(String telematikId, List kuerzelL, Mandant mandant) {

        if (!notAvailable && directoryEntry.getDirectoryEntryBase().getDn().getUid().equals("-1")) {
            setEditable(true);
            return true;
        }

        if (telematikId == null) {
            return false;
        }

        boolean cont = true;
        if (kuerzelL != null) {
            cont = false;
            for (Iterator kurzelIterator = kuerzelL.iterator(); kurzelIterator.hasNext(); ) {
                String kuerzel = (String) kurzelIterator.next();
                if (telematikId.startsWith(kuerzel.trim())) {
                    cont = true;
                    break;
                }
            }
        }

        if (mandant.isFilternEintraege()) {
            if (!cont) {
                return false;
            }
        }

        if (mandant.isBearbeitenEintraege()) {
            setEditable(true);
        }
        else if (mandant.isBearbeitenNurFiltereintraege() && !cont) {
            setEditable(false);
        }
        else if (mandant.isBearbeitenNurFiltereintraege() && cont) {
            setEditable(true);
        }
        else if (!mandant.isBearbeitenNurFiltereintraege()) {
            setEditable(false);
        }

        if (!notAvailable && !directoryEntry.getDirectoryEntryBase().getHolder().isEmpty() && !directoryEntry.getDirectoryEntryBase().getHolder().contains(mandant.getVzdAuthId())) {
            setEditable(false);
        }

        return true;
    }

    private boolean fill(DirectoryEntry entry, List kuerzelL, Mandant mandant) throws Exception {
        checkSuccess = check(entry.getDirectoryEntryBase().getTelematikID(), kuerzelL, mandant);
        if (!checkSuccess) {
            return false;
        }

        if (entry.getDirectoryEntryBase().getChangeDateTime() != null) {
            LocalDateTime localDateTime = DateTimeUtils.convertFrom(entry.getDirectoryEntryBase().getChangeDateTime()).toLocalDateTime();
            setChangedDateTime(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(localDateTime));
        }
        setStateOrProvinceName(EnumStateOrProvinceName.getFromHrText(entry.getDirectoryEntryBase().getStateOrProvinceName()));

        if (entry.getDirectoryEntryBase().getEntryType() != null && entry.getDirectoryEntryBase().getEntryType().size() > 0)  {
            setEntryType(EnumEntryType.getFromId(entry.getDirectoryEntryBase().getEntryType().get(0), log));
            if (getEntryType().equals(EnumEntryType.UNKNOWN) && entry.getDirectoryEntryBase().getEntryType() != null && !entry.getDirectoryEntryBase().getEntryType().isEmpty()) {
                try {
                    int entryTypeId = Integer.parseInt(entry.getDirectoryEntryBase().getEntryType().get(0));
                    if (entryTypeId < 1 || entryTypeId > 6) {
                        throw new IllegalStateException("unknwown entryType");
                    }
                }
                catch (Exception e) {
                    log.error("entryType for id: "+entry.getDirectoryEntryBase().getTelematikID()+" is unknown: "+entry.getDirectoryEntryBase().getEntryType().get(0));
                }
            }
        }
        else {
            setEntryType(EnumEntryType.UNKNOWN);
        }

        setPersonalEntry(EnumTriValue.getFromBool(entry.getDirectoryEntryBase().getPersonalEntry()));
        setDataFromAuthority(EnumTriValue.getFromBool(entry.getDirectoryEntryBase().getDataFromAuthority()));
        setActive(EnumTriValue.getFromBool(entry.getDirectoryEntryBase().getActive()));

        //mails auslesen
        if (entry.getFachdaten() != null) {
            for (Iterator<Fachdaten> iterator = entry.getFachdaten().iterator(); iterator.hasNext(); ) {
                Fachdaten fachdaten = iterator.next();
                if (fachdaten.getFAD1() == null) {
                    continue;
                }
                for (Iterator<FAD1> fad1Iterator = fachdaten.getFAD1().iterator(); fad1Iterator.hasNext(); ) {
                    FAD1 fad1 = fad1Iterator.next();
                    if (fad1.getMail() == null) {
                        continue;
                    }
                    for (Iterator<String> mailIterator = fad1.getMail().iterator(); mailIterator.hasNext(); ) {
                        String mail = mailIterator.next();
                        getKimMails().add(mail);
                    }
                }
            }
        }

        return true;
    }

    //util methods
    public String getDomainIDAsStr() {
        if (directoryEntry != null && directoryEntry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryEntry.getDirectoryEntryBase().getDomainID());
        }
        return "";
    }
    public String getSpecializationAsStr() {
        if (directoryEntry != null && directoryEntry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryEntry.getDirectoryEntryBase().getSpecialization());
        }
        return "";
    }
    public String getProfessionOIDAsStr() {
        if (directoryEntry != null && directoryEntry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryEntry.getDirectoryEntryBase().getProfessionOID());
        }
        return "";
    }
    public String getHolderAsStr() {
        if (directoryEntry != null && directoryEntry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryEntry.getDirectoryEntryBase().getHolder());
        }
        return "";
    }
    public String getMetaAsStr() {
        if (directoryEntry != null && directoryEntry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryEntry.getDirectoryEntryBase().getMeta());
        }
        return "";
    }
    public String getKimMailsAsStr() {
        if (getKimMails() != null) {
            return StringUtils.listToString(getKimMails());
        }
        return "";
    }
    public String getLogEntriesAsStr() {
        if (getLogEntries() != null) {
            return logEntries.stream().collect(Collectors.joining("\n"));
        }
        return "";
    }
}
