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

    private VzdEntryWrapper vzdEntryWrapper;
    private boolean editable = false;
    private boolean fillSuccess = false;
    private boolean checkSuccess = false;
    private boolean notAvailable = false;
    private List<String> fadMailAttrs = new ArrayList<>();
    private List<String> fadKomLeDataAttrs = new ArrayList<>();
    private List<String> fadKimDataAttrs = new ArrayList<>();
    private List<String> logEntries = new ArrayList<>();
    private String changedDateTime;
    private EnumStateOrProvinceName stateOrProvinceName = EnumStateOrProvinceName.UNKNOWN;
    private EnumTriValue personalEntry = EnumTriValue.UNDEFINED;
    private EnumTriValue dataFromAuthority = EnumTriValue.UNDEFINED;
    private EnumTriValue active = EnumTriValue.UNDEFINED;
    private EnumEntryType entryType = EnumEntryType.UNKNOWN;

    private DirectoryEntryContainer() {}

    public DirectoryEntryContainer(VzdEntryWrapper vzdEntryWrapper, List kuerzelL, Mandant mandant) throws Exception {
        this.vzdEntryWrapper = vzdEntryWrapper;
        this.fillSuccess = fill(vzdEntryWrapper, kuerzelL, mandant);
        this.notAvailable = false;
    }

    public DirectoryEntryContainer(boolean notAvailable, String telematikId, List kuerzelL, Mandant mandant) {
        this.notAvailable = notAvailable;
        checkSuccess = check(telematikId, kuerzelL, mandant);
    }

    private boolean check(String telematikId, List kuerzelL, Mandant mandant) {

        if (!notAvailable && vzdEntryWrapper.extractDirectoryEntryUid().equals("-1")) {
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

        if (!notAvailable && vzdEntryWrapper.checkDirectoryEntryHolder(mandant.getVzdAuthId())) {
            setEditable(false);
        }

        return true;
    }

    private boolean fill(VzdEntryWrapper vzdEntryWrapper, List kuerzelL, Mandant mandant) throws Exception {
        checkSuccess = check(vzdEntryWrapper.extractDirectoryEntryTelematikId(), kuerzelL, mandant);
        if (!checkSuccess) {
            return false;
        }

        if (vzdEntryWrapper.extractDirectoryEntryChangeDateTime() != null) {
            LocalDateTime localDateTime = DateTimeUtils.convertFrom(vzdEntryWrapper.extractDirectoryEntryChangeDateTime()).toLocalDateTime();
            setChangedDateTime(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(localDateTime));
        }
        setStateOrProvinceName(EnumStateOrProvinceName.getFromHrText(vzdEntryWrapper.extractDirectoryEntryStateOrProvinceName()));

        List<String> entryType = vzdEntryWrapper.extractDirectoryEntryEntryType();
        if (entryType != null && entryType.size() > 0)  {
            setEntryType(EnumEntryType.getFromId(entryType.get(0), log));
            if (getEntryType().equals(EnumEntryType.UNKNOWN) && entryType != null && !entryType.isEmpty()) {
                try {
                    int entryTypeId = Integer.parseInt(entryType.get(0));
                    if (entryTypeId < Integer.parseInt(EnumEntryType.Berufsgruppe.getId()) || entryTypeId > Integer.parseInt(EnumEntryType.DiGA.getId())) {
                        throw new IllegalStateException("unknwown entryType");
                    }
                }
                catch (Exception e) {
                    log.error("entryType for id: "+vzdEntryWrapper.extractDirectoryEntryTelematikId()+" is unknown: "+entryType.get(0));
                }
            }
        }
        else {
            setEntryType(EnumEntryType.UNKNOWN);
        }

        setPersonalEntry(vzdEntryWrapper.extractDirectoryEntryPersonalEntry());
        setDataFromAuthority(vzdEntryWrapper.extractDirectoryEntryDataFromAuthority());
        setActive(vzdEntryWrapper.extractDirectoryEntryActive());
        vzdEntryWrapper.extractDirectoryEntryKimMailInfos(fadMailAttrs, fadKomLeDataAttrs, fadKimDataAttrs);
        return true;
    }

    //util methods
    public String getDomainIDAsStr() {
        return vzdEntryWrapper.extractDirectoryEntryDomainIDAsStr();
    }
    public String getSpecializationAsStr() {
        return vzdEntryWrapper.extractDirectoryEntrySpecializationAsStr();
    }
    public String getProfessionOIDAsStr() {
        return vzdEntryWrapper.extractDirectoryEntryProfessionOIDAsStr();
    }
    public String getHolderAsStr() {
        return vzdEntryWrapper.extractDirectoryEntryHolderAsStr();
    }
    public String getMetaAsStr() {
        return vzdEntryWrapper.extractDirectoryEntryMetaAsStr();
    }
    public String getFadMailAttrsAsStr() {
        if (getFadMailAttrs() != null) {
            return StringUtils.listToString(getFadMailAttrs(), ",");
        }
        return "";
    }
    public String getFadKomLeDataAttrsAsStr() {
        if (getFadKomLeDataAttrs() != null) {
            return StringUtils.listToString(getFadKomLeDataAttrs(), ";");
        }
        return "";
    }
    public String getFadKimDataAttrsAsStr() {
        if (getFadKimDataAttrs() != null) {
            return StringUtils.listToString(getFadKimDataAttrs(), "|");
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
