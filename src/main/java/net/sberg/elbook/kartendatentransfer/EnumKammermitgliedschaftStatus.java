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

public enum EnumKammermitgliedschaftStatus {
    ABGEHEND_GRUND_BERUF("Berufsangehöriger übt den Beruf in unserem Kammerbezirk aus und ist aus diesem Grund Kammermitglied."),
    ABGEHEND_GRUND_SONSTIGER("Die Kammermitgliedschaft des Berufsangehörigen besteht aufgrund sonstiger Umstände."),
    ABGEHEND_KEIN("Es besteht keine Mitgliedschaft des Berufsangehörigen in unserer Kammer."),
    EMPFANGEND_GRUND_BERUF_SONSTIGER("Uns ist bekannt, dass der Berufsangehörige den Beruf in Ihrem Kammerbezirk ausübt bzw. aufgrund anderer Umstände Mitglied in Ihrer Kammer ist."),
    UNBEKANNT("Unbekannt");

    private String hrText;
    private EnumKammermitgliedschaftStatus(String hrText) {
        this.hrText = hrText;
    }
    public String getHrText() {
        return hrText;
    }
}
