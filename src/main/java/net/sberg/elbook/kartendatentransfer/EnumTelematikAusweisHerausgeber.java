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

import net.sberg.elbook.mandantcmpts.Mandant;

public enum EnumTelematikAusweisHerausgeber {
    AK3_01("Landesapothekerkammer Baden-Württemberg"),
    AK3_02("Bayerische Landesapothekerkammer"),
    AK3_03("Apothekerkammer Berlin"),
    AK3_04("Landesapothekerkammer Brandenburg"),
    AK3_05("Apothekerkammer Bremen"),
    AK3_06("Apothekerkammer Hamburg"),
    AK3_07("Landesapothekerkammer Hessen"),
    AK3_08("Apothekerkammer Mecklenburg-Vorpommern"),
    AK3_09("Apothekerkammer Niedersachsen"),
    AK3_10("Apothekerkammer Nordrhein"),
    AK3_11("Landesapothekerkammer Rheinland-Pfalz"),
    AK3_12("Apothekerkammer des Saarlandes"),
    AK3_13("Apothekerkammer Sachsen-Anhalt"),
    AK3_14("Sächsische Landesapothekerkammer"),
    AK3_15("Apothekerkammer Schleswig-Holstein"),
    AK3_16("Landesapothekerkammer Thüringen"),
    AK3_17("Apothekerkammer Westfalen-Lippe");

    private String hrText;
    private EnumTelematikAusweisHerausgeber(String hrText) {
        this.hrText = hrText;
    }
    public String getHrText() {
        return hrText;
    }

    public static final EnumTelematikAusweisHerausgeber fromKuerzel(String kuerzel) {
        switch (kuerzel) {
            case Mandant.KUERZEL_3_01: return AK3_01;
            case Mandant.KUERZEL_3_02: return AK3_02;
            case Mandant.KUERZEL_3_03: return AK3_03;
            case Mandant.KUERZEL_3_04: return AK3_04;
            case Mandant.KUERZEL_3_05: return AK3_05;
            case Mandant.KUERZEL_3_06: return AK3_06;
            case Mandant.KUERZEL_3_07: return AK3_07;
            case Mandant.KUERZEL_3_08: return AK3_08;
            case Mandant.KUERZEL_3_09: return AK3_09;
            case Mandant.KUERZEL_3_10: return AK3_10;
            case Mandant.KUERZEL_3_11: return AK3_11;
            case Mandant.KUERZEL_3_12: return AK3_12;
            case Mandant.KUERZEL_3_13: return AK3_13;
            case Mandant.KUERZEL_3_14: return AK3_14;
            case Mandant.KUERZEL_3_15: return AK3_15;
            case Mandant.KUERZEL_3_16: return AK3_16;
            case Mandant.KUERZEL_3_17: return AK3_17;
            default: throw new IllegalStateException("unbekanntes Kuerzel: "+kuerzel);
        }
    }
}
