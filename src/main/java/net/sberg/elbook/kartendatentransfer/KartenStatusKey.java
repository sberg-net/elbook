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

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "KartenStatusKey")
@XmlEnum
public enum KartenStatusKey {

    @XmlEnumValue("Unbekannt")
    UNBEKANNT("Unbekannt"),
    @XmlEnumValue("Auslieferung")
    AUSLIEFERUNG("Auslieferung"),
    @XmlEnumValue("Fristüberschreitung Empfangsbestätigung")
    FRISTUEBERSCHREITUNG_EMPFANGSBESTAETIGUNG("Fristüberschreitung Empfangsbestätigung"),
    @XmlEnumValue("Karte ausgeliefert")
    KARTE_AUSGELIEFERT("Karte ausgeliefert"),
    @XmlEnumValue("Karte unzustellbar zurück")
    KARTE_UNZUSTELLBAR_ZURUECK("Karte unzustellbar zurück"),
    @XmlEnumValue("Sperrung beantragt")
    SPERRUNG_BEANTRAGT("Sperrung beantragt"),
    @XmlEnumValue("Zertifikate abgelaufen")
    ZERTIFIKATE_ABGELAUFEN("Zertifikate abgelaufen"),
    @XmlEnumValue("Zertifikate endgültig nicht freigeschaltet")
    ZERTIFIKATE_ENDGUELTIG_NICHT_FREIGESCHALTET("Zertifikate endgültig nicht freigeschaltet"),
    @XmlEnumValue("Zertifikate freigeschaltet")
    ZERTIFIKATE_FREIGESCHALTET("Zertifikate freigeschaltet"),
    @XmlEnumValue("Zertifikate gesperrt")
    ZERTIFIKATE_GESPERRT("Zertifikate gesperrt");
    private final String value;

    KartenStatusKey(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static KartenStatusKey fromValue(String v) {
        for (KartenStatusKey c: KartenStatusKey.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
