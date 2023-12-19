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
package net.sberg.elbook.logeintragcmpts;

public enum EnumLogEintragArtikelTyp {

    MANUAL_INSERT("Manuell Eingefügt"),
    MANUAL_UPDATE("Manuell Geändert"),
    MANUAL_DELETE("Manuell Gelöscht"),
    MANUAL_STATE_CHANGED("Manuell Status geändert"),
    MANUAL_CERT_INSERT("Manuell Zertifikat Eingefügt"),
    MANUAL_CERT_DELETE("Manuell Zertifikat Gelöscht"),
    CARD_ISSUER_INSERT("Kartenherausgeber / Eingefügt"),
    CARD_ISSUER_UPDATE("Kartenherausgeber / Geändert"),
    CARD_ISSUER_DELETE("Kartenherausgeber / Gelöscht"),
    CARD_ISSUER_CERT_INSERT("Kartenherausgeber / Zertifikat Eingefügt");

    private String hrText;
    private EnumLogEintragArtikelTyp(String hrText) {
        this.hrText = hrText;
    }
    public String getHrText() {
        return hrText;
    }
}
