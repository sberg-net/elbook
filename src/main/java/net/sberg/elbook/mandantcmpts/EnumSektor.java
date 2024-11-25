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

public enum EnumSektor {

    APOTHEKE("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#smc-b-eintrag-f%C3%BCr-apotheken-abda-gematik") {
        public String getBusinessId(String telematikId) {
            return String.valueOf(Integer.parseInt(telematikId.split("\\.")[2]));
        }
    },
    APOTHEKER("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#hba-eintrag-f%C3%BCr-apotheker-pharmazieingenieure-und-apothekerassistenten-abda-gematik") {
        public String getBusinessId(String telematikId) {
            return String.valueOf(Integer.parseInt(telematikId.split("\\.")[2]));
        }
    },
    ARZT("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#%C3%A4rzte-kammern-hba-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    ARZTPRAXIS("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#kassen%C3%A4rztliche-vereinigungen-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    ZAHNARZT("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#zahn%C3%A4rzte-kammern-hba-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    ZAHNARZTPRAXIS("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#kassenzahn%C3%A4rztliche-vereinigungen-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    PSYCHOTHERAPEUTH("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#psychotherapeuten-hba-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    KRANKENHAUS("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#dktig-i-a-d-dkg-krankenhaus-krankenhausapotheke-vorsorge-und-rehabilitationseinrichtungen-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    GKV("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#gkv-sv-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    GKV_EPA("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#gkv-sv-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    GEMATIK_SMCB("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#gematik-weitere-%C3%A4rztliche-institutionen-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    GEMATIK_SMCB_ORG("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#smc-b-org-eintrag-gematik") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    GEMATIK_PKV_KOSTENTRAEGER("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#gematik-kostentr%C3%A4ger-pkv-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    GEMATIK_DIGA("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#gematik-diga-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    EGBR_HBA("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#egbr-weitere-gesundheitsfachberufe-hba-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    },
    EGBR_SMCB("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#egbr-nicht%C3%A4rztliche-institutionen-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
    };

    private String implLeitfadenUrl;

    private EnumSektor(String implLeitfadenUrl) {
        this.implLeitfadenUrl = implLeitfadenUrl;
    }

    public String getImplLeitfadenUrl() {
        return implLeitfadenUrl;
    }

    public abstract String getBusinessId(String telematikId);
}
