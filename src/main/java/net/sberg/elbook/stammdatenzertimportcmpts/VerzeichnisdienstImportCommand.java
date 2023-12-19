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
package net.sberg.elbook.stammdatenzertimportcmpts;

import de.gematik.vzd.model.BaseDirectoryEntry;
import de.gematik.vzd.model.DirectoryEntry;
import lombok.Data;
import net.sberg.elbook.mandantcmpts.EnumSektor;
import net.sberg.elbook.verzeichnisdienstcmpts.DirectoryEntrySaveContainer;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class VerzeichnisdienstImportCommand {

    // id attributes
    private String verwaltungsId;
    private String vzdUid;
    private String telematikID;
    private List<String> sektorIds;

    // name attributes
    private String vorname;
    private String nachname;
    private String allgemeinerName;
    private String andererName;
    private String anzeigeName;
    private String titel;
    private String organisation;

    // address attributes
    private String strasseUndHausnummer;
    private String plz;
    private String ort;
    private EnumStateOrProvinceName bundesland;
    private String laenderCode;

    // profession attributes
    private List<String> fachrichtungen;
    private EnumEntryType eintragsTyp;

    // system attributes
    private String maxKomLeAdr;
    private EnumTriValue aktiv = EnumTriValue.YES;

    private List<EncZertifikat> encZertifikat = new ArrayList<>();

    private boolean toDelete;

    public static VerzeichnisdienstImportCommand merge(VerzeichnisdienstImportCommand src, VerzeichnisdienstImportCommand dest) {
        VerzeichnisdienstImportCommand res = new VerzeichnisdienstImportCommand();

        res.setVerwaltungsId(src.getVerwaltungsId());
        res.setVzdUid(src.getVzdUid());
        res.setTelematikID(src.getTelematikID());
        res.setSektorIds(src.getSektorIds());

        res.setStrasseUndHausnummer(src.getStrasseUndHausnummer());
        res.setPlz(src.getPlz());
        res.setOrt(src.getOrt());
        res.setLaenderCode(src.getLaenderCode());
        res.setBundesland(src.getBundesland());

        res.setVorname(src.getVorname());
        res.setNachname(src.getNachname());
        res.setAnzeigeName(src.getAnzeigeName());
        res.setAllgemeinerName(src.getAllgemeinerName());
        res.setAndererName(src.getAndererName());
        res.setTitel(src.getTitel());
        res.setOrganisation(src.getOrganisation());

        res.setFachrichtungen(src.getFachrichtungen());
        res.setEintragsTyp(src.getEintragsTyp());

        res.setMaxKomLeAdr(src.getMaxKomLeAdr());
        res.setAktiv(src.getAktiv());

        res.getEncZertifikat().addAll(src.getEncZertifikat());
        res.getEncZertifikat().addAll(dest.getEncZertifikat());

        res.setToDelete(src.isToDelete());
        return res;
    }

    public DirectoryEntrySaveContainer createAddDirEntryCommand(TiVZDProperties tiVZDProperties, EnumSektor sektor) {
        DirectoryEntrySaveContainer directoryEntrySaveContainer = new DirectoryEntrySaveContainer();
        directoryEntrySaveContainer.setCreate(true);

        AddDirEntryCommand addDirEntryCommand = new AddDirEntryCommand();
        directoryEntrySaveContainer.setAddDirEntryCommand(addDirEntryCommand);

        addDirEntryCommand.setUid(vzdUid);
        addDirEntryCommand.setTelematikId(telematikID);
        addDirEntryCommand.setDomainId(sektorIds);

        addDirEntryCommand.setGivenName(vorname);
        addDirEntryCommand.setSn(nachname);
        addDirEntryCommand.setOtherName(andererName);
        addDirEntryCommand.setDisplayName(anzeigeName);
        addDirEntryCommand.setCn(allgemeinerName);
        addDirEntryCommand.setTitle(titel);
        addDirEntryCommand.setOrganization(organisation);

        addDirEntryCommand.setStreetAddress(strasseUndHausnummer);
        addDirEntryCommand.setPostalCode(plz);
        addDirEntryCommand.setLocalityName(ort);
        addDirEntryCommand.setStateOrProvinceName(bundesland);
        addDirEntryCommand.setCountryCode(laenderCode);

        addDirEntryCommand.setSpecialization(fachrichtungen);
        addDirEntryCommand.setEntryType(eintragsTyp != null?eintragsTyp:sektor.getEntryType());

        addDirEntryCommand.setMaxKomLeAdr(maxKomLeAdr);
        addDirEntryCommand.setActive(aktiv);

        if (!sektor.getEntryType().equals(EnumEntryType.Berufsgruppe) && !sektor.getEntryType().equals(EnumEntryType.Versicherte)) {
            if (nachname == null) {
                addDirEntryCommand.setSn(anzeigeName);
            }
        }

        addDirEntryCommand.setHolder(new ArrayList<>());
        addDirEntryCommand.getHolder().add(tiVZDProperties.getAuthId());

        return directoryEntrySaveContainer;
    }

    public DirectoryEntrySaveContainer createModDirEntryCommand(TiVZDProperties tiVZDProperties, EnumSektor sektor, DirectoryEntry directoryEntry, Logger log) {
        DirectoryEntrySaveContainer directoryEntrySaveContainer = new DirectoryEntrySaveContainer();
        directoryEntrySaveContainer.setCreate(false);

        ModDirEntryCommand modDirEntryCommand = new ModDirEntryCommand();
        directoryEntrySaveContainer.setModDirEntryCommand(modDirEntryCommand);

        modDirEntryCommand.setUid(vzdUid);
        modDirEntryCommand.setTelematikId(telematikID);
        modDirEntryCommand.setDomainId(sektorIds != null?sektorIds:directoryEntry.getDirectoryEntryBase().getDomainID());

        modDirEntryCommand.setSn(nachname != null?nachname:directoryEntry.getDirectoryEntryBase().getSn());
        modDirEntryCommand.setGivenName(vorname != null?vorname:directoryEntry.getDirectoryEntryBase().getGivenName());
        modDirEntryCommand.setOtherName(andererName != null?andererName:directoryEntry.getDirectoryEntryBase().getOtherName());
        modDirEntryCommand.setDisplayName(anzeigeName != null?anzeigeName:directoryEntry.getDirectoryEntryBase().getDisplayName());
        modDirEntryCommand.setCn(allgemeinerName != null?allgemeinerName:directoryEntry.getDirectoryEntryBase().getCn());
        modDirEntryCommand.setTitle(titel != null?titel:directoryEntry.getDirectoryEntryBase().getTitle());
        modDirEntryCommand.setOrganization(organisation != null?organisation:directoryEntry.getDirectoryEntryBase().getOrganization());

        modDirEntryCommand.setStreetAddress(strasseUndHausnummer != null?strasseUndHausnummer:directoryEntry.getDirectoryEntryBase().getStreetAddress());
        modDirEntryCommand.setPostalCode(plz != null?plz:directoryEntry.getDirectoryEntryBase().getPostalCode());
        modDirEntryCommand.setLocalityName(ort != null?ort:directoryEntry.getDirectoryEntryBase().getLocalityName());
        modDirEntryCommand.setStateOrProvinceName(bundesland != null?bundesland:EnumStateOrProvinceName.getFromHrText(directoryEntry.getDirectoryEntryBase().getStateOrProvinceName()));
        modDirEntryCommand.setCountryCode(laenderCode != null?laenderCode:directoryEntry.getDirectoryEntryBase().getCountryCode());

        modDirEntryCommand.setSpecialization(fachrichtungen != null?fachrichtungen:directoryEntry.getDirectoryEntryBase().getSpecialization());

        if (eintragsTyp != null) {
            modDirEntryCommand.setEntryType(eintragsTyp);
        }
        else {
            modDirEntryCommand.setEntryType(sektor.getEntryType());
        }

        modDirEntryCommand.setMeta(directoryEntry.getDirectoryEntryBase().getMeta());
        modDirEntryCommand.setActive(aktiv != null?aktiv:EnumTriValue.getFromBool(directoryEntry.getDirectoryEntryBase().getActive()));
        modDirEntryCommand.setMaxKomLeAdr(maxKomLeAdr != null?maxKomLeAdr:directoryEntry.getDirectoryEntryBase().getMaxKOMLEadr());

        if (directoryEntry.getDirectoryEntryBase().getHolder().isEmpty()) {
            modDirEntryCommand.setHolder(new ArrayList<>());
            modDirEntryCommand.getHolder().add(tiVZDProperties.getAuthId());
        }
        else {
            modDirEntryCommand.setHolder(directoryEntry.getDirectoryEntryBase().getHolder());
        }

        return directoryEntrySaveContainer;
    }

    public boolean toUpdate(EnumSektor sektor, DirectoryEntry directoryEntry) throws Exception {
        if (directoryEntry == null) {
            return false;
        }

        if (!sektor.getEntryType().equals(EnumEntryType.Berufsgruppe) && !sektor.getEntryType().equals(EnumEntryType.Versicherte)) {
            if (nachname == null) {
                setNachname(anzeigeName);
            }
            if (allgemeinerName == null) {
                setAllgemeinerName(anzeigeName);
            }
        }

        if (sektor.getEntryType().equals(EnumEntryType.Leistungserbringerinstitution)
            ||
            sektor.getEntryType().equals(EnumEntryType.Krankenkasse)
            ||
            sektor.getEntryType().equals(EnumEntryType.Krankenkasse_ePA)
            ||
            sektor.getEntryType().equals(EnumEntryType.Organisation)
        ) {
            if (directoryEntry.getDirectoryEntryBase().getGivenName() != null
                &&
                !directoryEntry.getDirectoryEntryBase().getGivenName().trim().isEmpty()
            ) {
                setVorname("");
            }
            else {
                setVorname(null);
            }
        }

        //special komfort handling -> lower versions
        if (getEintragsTyp() == null) {
            setEintragsTyp(sektor.getEntryType());
        }
        if (getLaenderCode() == null && directoryEntry.getDirectoryEntryBase().getCountryCode() != null) {
            setLaenderCode(directoryEntry.getDirectoryEntryBase().getCountryCode());
        }
        if (getMaxKomLeAdr() == null && directoryEntry.getDirectoryEntryBase().getMaxKOMLEadr() != null) {
            setMaxKomLeAdr(directoryEntry.getDirectoryEntryBase().getMaxKOMLEadr());
        }

        boolean toUpdate = createHash() != createHash(directoryEntry);

        if (directoryEntry.getDirectoryEntryBase().getHolder().isEmpty()) {
            toUpdate = true;
        }

        return toUpdate;
    }

    public boolean toDelete(DirectoryEntry directoryEntry) throws Exception {
        if (directoryEntry == null) {
            return toDelete;
        }
        return toDelete;
    }

    private int createHash() {
        int compareDataCode = 0;

        //ids
        if (telematikID != null) {
            compareDataCode = compareDataCode + (telematikID.trim().replaceAll(" ", "").hashCode() * 3);
        }
        if (sektorIds != null) {
            String fachrichtungenStr = sektorIds.stream().map( n -> n.toString() ).collect( Collectors.joining( "," ) );
            compareDataCode = compareDataCode + (fachrichtungenStr.trim().replaceAll(" ", "").hashCode() * 5);
        }

        //name
        if (vorname != null) {
            compareDataCode = compareDataCode + (vorname.trim().replaceAll(" ", "").hashCode() * 7);
        }
        if (nachname != null) {
            compareDataCode = compareDataCode + (nachname.trim().replaceAll(" ", "").hashCode() * 11);
        }
        if (anzeigeName != null) {
            compareDataCode = compareDataCode + (anzeigeName.trim().replaceAll(" ", "").hashCode() * 13);
        }
        if (titel != null) {
            compareDataCode = compareDataCode + (titel.trim().replaceAll(" ", "").hashCode() * 17);
        }
        if (andererName != null) {
            compareDataCode = compareDataCode + (andererName.trim().replaceAll(" ", "").hashCode() * 19);
        }
        if (allgemeinerName != null) {
            compareDataCode = compareDataCode + (allgemeinerName.trim().replaceAll(" ", "").hashCode() * 23);
        }
        if (organisation != null) {
            compareDataCode = compareDataCode + (organisation.trim().replaceAll(" ", "").hashCode() * 29);
        }

        //address
        if (strasseUndHausnummer != null) {
            compareDataCode = compareDataCode + (strasseUndHausnummer.trim().replaceAll(" ", "").hashCode() * 31);
        }
        if (plz != null) {
            compareDataCode = compareDataCode + (plz.trim().replaceAll(" ", "").hashCode() * 37);
        }
        if (ort != null) {
            compareDataCode = compareDataCode + (ort.trim().replaceAll(" ", "").hashCode() * 41);
        }
        if (bundesland != null) {
            compareDataCode = compareDataCode + (bundesland.getHrText().trim().replaceAll(" ", "").hashCode() * 43);
        }
        if (laenderCode != null) {
            compareDataCode = compareDataCode + (laenderCode.trim().replaceAll(" ", "").hashCode() * 47);
        }

        //profession
        if (fachrichtungen != null) {
            String fachrichtungenStr = fachrichtungen.stream().map( n -> n.toString() ).collect( Collectors.joining( "," ) );
            compareDataCode = compareDataCode + (fachrichtungenStr.trim().replaceAll(" ", "").hashCode() * 53);
        }
        if (eintragsTyp != null) {
            compareDataCode = compareDataCode + (eintragsTyp.getId().trim().replaceAll(" ", "").hashCode() * 59);
        }

        //system
        if (maxKomLeAdr != null) {
            compareDataCode = compareDataCode + (maxKomLeAdr.trim().replaceAll(" ", "").hashCode() * 61);
        }
        //active
        if (aktiv != null && aktiv.getDataValue() != null) {
            compareDataCode = compareDataCode + (aktiv.getDataValue().hashCode() * 67);
        }

        return compareDataCode;
    }

    private int createHash(DirectoryEntry directoryEntry) {
        BaseDirectoryEntry baseDirectoryEntry = directoryEntry.getDirectoryEntryBase();
        int compareDataCode = 0;

        //ids
        //telematikID
        if (baseDirectoryEntry.getTelematikID() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getTelematikID().trim().replaceAll(" ", "").hashCode() * 3);
        }
        //sektorIds
        if (baseDirectoryEntry.getDomainID() != null) {
            String fachrichtungenStr = baseDirectoryEntry.getDomainID().stream().map( n -> n.toString() ).collect( Collectors.joining( "," ) );
            compareDataCode = compareDataCode + (fachrichtungenStr.trim().replaceAll(" ", "").hashCode() * 5);
        }

        //name
        //vorname
        if (baseDirectoryEntry.getGivenName() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getGivenName().trim().replaceAll(" ", "").hashCode() * 7);
        }
        //nachname
        if (baseDirectoryEntry.getSn() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getSn().trim().replaceAll(" ", "").hashCode() * 11);
        }
        //anzeigeName
        if (baseDirectoryEntry.getDisplayName() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getDisplayName().trim().replaceAll(" ", "").hashCode() * 13);
        }
        //titel
        if (baseDirectoryEntry.getTitle() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getTitle().trim().replaceAll(" ", "").hashCode() * 17);
        }
        //andererName
        if (baseDirectoryEntry.getOtherName() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getOtherName().trim().replaceAll(" ", "").hashCode() * 19);
        }
        //allgemeinerName
        if (baseDirectoryEntry.getCn() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getCn().trim().replaceAll(" ", "").hashCode() * 23);
        }
        //organisation
        if (baseDirectoryEntry.getOrganization() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getOrganization().trim().replaceAll(" ", "").hashCode() * 29);
        }

        //address
        //strasseUndHausnummer
        if (baseDirectoryEntry.getStreetAddress() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getStreetAddress().trim().replaceAll(" ", "").hashCode() * 31);
        }
        //plz
        if (baseDirectoryEntry.getPostalCode() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getPostalCode().trim().replaceAll(" ", "").hashCode() * 37);
        }
        //ort
        if (baseDirectoryEntry.getLocalityName() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getLocalityName().trim().replaceAll(" ", "").hashCode() * 41);
        }
        //bundesland
        if (baseDirectoryEntry.getStateOrProvinceName() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getStateOrProvinceName().trim().replaceAll(" ", "").hashCode() * 43);
        }
        //laenderCode
        if (baseDirectoryEntry.getCountryCode() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getCountryCode().trim().replaceAll(" ", "").hashCode() * 47);
        }

        //profession
        //fachrichtungen
        if (baseDirectoryEntry.getSpecialization() != null) {
            String fachrichtungenStr = baseDirectoryEntry.getSpecialization().stream().map( n -> n.toString() ).collect( Collectors.joining( "," ) );
            compareDataCode = compareDataCode + (fachrichtungenStr.trim().replaceAll(" ", "").hashCode() * 53);
        }
        //eintragsTyp
        if (baseDirectoryEntry.getEntryType() != null && !baseDirectoryEntry.getEntryType().isEmpty()) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getEntryType().get(0).trim().replaceAll(" ", "").hashCode() * 59);
        }

        //system
        //maxKomLeAdr
        if (baseDirectoryEntry.getMaxKOMLEadr() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getMaxKOMLEadr().trim().replaceAll(" ", "").hashCode() * 61);
        }
        //active
        if (baseDirectoryEntry.getActive() != null) {
            compareDataCode = compareDataCode + (baseDirectoryEntry.getActive().hashCode() * 67);
        }

        return compareDataCode;
    }
}
