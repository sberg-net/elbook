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
package net.sberg.elbook.kartendatentransfer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import net.sberg.elbook.common.LocalDateSecondsDeserializer;
import net.sberg.elbook.common.LocalDateSecondsSerializer;

import java.time.LocalDate;

@Data
public class HbaKartendatenTransferCommand {

    @JsonDeserialize(using = LocalDateSecondsDeserializer.class)
    @JsonSerialize(using = LocalDateSecondsSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate geburtsdatum;

    private String geburtsort;
    private ProfessionItemKey berufsgruppe = ProfessionItemKey.APOTHEKER_IN;

    private String vorname;
    private String nachname;
    private String akademischerGrad;

    private String strasse;
    private String hausnummer;
    private String anschriftenzusatz;
    private String postleitzahl;
    private String wohnort;
    private boolean unbekanntVerzogen;
    private EnumApprobationStatus approbationStatus = EnumApprobationStatus.UNBEKANNT;
    private EnumKammermitgliedschaftStatus kammermitgliedschaftStatus = EnumKammermitgliedschaftStatus.UNBEKANNT;

    private EnumKartendatenTransferCommandTyp typ = EnumKartendatenTransferCommandTyp.ABGEBEND;

    private String bemerkung;

    private int anzahlAusweise = 1;

    //ausweis 1
    @JsonDeserialize(using = LocalDateSecondsDeserializer.class)
    @JsonSerialize(using = LocalDateSecondsSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate gueltigVon;
    @JsonDeserialize(using = LocalDateSecondsDeserializer.class)
    @JsonSerialize(using = LocalDateSecondsSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate gueltigBis;
    private QVDA qvda = QVDA.BUNDESDRUCKEREI;
    private String ausweisnummer;
    private String telematikId;
    private KartenStatusKey kartenStatus = KartenStatusKey.UNBEKANNT;

    //ausweis 2
    @JsonDeserialize(using = LocalDateSecondsDeserializer.class)
    @JsonSerialize(using = LocalDateSecondsSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate gueltigVon2;
    @JsonDeserialize(using = LocalDateSecondsDeserializer.class)
    @JsonSerialize(using = LocalDateSecondsSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate gueltigBis2;
    private QVDA qvda2 = QVDA.BUNDESDRUCKEREI;
    private String ausweisnummer2;
    private String telematikId2;
    private KartenStatusKey kartenStatus2 = KartenStatusKey.UNBEKANNT;

    //ausweis 3
    @JsonDeserialize(using = LocalDateSecondsDeserializer.class)
    @JsonSerialize(using = LocalDateSecondsSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate gueltigVon3;
    @JsonDeserialize(using = LocalDateSecondsDeserializer.class)
    @JsonSerialize(using = LocalDateSecondsSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate gueltigBis3;
    private QVDA qvda3 = QVDA.BUNDESDRUCKEREI;
    private String ausweisnummer3;
    private String telematikId3;
    private KartenStatusKey kartenStatus3 = KartenStatusKey.UNBEKANNT;

}
