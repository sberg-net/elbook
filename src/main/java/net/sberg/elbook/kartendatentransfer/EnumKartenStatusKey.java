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

import de.gematik.ws.cm.pers.hba_smc_b.v1.KartenStatusKey;
import jakarta.xml.bind.annotation.XmlEnumValue;

public enum EnumKartenStatusKey {

    @XmlEnumValue("Unbekannt")
    UNBEKANNT("Unbekannt"),
    @XmlEnumValue("Auslieferung")
    AUSLIEFERUNG("Auslieferung"),
    @XmlEnumValue("Frist\u00fcberschreitung Empfangsbest\u00e4tigung")
    FRISTUEBERSCHREITUNG_EMPFANGSBESTAETIGUNG("Frist\u00fcberschreitung Empfangsbest\u00e4tigung"),
    @XmlEnumValue("Karte ausgeliefert")
    KARTE_AUSGELIEFERT("Karte ausgeliefert"),
    @XmlEnumValue("Karte unzustellbar zur\u00fcck")
    KARTE_UNZUSTELLBAR_ZURUECK("Karte unzustellbar zur\u00fcck"),
    @XmlEnumValue("Sperrung beantragt")
    SPERRUNG_BEANTRAGT("Sperrung beantragt"),
    @XmlEnumValue("Zertifikate abgelaufen")
    ZERTIFIKATE_ABGELAUFEN("Zertifikate abgelaufen"),
    @XmlEnumValue("Zertifikate endg\u00fcltig nicht freigeschaltet")
    ZERTIFIKATE_ENDGUELTIG_NICHT_FREIGESCHALTET("Zertifikate endg\u00fcltig nicht freigeschaltet"),
    @XmlEnumValue("Zertifikate freigeschaltet")
    ZERTIFIKATE_FREIGESCHALTET("Zertifikate freigeschaltet"),
    @XmlEnumValue("Zertifikate gesperrt")
    ZERTIFIKATE_GESPERRT("Zertifikate gesperrt");
    private final String value;

    EnumKartenStatusKey(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumKartenStatusKey fromValue(String v) {
        for (EnumKartenStatusKey c: EnumKartenStatusKey.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

