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

import de.gematik.ws.cm.pers.hba_smc_b.v1.ExtCertType;
import de.gematik.ws.cm.pers.hba_smc_b.v1.HbaAntragExport;
import de.gematik.ws.cm.pers.hba_smc_b.v1.ProdResultType;
import de.gematik.ws.cm.pers.hba_smc_b.v1.SmcbAntragExport;
import net.sberg.elbook.stammdatenzertimportcmpts.EncZertifikat;
import net.sberg.elbook.stammdatenzertimportcmpts.VerzeichnisdienstImportCommand;
import org.apache.commons.codec.binary.Base64;

import java.util.*;

public enum EnumSektor {

    APOTHEKE("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#smc-b-eintrag-f%C3%BCr-apotheken-abda-gematik") {
        public String getBusinessId(String telematikId) {
            return String.valueOf(Integer.parseInt(telematikId.split("\\.")[2]));
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            if (!verzeichnisdienstImportCommand.getHelperSmcbAntragExports().isEmpty()) {

                Map<String, VerzeichnisdienstImportCommand> hMap = new HashMap<>();
                Map<String, VerzeichnisdienstImportCommand> hVersandhandelMap = new HashMap<>();
                Map<String, VerzeichnisdienstImportCommand> hHeimversorgungMap = new HashMap<>();
                Map<String, VerzeichnisdienstImportCommand> hKrankenhausversorgungMap = new HashMap<>();
                Map<String, VerzeichnisdienstImportCommand> hSterilherstellungMap = new HashMap<>();

                for (Iterator<SmcbAntragExport> iterator = verzeichnisdienstImportCommand.getHelperSmcbAntragExports().iterator(); iterator.hasNext(); ) {

                    SmcbAntragExport smcbAntragExport = iterator.next();
                    String telematikId = smcbAntragExport.getInstitution().getTelematikID().getValue();
                    if (!hMap.containsKey(telematikId)) {
                        hMap.put(telematikId, VerzeichnisdienstImportCommand.copy(verzeichnisdienstImportCommand));
                    }
                    VerzeichnisdienstImportCommand hCmd = hMap.get(telematikId);

                    //save the certs
                    for (Iterator<ProdResultType> iteratored = smcbAntragExport.getProdResult().iterator(); iteratored.hasNext(); ) {
                        ProdResultType prodResultType = iteratored.next();
                        for (Iterator<ExtCertType> zertIterator = prodResultType.getZertifikate().iterator(); zertIterator.hasNext(); ) {
                            ExtCertType extCertType = zertIterator.next();
                            if (extCertType.getCertificateSem().contains(".ENC.")) {
                                EncZertifikat encZertifikat = new EncZertifikat();
                                encZertifikat.setContent(Base64.encodeBase64String(extCertType.getCertificateValue()));
                                hCmd.getEncZertifikat().add(encZertifikat);
                            }
                        }
                    }

                    //check on orgEinheiten
                    if (hCmd.getFachrichtungen() == null) {
                        hCmd.setFachrichtungen(new ArrayList<>());
                    }
                    hCmd.getFachrichtungen().clear();
                    hCmd.getFachrichtungen().add("10");
                    String abteilung = smcbAntragExport.getInstitution().getAbteilung();
                    String instName = smcbAntragExport.getInstitution().getInstName();
                    String orgEinheit = "";
                    String handelsName = null;
                    if (abteilung != null && !abteilung.trim().isEmpty()) {
                        if (abteilung.contains("(") && abteilung.contains(")")) {
                            orgEinheit = abteilung.substring(abteilung.indexOf("(")).trim();
                            handelsName = abteilung.substring(abteilung.indexOf("(")+1,abteilung.indexOf(")"));
                        }
                        else {
                            orgEinheit = abteilung.trim();
                        }
                    }
                    else {

                        String handelsNamePattern = null;
                        if (instName.contains("(Versandhandel")) {
                            orgEinheit = "Versandhandel";
                            handelsNamePattern = "(Versandhandel";
                        }
                        else if (instName.contains("(VH")) {
                            orgEinheit = "Versandhandel";
                            handelsNamePattern = "(VH";
                        }
                        else if (instName.contains(" - Versandhandel")) {
                            orgEinheit = "Versandhandel";
                            handelsNamePattern = "- Versandhandel";
                        }
                        else if (instName.contains(" - VH")) {
                            orgEinheit = "Versandhandel";
                            handelsNamePattern = " - VH";
                        }

                        if (handelsNamePattern != null) {
                            handelsName = instName.substring(instName.indexOf(handelsNamePattern) + handelsNamePattern.length());
                            handelsName = handelsName.replaceAll("\\)", "").trim();
                        }

                        //Heimversorgung
                        if (instName.contains("(Heimversorgung")) {
                            orgEinheit = "Heimversorgung";
                        }
                        else if (instName.contains("(HV")) {
                            orgEinheit = "Heimversorgung";
                        }
                        else if (instName.contains(" - Heimversorgung")) {
                            orgEinheit = "Heimversorgung";
                        }
                        else if (instName.contains(" - HV")) {
                            orgEinheit = "Heimversorgung";
                        }
                        //Krankenhausversorgung
                        else if (instName.contains("(Krankenhausversorgung")) {
                            orgEinheit = "Krankenhausversorgung";
                        }
                        else if (instName.contains("(KHV")) {
                            orgEinheit = "Krankenhausversorgung";
                        }
                        else if (instName.contains(" - Krankenhausversorgung")) {
                            orgEinheit = "Krankenhausversorgung";
                        }
                        else if (instName.contains(" - KHV")) {
                            orgEinheit = "Krankenhausversorgung";
                        }
                        //Sterilherstellung
                        else if (instName.contains("(Sterilherstellung")) {
                            orgEinheit = "Sterilherstellung";
                        }
                        else if (instName.contains("(SH")) {
                            orgEinheit = "Sterilherstellung";
                        }
                        else if (instName.contains(" - Sterilherstellung")) {
                            orgEinheit = "Sterilherstellung";
                        }
                        else if (instName.contains(" - SH")) {
                            orgEinheit = "Sterilherstellung";
                        }
                    }

                    if (orgEinheit.equals("Versandhandel")) {
                        hCmd.getFachrichtungen().clear();
                        hCmd.getFachrichtungen().add("40");
                        if (!hVersandhandelMap.containsKey(telematikId)) {
                            hVersandhandelMap.put(telematikId, hCmd);
                        }
                    }
                    else if (orgEinheit.equals("Heimversorgung")) {
                        hCmd.getFachrichtungen().clear();
                        hCmd.getFachrichtungen().add("30");
                        if (!hHeimversorgungMap.containsKey(telematikId)) {
                            hHeimversorgungMap.put(telematikId, hCmd);
                        }
                    }
                    else if (orgEinheit.equals("Krankenhausversorgung")) {
                        hCmd.getFachrichtungen().clear();
                        hCmd.getFachrichtungen().add("20");
                        if (!hKrankenhausversorgungMap.containsKey(telematikId)) {
                            hKrankenhausversorgungMap.put(telematikId, hCmd);
                        }
                    }
                    else if (orgEinheit.equals("Sterilherstellung")) {
                        hCmd.getFachrichtungen().clear();
                        hCmd.getFachrichtungen().add("50");
                        if (!hSterilherstellungMap.containsKey(telematikId)) {
                            hSterilherstellungMap.put(telematikId, hCmd);
                        }
                    }

                    if (!orgEinheit.isEmpty() && handelsName == null) {
                        hCmd.setAnzeigeName(hCmd.getAnzeigeName()+" ("+orgEinheit+")");

                    }
                    else if (!orgEinheit.isEmpty() && handelsName != null) {
                        hCmd.setAnzeigeName(handelsName+" ("+orgEinheit+" - "+hCmd.getAnzeigeName()+")");
                    }
                }

                //handle ignore
                List<VerzeichnisdienstImportCommand> res = new ArrayList<>();
                if (hVersandhandelMap.size() == 1) {
                    hVersandhandelMap.keySet().forEach(s -> {
                        hVersandhandelMap.get(s).setTelematikID(s);
                        hVersandhandelMap.get(s).setToIgnore(false);
                        res.add(hVersandhandelMap.get(s));
                        hMap.remove(s);
                    });
                }
                if (hHeimversorgungMap.size() == 1) {
                    hHeimversorgungMap.keySet().forEach(s -> {
                        hHeimversorgungMap.get(s).setToIgnore(false);
                        hHeimversorgungMap.get(s).setTelematikID(s);
                        res.add(hHeimversorgungMap.get(s));
                        hMap.remove(s);
                    });
                }
                if (hKrankenhausversorgungMap.size() == 1) {
                    hKrankenhausversorgungMap.keySet().forEach(s -> {
                        hKrankenhausversorgungMap.get(s).setToIgnore(false);
                        hKrankenhausversorgungMap.get(s).setTelematikID(s);
                        res.add(hKrankenhausversorgungMap.get(s));
                        hMap.remove(s);
                    });
                }
                if (hSterilherstellungMap.size() == 1) {
                    hSterilherstellungMap.keySet().forEach(s -> {
                        hSterilherstellungMap.get(s).setToIgnore(false);
                        hSterilherstellungMap.get(s).setTelematikID(s);
                        res.add(hSterilherstellungMap.get(s));
                        hMap.remove(s);
                    });
                }

                if (hMap.size() == 1) {
                    hMap.keySet().forEach(s -> {
                        hMap.get(s).setToIgnore(false);
                        hMap.get(s).setTelematikID(s);
                        res.add(hMap.get(s));
                    });
                }

                return res;
            }
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    APOTHEKER("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#hba-eintrag-f%C3%BCr-apotheker-pharmazieingenieure-und-apothekerassistenten-abda-gematik") {
        public String getBusinessId(String telematikId) {
            return String.valueOf(Integer.parseInt(telematikId.split("\\.")[2]));
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            if (!verzeichnisdienstImportCommand.getHelperHbaAntragExports().isEmpty() && verzeichnisdienstImportCommand.getHelperTelematikIDs().size() == 1) {
                verzeichnisdienstImportCommand.setToIgnore(false);
                for (Iterator<HbaAntragExport> iterator = verzeichnisdienstImportCommand.getHelperHbaAntragExports().iterator(); iterator.hasNext(); ) {
                    HbaAntragExport hbaAntragExport = iterator.next();
                    verzeichnisdienstImportCommand.setTelematikID(hbaAntragExport.getFreigabedaten().getTelematikID());
                    for (Iterator<ProdResultType> iteratored = hbaAntragExport.getProdResult().iterator(); iteratored.hasNext(); ) {
                        ProdResultType prodResultType = iteratored.next();
                        for (Iterator<ExtCertType> zertIterator = prodResultType.getZertifikate().iterator(); zertIterator.hasNext(); ) {
                            ExtCertType extCertType = zertIterator.next();
                            if (extCertType.getCertificateSem().contains(".ENC.")) {
                                EncZertifikat encZertifikat = new EncZertifikat();
                                encZertifikat.setContent(Base64.encodeBase64String(extCertType.getCertificateValue()));
                                verzeichnisdienstImportCommand.getEncZertifikat().add(encZertifikat);
                            }
                        }
                    }
                }
                return List.of(verzeichnisdienstImportCommand);
            }
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    ARZT("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#%C3%A4rzte-kammern-hba-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    ARZTPRAXIS("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#kassen%C3%A4rztliche-vereinigungen-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    ZAHNARZT("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#zahn%C3%A4rzte-kammern-hba-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    ZAHNARZTPRAXIS("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#kassenzahn%C3%A4rztliche-vereinigungen-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    PSYCHOTHERAPEUTH("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#psychotherapeuten-hba-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    KRANKENHAUS("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#dktig-i-a-d-dkg-krankenhaus-krankenhausapotheke-vorsorge-und-rehabilitationseinrichtungen-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    GKV("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#gkv-sv-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    GKV_EPA("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#gkv-sv-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    GEMATIK_SMCB_WAEI("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#gematik-weitere-%C3%A4rztliche-institutionen-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    GEMATIK_SMCB_ORG("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#smc-b-org-eintrag-gematik") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    GEMATIK_PKV_KOSTENTRAEGER("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#gematik-kostentr%C3%A4ger-pkv-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    GEMATIK_DIGA("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#gematik-diga-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    EGBR_HBA("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#egbr-weitere-gesundheitsfachberufe-hba-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
        }
    },
    EGBR_SMCB("https://github.com/gematik/api-vzd/blob/main/docs/gemILF_Pflege_VZD.adoc#egbr-nicht%C3%A4rztliche-institutionen-smc-b-eintrag") {
        public String getBusinessId(String telematikId) {
            throw new IllegalStateException("not implemented");
        }
        public List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand) {
            return List.of(verzeichnisdienstImportCommand);
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
    public abstract List<VerzeichnisdienstImportCommand> checkCommand(VerzeichnisdienstImportCommand verzeichnisdienstImportCommand);
}
