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

import lombok.Data;
import net.sberg.elbook.vzdclientcmpts.command.EnumEntryType;
import net.sberg.elbook.vzdclientcmpts.command.EnumStateOrProvinceName;
import net.sberg.elbook.vzdclientcmpts.command.EnumTriValue;

import java.util.ArrayList;
import java.util.List;

@Data
public class VerzeichnisdienstImportErgebnis {

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
    private String titel;
    private String anzeigeName;
    private String organisation;

    // address attributes
    private String strasseUndHausnummer;
    private String plz;
    private String ort;
    private EnumStateOrProvinceName bundesland;
    private String laenderCode;

    // profession attributes
    private List<String> fachrichtungen;
    private EnumEntryType eintragstyp;

    // system attributes
    private String maxKomLeAdr;
    private EnumTriValue aktiv;
    private List<String> besitzer;

    private List<String> log = new ArrayList<>();
    private boolean error = false;
    private boolean insert = false;
    private boolean update = false;
    private boolean certUpdate = false;
    private boolean delete = false;
    private boolean silentMode = false;
    private boolean ignore = false;

    public void fill(VerzeichnisdienstImportCommand command) {

        setVerwaltungsId(command.getVerwaltungsId());
        setTelematikID(command.getTelematikID());
        setVzdUid(command.getVzdUid());
        setSektorIds(command.getSektorIds());
        setLanrs(command.getLanrs());
        setTelematikIDMutterOrgEinheit(command.getTelematikIDMutterOrgEinheit());

        setVorname(command.getVorname());
        setNachname(command.getNachname());
        setAllgemeinerName(command.getAllgemeinerName());
        setAndererName(command.getAndererName());
        setTitel(command.getTitel());
        setAnzeigeName(command.getAnzeigeName());
        setOrganisation(command.getOrganisation());

        setStrasseUndHausnummer(command.getStrasseUndHausnummer());
        setOrt(command.getOrt());
        setPlz(command.getPlz());
        setBundesland(command.getBundesland());
        setLaenderCode(command.getLaenderCode());

        setFachrichtungen(command.getFachrichtungen());
        setEintragstyp(command.getEintragsTyp());

        setMaxKomLeAdr(command.getMaxKomLeAdr());
        setAktiv(command.getAktiv());
        setBesitzer(command.getBesitzer());

        setIgnore(command.isToIgnore());
    }

}
