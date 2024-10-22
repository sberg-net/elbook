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
package net.sberg.elbook.mandantcmpts;

import net.sberg.elbook.tspcmpts.EnumAntragTyp;
import net.sberg.elbook.vzdclientcmpts.command.EnumEntryType;

public enum EnumSektor {

    APOTHEKE(EnumEntryType.Leistungserbringerinstitution, EnumAntragTyp.SMCB) {
        public String getBusinessId(String telematikId) {
            return String.valueOf(Integer.parseInt(telematikId.split("\\.")[2]));
        }
    },
    APOTHEKER(EnumEntryType.Berufsgruppe, EnumAntragTyp.HBA){
        public String getBusinessId(String telematikId) {
            return String.valueOf(Integer.parseInt(telematikId.split("\\.")[2]));
        }
    },
    ARZT(EnumEntryType.Berufsgruppe, EnumAntragTyp.HBA){
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    ARZTPRAXIS(EnumEntryType.Leistungserbringerinstitution, EnumAntragTyp.SMCB){
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    ZAHNARZT(EnumEntryType.Berufsgruppe, EnumAntragTyp.HBA){
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    ZAHNARZTPRAXIS(EnumEntryType.Leistungserbringerinstitution, EnumAntragTyp.SMCB){
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    PSYCHOTHERAPEUTH(EnumEntryType.Berufsgruppe, EnumAntragTyp.HBA){
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    KRANKENHAUS(EnumEntryType.Leistungserbringerinstitution, EnumAntragTyp.SMCB){
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    GKV(EnumEntryType.Krankenkasse, EnumAntragTyp.SMCB){
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    GKV_EPA(EnumEntryType.Krankenkasse_ePA, EnumAntragTyp.SMCB){
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    HBAGEMATIK(EnumEntryType.Berufsgruppe, EnumAntragTyp.HBA){
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    SMCBGEMATIK(EnumEntryType.Organisation, EnumAntragTyp.SMCB){
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    HBAEGBR(EnumEntryType.Berufsgruppe, EnumAntragTyp.HBA){
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    SMCBEGBR(EnumEntryType.Leistungserbringerinstitution, EnumAntragTyp.SMCB){
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    };

    private EnumEntryType entryType;
    private EnumAntragTyp antragTyp;

    private EnumSektor(EnumEntryType entryType, EnumAntragTyp antragTyp) {
        this.entryType = entryType;
        this.antragTyp = antragTyp;
    }

    public EnumEntryType getEntryType() {
        return entryType;
    }
    public EnumAntragTyp getAntragTyp() { return antragTyp; }

    public abstract String getBusinessId(String telematikId);
}
