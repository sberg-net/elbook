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
package net.sberg.elbook.tspcmpts;

public enum EnumTspName {
    BUNDESDRUCKEREI("Bundesdruckerei", QVDA.BUNDESDRUCKEREI),
    MEDISIGN("Medisign", QVDA.MEDISIGN),
    SHC ("SHC Stolle & Heinz", QVDA.SHC),
    TSYSTEMS ("T-Systems", QVDA.TSYSTEMS);

    private String hrText;
    private QVDA qvda;
    private EnumTspName(String hrText, QVDA qvda) {
        this.hrText = hrText;
        this.qvda = qvda;
    }
    public String getHrText() {
        return hrText;
    }
    public QVDA getQvda() { return qvda; }
}
