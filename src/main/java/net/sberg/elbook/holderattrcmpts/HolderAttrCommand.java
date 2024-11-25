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
package net.sberg.elbook.holderattrcmpts;

import lombok.Data;
import net.sberg.elbook.mandantcmpts.EnumSektor;
import net.sberg.elbook.stammdatenzertimportcmpts.EncZertifikat;
import net.sberg.elbook.verzeichnisdienstcmpts.DirectoryEntrySaveContainer;
import net.sberg.elbook.verzeichnisdienstcmpts.VzdEntryWrapper;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class HolderAttrCommand {

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
    private boolean toIgnore;

    public static HolderAttrCommand merge(HolderAttrCommand src, HolderAttrCommand dest) {
        HolderAttrCommand res = new HolderAttrCommand();

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

    public DirectoryEntrySaveContainer createModDirEntryCommand(TiVZDProperties tiVZDProperties, EnumSektor sektor, VzdEntryWrapper directoryEntry, Logger log) {
        DirectoryEntrySaveContainer directoryEntrySaveContainer = new DirectoryEntrySaveContainer();
        directoryEntrySaveContainer.setCreate(false);

        ModDirEntryCommand modDirEntryCommand = new ModDirEntryCommand();
        directoryEntrySaveContainer.setModDirEntryCommand(modDirEntryCommand);

        modDirEntryCommand.setUid(vzdUid);
        modDirEntryCommand.setTelematikId(telematikID);
        modDirEntryCommand.setDomainId(sektorIds != null?sektorIds:directoryEntry.extractDirectoryEntryDomainID());

        modDirEntryCommand.setSn(nachname != null?nachname:directoryEntry.extractDirectoryEntrySn());
        modDirEntryCommand.setGivenName(vorname != null?vorname:directoryEntry.extractDirectoryEntryGivenName());
        modDirEntryCommand.setOtherName(andererName != null?andererName:directoryEntry.extractDirectoryEntryOtherName());
        modDirEntryCommand.setDisplayName(anzeigeName != null?anzeigeName:directoryEntry.extractDirectoryEntryDisplayName());
        modDirEntryCommand.setCn(allgemeinerName != null?allgemeinerName:directoryEntry.extractDirectoryEntryCn());
        modDirEntryCommand.setTitle(titel != null?titel:directoryEntry.extractDirectoryEntryTitle());
        modDirEntryCommand.setOrganization(organisation != null?organisation:directoryEntry.extractDirectoryEntryOrganization());

        modDirEntryCommand.setStreetAddress(strasseUndHausnummer != null?strasseUndHausnummer:directoryEntry.extractDirectoryEntryStreetAddress());
        modDirEntryCommand.setPostalCode(plz != null?plz:directoryEntry.extractDirectoryEntryPostalCode());
        modDirEntryCommand.setLocalityName(ort != null?ort:directoryEntry.extractDirectoryEntryLocalityName());
        modDirEntryCommand.setStateOrProvinceName(bundesland != null?bundesland:EnumStateOrProvinceName.getFromHrText(directoryEntry.extractDirectoryEntryStateOrProvinceName()));
        modDirEntryCommand.setCountryCode(laenderCode != null?laenderCode:directoryEntry.extractDirectoryEntryCountryCode());

        modDirEntryCommand.setSpecialization(fachrichtungen != null?fachrichtungen:directoryEntry.extractDirectoryEntrySpecialization());

        if (eintragsTyp != null) {
            modDirEntryCommand.setEntryType(eintragsTyp);
        }
        else {
            modDirEntryCommand.setEntryType(sektor.getEntryType());
        }

        modDirEntryCommand.setMeta(directoryEntry.extractDirectoryEntryMeta());
        modDirEntryCommand.setActive(aktiv != null?aktiv:directoryEntry.extractDirectoryEntryActive());
        modDirEntryCommand.setMaxKomLeAdr(maxKomLeAdr != null?maxKomLeAdr:directoryEntry.extractDirectoryEntryMaxKOMLEadr());

        if (directoryEntry.extractDirectoryEntryHolder().isEmpty()) {
            modDirEntryCommand.setHolder(new ArrayList<>());
            modDirEntryCommand.getHolder().add(tiVZDProperties.getAuthId());
        }
        else {
            modDirEntryCommand.setHolder(directoryEntry.extractDirectoryEntryHolder());
        }

        return directoryEntrySaveContainer;
    }

    public boolean toUpdate(EnumSektor sektor, VzdEntryWrapper directoryEntry) throws Exception {
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
            if (directoryEntry.extractDirectoryEntryGivenName() != null
                &&
                !directoryEntry.extractDirectoryEntryGivenName().trim().isEmpty()
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
        if (getLaenderCode() == null && directoryEntry.extractDirectoryEntryCountryCode() != null) {
            setLaenderCode(directoryEntry.extractDirectoryEntryCountryCode());
        }
        if (getMaxKomLeAdr() == null && directoryEntry.extractDirectoryEntryMaxKOMLEadr() != null) {
            setMaxKomLeAdr(directoryEntry.extractDirectoryEntryMaxKOMLEadr());
        }

        boolean toUpdate = createHash() != createHash(directoryEntry);

        if (directoryEntry.extractDirectoryEntryHolder().isEmpty()) {
            toUpdate = true;
        }

        return toUpdate;
    }

    public boolean toDelete(VzdEntryWrapper directoryEntry) throws Exception {
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

    private int createHash(VzdEntryWrapper directoryEntry) {
        int compareDataCode = 0;

        //ids
        //telematikID
        if (directoryEntry.extractDirectoryEntryTelematikId() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryTelematikId().trim().replaceAll(" ", "").hashCode() * 3);
        }
        //sektorIds
        if (directoryEntry.extractDirectoryEntryDomainID() != null) {
            String fachrichtungenStr = directoryEntry.extractDirectoryEntryDomainID().stream().map( n -> n.toString() ).collect( Collectors.joining( "," ) );
            compareDataCode = compareDataCode + (fachrichtungenStr.trim().replaceAll(" ", "").hashCode() * 5);
        }

        //name
        //vorname
        if (directoryEntry.extractDirectoryEntryGivenName() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryGivenName().trim().replaceAll(" ", "").hashCode() * 7);
        }
        //nachname
        if (directoryEntry.extractDirectoryEntrySn() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntrySn().trim().replaceAll(" ", "").hashCode() * 11);
        }
        //anzeigeName
        if (directoryEntry.extractDirectoryEntryDisplayName() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryDisplayName().trim().replaceAll(" ", "").hashCode() * 13);
        }
        //titel
        if (directoryEntry.extractDirectoryEntryTitle() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryTitle().trim().replaceAll(" ", "").hashCode() * 17);
        }
        //andererName
        if (directoryEntry.extractDirectoryEntryOtherName() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryOtherName().trim().replaceAll(" ", "").hashCode() * 19);
        }
        //allgemeinerName
        if (directoryEntry.extractDirectoryEntryCn() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryCn().trim().replaceAll(" ", "").hashCode() * 23);
        }
        //organisation
        if (directoryEntry.extractDirectoryEntryOrganization() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryOrganization().trim().replaceAll(" ", "").hashCode() * 29);
        }

        //address
        //strasseUndHausnummer
        if (directoryEntry.extractDirectoryEntryStreetAddress() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryStreetAddress().trim().replaceAll(" ", "").hashCode() * 31);
        }
        //plz
        if (directoryEntry.extractDirectoryEntryPostalCode() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryPostalCode().trim().replaceAll(" ", "").hashCode() * 37);
        }
        //ort
        if (directoryEntry.extractDirectoryEntryLocalityName() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryLocalityName().trim().replaceAll(" ", "").hashCode() * 41);
        }
        //bundesland
        if (directoryEntry.extractDirectoryEntryStateOrProvinceName() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryStateOrProvinceName().trim().replaceAll(" ", "").hashCode() * 43);
        }
        //laenderCode
        if (directoryEntry.extractDirectoryEntryCountryCode() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryCountryCode().trim().replaceAll(" ", "").hashCode() * 47);
        }

        //profession
        //fachrichtungen
        if (directoryEntry.extractDirectoryEntrySpecialization() != null) {
            String fachrichtungenStr = directoryEntry.extractDirectoryEntrySpecialization().stream().map( n -> n.toString() ).collect( Collectors.joining( "," ) );
            compareDataCode = compareDataCode + (fachrichtungenStr.trim().replaceAll(" ", "").hashCode() * 53);
        }
        //eintragsTyp
        if (directoryEntry.extractDirectoryEntryEntryType() != null && !directoryEntry.extractDirectoryEntryEntryType().isEmpty()) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryEntryType().get(0).trim().replaceAll(" ", "").hashCode() * 59);
        }

        //system
        //maxKomLeAdr
        if (directoryEntry.extractDirectoryEntryMaxKOMLEadr() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryMaxKOMLEadr().trim().replaceAll(" ", "").hashCode() * 61);
        }
        //active
        if (directoryEntry.extractDirectoryEntryActive() != null && directoryEntry.extractDirectoryEntryActive().getDataValue() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryActive().getDataValue().hashCode() * 67);
        }

        return compareDataCode;
    }
}
