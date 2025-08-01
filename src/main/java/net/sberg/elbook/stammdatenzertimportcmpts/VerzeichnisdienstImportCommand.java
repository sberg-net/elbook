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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.gematik.ws.cm.pers.hba_smc_b.v1.SmcbAntragExport;
import lombok.Data;
import net.sberg.elbook.glossarcmpts.TelematikIdInfo;
import net.sberg.elbook.mandantcmpts.Mandant;
import net.sberg.elbook.verzeichnisdienstcmpts.DirectoryEntrySaveContainer;
import net.sberg.elbook.verzeichnisdienstcmpts.VzdEntryWrapper;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.*;

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
    private List<String> lanrs;
    private String telematikIDMutterOrgEinheit;

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
    private List<String> besitzer = new ArrayList<>();

    private List<EncZertifikat> encZertifikat = new ArrayList<>();

    private boolean toDelete;
    private boolean toIgnore;

    @JsonIgnore
    private TelematikIdInfo telematikIdInfo;
    @JsonIgnore
    private List<SmcbAntragExport> helperSmcbAntragExports = new ArrayList<>();
    @JsonIgnore
    private List<String> hbaAntragCertFiles = new ArrayList<>();
    @JsonIgnore
    private List<String> helperTelematikIDs = new ArrayList<>();

    public static VerzeichnisdienstImportCommand merge(VerzeichnisdienstImportCommand src, VerzeichnisdienstImportCommand dest) {
        VerzeichnisdienstImportCommand res = new VerzeichnisdienstImportCommand();

        res.setVerwaltungsId(src.getVerwaltungsId());
        res.setVzdUid(src.getVzdUid());
        res.setTelematikID(src.getTelematikID());
        res.setSektorIds(src.getSektorIds());
        res.setLanrs(src.getLanrs());
        res.setTelematikIDMutterOrgEinheit(src.getTelematikIDMutterOrgEinheit());

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
        res.setBesitzer(src.getBesitzer());

        res.getEncZertifikat().addAll(src.getEncZertifikat());
        res.getEncZertifikat().addAll(dest.getEncZertifikat());

        res.setToDelete(src.isToDelete());
        res.setToIgnore(src.isToIgnore());

        res.setTelematikIdInfo(src.getTelematikIdInfo());
        return res;
    }

    public static VerzeichnisdienstImportCommand copy(VerzeichnisdienstImportCommand src) {
        VerzeichnisdienstImportCommand res = new VerzeichnisdienstImportCommand();

        res.setVerwaltungsId(src.getVerwaltungsId());
        res.setVzdUid(src.getVzdUid());
        res.setTelematikID(src.getTelematikID());
        res.setSektorIds(src.getSektorIds());
        res.setLanrs(src.getLanrs());
        res.setTelematikIDMutterOrgEinheit(src.getTelematikIDMutterOrgEinheit());

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
        res.setBesitzer(src.getBesitzer());

        res.getEncZertifikat().addAll(src.getEncZertifikat());

        res.setToDelete(src.isToDelete());
        res.setToIgnore(src.isToIgnore());

        res.setTelematikIdInfo(src.getTelematikIdInfo());
        return res;
    }

    public DirectoryEntrySaveContainer createAddDirEntryCommand(Mandant mandant, TiVZDProperties tiVZDProperties) {
        EnumEntryType entryType = telematikIdInfo.getProfessionOIDInfos().get(0).getEntryType();

        DirectoryEntrySaveContainer directoryEntrySaveContainer = new DirectoryEntrySaveContainer();
        directoryEntrySaveContainer.setCreate(true);

        AddDirEntryCommand addDirEntryCommand = new AddDirEntryCommand();
        directoryEntrySaveContainer.setAddDirEntryCommand(addDirEntryCommand);

        addDirEntryCommand.setUid(vzdUid);
        addDirEntryCommand.setTelematikId(telematikID);
        addDirEntryCommand.setDomainId(sektorIds);
        addDirEntryCommand.setLanr(lanrs);
        addDirEntryCommand.setProvidedBy(telematikIDMutterOrgEinheit);

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

        if (telematikIdInfo.getProfessionOIDInfos().get(0).isOrganization()) {
            addDirEntryCommand.setStateOrProvinceName(mandant.getBundesland());
        }
        else {
            addDirEntryCommand.setStateOrProvinceName(bundesland);
        }

        if (telematikIdInfo.getProfessionOIDInfos().get(0).isOrganization()) {
            if (laenderCode == null) {
                addDirEntryCommand.setCountryCode("DE");
            }
            else {
                addDirEntryCommand.setCountryCode(laenderCode);
            }
        }
        else {
            addDirEntryCommand.setCountryCode(laenderCode);
        }

        addDirEntryCommand.setSpecialization(fachrichtungen);
        addDirEntryCommand.setEntryType(eintragsTyp != null?eintragsTyp:entryType);

        addDirEntryCommand.setMaxKomLeAdr(maxKomLeAdr);
        addDirEntryCommand.setActive(aktiv);

        if (telematikIdInfo.getProfessionOIDInfos().get(0).isOrganization()) {
            if (nachname == null) {
                addDirEntryCommand.setSn(anzeigeName);
            }
        }

        addDirEntryCommand.setHolder(new ArrayList<>());
        if (besitzer.isEmpty() || !besitzer.contains(tiVZDProperties.getAuthId())) {
            besitzer.add(tiVZDProperties.getAuthId());
        }
        addDirEntryCommand.getHolder().addAll(besitzer);

        return directoryEntrySaveContainer;
    }

    public DirectoryEntrySaveContainer createModDirEntryCommand(VzdEntryWrapper directoryEntry) {
        EnumEntryType entryType = telematikIdInfo.getProfessionOIDInfos().get(0).getEntryType();

        DirectoryEntrySaveContainer directoryEntrySaveContainer = new DirectoryEntrySaveContainer();
        directoryEntrySaveContainer.setCreate(false);

        ModDirEntryCommand modDirEntryCommand = new ModDirEntryCommand();
        directoryEntrySaveContainer.setModDirEntryCommand(modDirEntryCommand);

        modDirEntryCommand.setUid(vzdUid);
        modDirEntryCommand.setTelematikId(telematikID);
        modDirEntryCommand.setDomainId(sektorIds != null?sektorIds:directoryEntry.extractDirectoryEntryDomainID());
        modDirEntryCommand.setLanr(lanrs != null?lanrs:directoryEntry.extractDirectoryEntryLanr());
        modDirEntryCommand.setProvidedBy(telematikIDMutterOrgEinheit != null?telematikIDMutterOrgEinheit:directoryEntry.extractDirectoryEntryProvidedBy());

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
        modDirEntryCommand.setStateOrProvinceName(bundesland);
        modDirEntryCommand.setCountryCode(laenderCode != null?laenderCode:directoryEntry.extractDirectoryEntryCountryCode());

        modDirEntryCommand.setSpecialization(fachrichtungen != null?fachrichtungen:directoryEntry.extractDirectoryEntrySpecialization());

        if (eintragsTyp != null) {
            modDirEntryCommand.setEntryType(eintragsTyp);
        }
        else {
            modDirEntryCommand.setEntryType(entryType);
        }

        modDirEntryCommand.setMeta(directoryEntry.extractDirectoryEntryMeta());
        modDirEntryCommand.setActive(aktiv != null?aktiv:directoryEntry.extractDirectoryEntryActive());
        modDirEntryCommand.setMaxKomLeAdr(maxKomLeAdr != null?maxKomLeAdr:directoryEntry.extractDirectoryEntryMaxKOMLEadr());

        modDirEntryCommand.setHolder(new ArrayList<>());
        modDirEntryCommand.getHolder().addAll(besitzer);

        return directoryEntrySaveContainer;
    }

    public boolean toUpdate(VzdEntryWrapper directoryEntry, Mandant mandant, TiVZDProperties tiVZDProperties) throws Exception {
        if (directoryEntry == null) {
            return false;
        }

        EnumEntryType entryType = telematikIdInfo.getProfessionOIDInfos().get(0).getEntryType();

        if (telematikIdInfo.getProfessionOIDInfos().get(0).isOrganization()) {
            if (nachname == null) {
                setNachname(anzeigeName);
            }
            if (allgemeinerName == null) {
                setAllgemeinerName(anzeigeName);
            }

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

        //handle address data
        if (telematikIdInfo.getProfessionOIDInfos().get(0).isOrganization()) {
            setBundesland(mandant.getBundesland());
        }
        else {
            if (getStrasseUndHausnummer() == null
                ||
                getStrasseUndHausnummer().trim().isEmpty()
                ||
                getPlz() == null
                ||
                getPlz().trim().isEmpty()
                ||
                getOrt() == null
                ||
                getOrt().trim().isEmpty()
                ||
                getBundesland() == null
                ||
                getLaenderCode() == null
                ||
                getLaenderCode().trim().isEmpty()
            ) {
                //strasse und hausnummer
                if (directoryEntry.extractDirectoryEntryStreetAddress() != null
                    &&
                    !directoryEntry.extractDirectoryEntryStreetAddress().trim().isEmpty()
                ) {
                    setStrasseUndHausnummer("");
                }
                else {
                    setStrasseUndHausnummer(null);
                }

                //plz
                if (directoryEntry.extractDirectoryEntryPostalCode() != null
                    &&
                    !directoryEntry.extractDirectoryEntryPostalCode().trim().isEmpty()
                ) {
                    setPlz("");
                }
                else {
                    setPlz(null);
                }

                //ort
                if (directoryEntry.extractDirectoryEntryLocalityName() != null
                    &&
                    !directoryEntry.extractDirectoryEntryLocalityName().trim().isEmpty()
                ) {
                    setOrt("");
                }
                else {
                    setOrt(null);
                }

                //ort
                if (directoryEntry.extractDirectoryEntryCountryCode() != null
                    &&
                    !directoryEntry.extractDirectoryEntryCountryCode().trim().isEmpty()
                ) {
                    setLaenderCode("");
                }
                else {
                    setLaenderCode(null);
                }

                setBundesland(null);
            }
        }

        //special komfort handling -> lower versions
        if (getEintragsTyp() == null) {
            setEintragsTyp(entryType);
        }
        if (getLaenderCode() == null && directoryEntry.extractDirectoryEntryCountryCode() != null) {
            setLaenderCode(directoryEntry.extractDirectoryEntryCountryCode());
        }
        if (getMaxKomLeAdr() == null && directoryEntry.extractDirectoryEntryMaxKOMLEadr() != null) {
            setMaxKomLeAdr(directoryEntry.extractDirectoryEntryMaxKOMLEadr());
        }

        boolean toUpdate = createHash() != createHash(directoryEntry);

        //besitzer check
        if (!besitzer.contains(tiVZDProperties.getAuthId())) {
            besitzer.add(tiVZDProperties.getAuthId());
        }
        //check on equal
        List<String> checkHolder = besitzer.stream().filter(s -> !directoryEntry.extractDirectoryEntryHolder().contains(s)).collect(Collectors.toList());
        List<String> reverseCheckHolder = directoryEntry.extractDirectoryEntryHolder().stream().filter(s -> !besitzer.contains(s)).collect(Collectors.toList());
        if (!checkHolder.isEmpty() || !reverseCheckHolder.isEmpty()) {
            toUpdate = true;
        }

        return toUpdate;
    }

    public boolean toDelete() throws Exception {
        return toDelete;
    }

    private int createHash() {
        int compareDataCode = 0;

        //ids
        if (telematikID != null) {
            compareDataCode = compareDataCode + (telematikID.trim().replaceAll(" ", "").hashCode() * 3);
        }
        if (sektorIds != null) {
            String sektorIdStr = sektorIds.stream().map( n -> n.toString() ).collect( Collectors.joining( "," ) );
            compareDataCode = compareDataCode + (sektorIdStr.trim().replaceAll(" ", "").hashCode() * 5);
        }
        if (lanrs != null) {
            String lanrStr = lanrs.stream().map( n -> n.toString() ).collect( Collectors.joining( "," ) );
            compareDataCode = compareDataCode + (lanrStr.trim().replaceAll(" ", "").hashCode() * 7);
        }
        if (telematikIDMutterOrgEinheit != null) {
            compareDataCode = compareDataCode + (telematikIDMutterOrgEinheit.trim().replaceAll(" ", "").hashCode() * 11);
        }

        //name
        if (vorname != null) {
            compareDataCode = compareDataCode + (vorname.trim().replaceAll(" ", "").hashCode() * 13);
        }
        if (nachname != null) {
            compareDataCode = compareDataCode + (nachname.trim().replaceAll(" ", "").hashCode() * 17);
        }
        if (anzeigeName != null) {
            compareDataCode = compareDataCode + (anzeigeName.trim().replaceAll(" ", "").hashCode() * 19);
        }
        if (titel != null) {
            compareDataCode = compareDataCode + (titel.trim().replaceAll(" ", "").hashCode() * 23);
        }
        if (andererName != null) {
            compareDataCode = compareDataCode + (andererName.trim().replaceAll(" ", "").hashCode() * 29);
        }
        if (allgemeinerName != null) {
            compareDataCode = compareDataCode + (allgemeinerName.trim().replaceAll(" ", "").hashCode() * 31);
        }
        if (organisation != null) {
            compareDataCode = compareDataCode + (organisation.trim().replaceAll(" ", "").hashCode() * 37);
        }

        //address
        if (strasseUndHausnummer != null) {
            compareDataCode = compareDataCode + (strasseUndHausnummer.trim().replaceAll(" ", "").hashCode() * 41);
        }
        if (plz != null) {
            compareDataCode = compareDataCode + (plz.trim().replaceAll(" ", "").hashCode() * 43);
        }
        if (ort != null) {
            compareDataCode = compareDataCode + (ort.trim().replaceAll(" ", "").hashCode() * 47);
        }
        if (bundesland != null) {
            compareDataCode = compareDataCode + (bundesland.getHrText().trim().replaceAll(" ", "").hashCode() * 53);
        }
        if (laenderCode != null) {
            compareDataCode = compareDataCode + (laenderCode.trim().replaceAll(" ", "").hashCode() * 59);
        }

        //profession
        if (fachrichtungen != null) {
            String fachrichtungenStr = fachrichtungen.stream().map( n -> n.toString() ).collect( Collectors.joining( "," ) );
            compareDataCode = compareDataCode + (fachrichtungenStr.trim().replaceAll(" ", "").hashCode() * 61);
        }
        if (eintragsTyp != null) {
            compareDataCode = compareDataCode + (eintragsTyp.getId().trim().replaceAll(" ", "").hashCode() * 67);
        }

        //system
        if (maxKomLeAdr != null) {
            compareDataCode = compareDataCode + (maxKomLeAdr.trim().replaceAll(" ", "").hashCode() * 71);
        }
        //active
        if (aktiv != null && aktiv.getDataValue() != null) {
            compareDataCode = compareDataCode + (aktiv.getDataValue().hashCode() * 73);
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
            String sektorIdStr = directoryEntry.extractDirectoryEntryDomainID().stream().map( n -> n.toString() ).collect( Collectors.joining( "," ) );
            compareDataCode = compareDataCode + (sektorIdStr.trim().replaceAll(" ", "").hashCode() * 5);
        }
        if (directoryEntry.extractDirectoryEntryLanr() != null) {
            String lanrStr = directoryEntry.extractDirectoryEntryLanr().stream().map( n -> n.toString() ).collect( Collectors.joining( "," ) );
            compareDataCode = compareDataCode + (lanrStr.trim().replaceAll(" ", "").hashCode() * 7);
        }
        if (directoryEntry.extractDirectoryEntryProvidedBy() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryProvidedBy().trim().replaceAll(" ", "").hashCode() * 11);
        }

        //name
        //vorname
        if (directoryEntry.extractDirectoryEntryGivenName() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryGivenName().trim().replaceAll(" ", "").hashCode() * 13);
        }
        //nachname
        if (directoryEntry.extractDirectoryEntrySn() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntrySn().trim().replaceAll(" ", "").hashCode() * 17);
        }
        //anzeigeName
        if (directoryEntry.extractDirectoryEntryDisplayName() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryDisplayName().trim().replaceAll(" ", "").hashCode() * 19);
        }
        //titel
        if (directoryEntry.extractDirectoryEntryTitle() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryTitle().trim().replaceAll(" ", "").hashCode() * 23);
        }
        //andererName
        if (directoryEntry.extractDirectoryEntryOtherName() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryOtherName().trim().replaceAll(" ", "").hashCode() * 29);
        }
        //allgemeinerName
        if (directoryEntry.extractDirectoryEntryCn() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryCn().trim().replaceAll(" ", "").hashCode() * 31);
        }
        //organisation
        if (directoryEntry.extractDirectoryEntryOrganization() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryOrganization().trim().replaceAll(" ", "").hashCode() * 37);
        }

        //address
        //strasseUndHausnummer
        if (directoryEntry.extractDirectoryEntryStreetAddress() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryStreetAddress().trim().replaceAll(" ", "").hashCode() * 41);
        }
        //plz
        if (directoryEntry.extractDirectoryEntryPostalCode() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryPostalCode().trim().replaceAll(" ", "").hashCode() * 43);
        }
        //ort
        if (directoryEntry.extractDirectoryEntryLocalityName() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryLocalityName().trim().replaceAll(" ", "").hashCode() * 47);
        }
        //bundesland
        if (directoryEntry.extractDirectoryEntryStateOrProvinceName() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryStateOrProvinceName().trim().replaceAll(" ", "").hashCode() * 53);
        }
        //laenderCode
        if (directoryEntry.extractDirectoryEntryCountryCode() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryCountryCode().trim().replaceAll(" ", "").hashCode() * 59);
        }

        //profession
        //fachrichtungen
        if (directoryEntry.extractDirectoryEntrySpecialization() != null) {
            String fachrichtungenStr = directoryEntry.extractDirectoryEntrySpecialization().stream().map( n -> n.toString() ).collect( Collectors.joining( "," ) );
            compareDataCode = compareDataCode + (fachrichtungenStr.trim().replaceAll(" ", "").hashCode() * 61);
        }
        //eintragsTyp
        if (directoryEntry.extractDirectoryEntryEntryType() != null && !directoryEntry.extractDirectoryEntryEntryType().isEmpty()) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryEntryType().get(0).trim().replaceAll(" ", "").hashCode() * 67);
        }

        //system
        //maxKomLeAdr
        if (directoryEntry.extractDirectoryEntryMaxKOMLEadr() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryMaxKOMLEadr().trim().replaceAll(" ", "").hashCode() * 71);
        }
        //active
        if (directoryEntry.extractDirectoryEntryActive() != null && directoryEntry.extractDirectoryEntryActive().getDataValue() != null) {
            compareDataCode = compareDataCode + (directoryEntry.extractDirectoryEntryActive().getDataValue().hashCode() * 73);
        }

        return compareDataCode;
    }
}
