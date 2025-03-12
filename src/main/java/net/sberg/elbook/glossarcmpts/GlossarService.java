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
package net.sberg.elbook.glossarcmpts;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import net.sberg.elbook.mandantcmpts.EnumSektor;
import net.sberg.elbook.tspcmpts.EnumAntragTyp;
import net.sberg.elbook.vzdclientcmpts.command.EnumEntryType;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GlossarService {

    private final Map<String, HolderInfo> holderStorage = new Hashtable<>();
    private final Map<String, ProfessionOIDInfoReduced> professionOIDStorage = new Hashtable<>();
    private final Map<String, TelematikIdPattern> telematikIdPatternStorage = new Hashtable<>();
    private final Map<String, SpecializationInfo> specializationStorage = new Hashtable<>();

    public static final String SEARCHVALUE_ALL_ELEMS = "*";

    @PostConstruct
    public void startup() throws Exception {
        //read holder
        holderStorage.clear();
        Map r = new ObjectMapper().readValue(getClass().getResourceAsStream("/glossar/CodeSystem-HolderCS.json"), Map.class);
        List l = (List) r.get("concept");
        l.forEach(o -> {
            HolderInfo holder = new HolderInfo();
            holder.setCode((String)((Map)o).get("code"));
            holder.setDisplay((String)((Map)o).get("display"));
            holderStorage.put(holder.getCode(), holder);
        });

        //read telematikid pattern
        telematikIdPatternStorage.clear();
        r = new ObjectMapper().readValue(getClass().getResourceAsStream("/glossar/TelematikID.mapping.json"), Map.class);
        l = (List) r.get("mapping");
        l.forEach(o -> {
            TelematikIdPattern telematikIdPattern = new TelematikIdPattern();
            telematikIdPattern.setCode((String)((Map)o).get("code"));
            telematikIdPattern.setPattern((String)((Map)o).get("pattern"));
            telematikIdPattern.setDisplayShort((String)((Map)o).get("displayShort"));
            telematikIdPattern.setFhirResourceType((String)((Map)o).get("fhirResourceType"));
            telematikIdPattern.setProfessionOIDs((String)((Map)o).get("professionOIDs"));
            telematikIdPattern.setSektor(EnumSektor.valueOf((String)((Map)o).get("sektor")));
            telematikIdPattern.setSektorImplLeitfadenUrl(telematikIdPattern.getSektor().getImplLeitfadenUrl());
            telematikIdPattern.setTelematikIdBildungsregelUrl((String)((Map)o).get("telematikIdBildungsregelUrl"));
            telematikIdPatternStorage.put(telematikIdPattern.getPattern(), telematikIdPattern);
        });

        //read CodeSystem-OrganizationProfessionOID.json
        r = new ObjectMapper().readValue(getClass().getResourceAsStream("/glossar/CodeSystem-OrganizationProfessionOID.json"), Map.class);
        l = (List) r.get("concept");
        l.forEach(o -> {
            ProfessionOIDInfoReduced professionOIDInfo = new ProfessionOIDInfoReduced();
            professionOIDInfo.setCode((String)((Map)o).get("code"));
            professionOIDInfo.setDisplay((String)((Map)o).get("display"));
            professionOIDInfo.setEntryType(EnumEntryType.valueOf((String)((Map)o).get("entryType")));
            professionOIDInfo.setEntryTypeId(professionOIDInfo.getEntryType().getId());
            professionOIDInfo.setEntryTypeText(professionOIDInfo.getEntryType().getHrText());
            professionOIDInfo.setOrganization(true);
            professionOIDInfo.setPractitionier(false);
            professionOIDInfo.setTspAntragTyp(EnumAntragTyp.SMCB);
            professionOIDStorage.put(professionOIDInfo.getCode(), professionOIDInfo);
        });

        //read CodeSystem-PractitionerProfessionOID.json
        r = new ObjectMapper().readValue(getClass().getResourceAsStream("/glossar/CodeSystem-PractitionerProfessionOID.json"), Map.class);
        l = (List) r.get("concept");
        l.forEach(o -> {
            ProfessionOIDInfoReduced professionOIDInfo = new ProfessionOIDInfoReduced();
            professionOIDInfo.setCode((String)((Map)o).get("code"));
            professionOIDInfo.setDisplay((String)((Map)o).get("display"));
            professionOIDInfo.setEntryType(EnumEntryType.valueOf((String)((Map)o).get("entryType")));
            professionOIDInfo.setEntryTypeId(professionOIDInfo.getEntryType().getId());
            professionOIDInfo.setEntryTypeText(professionOIDInfo.getEntryType().getHrText());
            professionOIDInfo.setOrganization(false);
            professionOIDInfo.setPractitionier(true);
            professionOIDInfo.setTspAntragTyp(EnumAntragTyp.HBA);
            professionOIDStorage.put(professionOIDInfo.getCode(), professionOIDInfo);
        });

        //read specialization
        readSpecialization("CodeSystem-aerztliche-fachrichtungen-oid-url.json");
        readSpecialization("CodeSystem-facharzttitelderaerztekammern-oid-url.json");
        readSpecialization("CodeSystem-kbv-cm-sfhir-bar2-wbo-oid-url.json");
        readSpecialization("CodeSystem-nicht-aerztliche-fachrichtungen-oid-url.json");
        readSpecialization("CodeSystem-PharmacyTypeLDAPCS.json");
        readSpecialization("CodeSystem-qualifikationen-nicht-aerztlicher-autoren-oid-url.json");
        readSpecialization("CodeSystem-qualifikatoren-zahnaerztlicher-autoren-oid-url.json");
        readSpecialization("CodeSystem-zahnaerztliche-fachrichtungen-oid-url.json");
    }

    private void readSpecialization(String name) throws Exception {
        Map r = new ObjectMapper().readValue(getClass().getResourceAsStream("/glossar/specialization/"+name), Map.class);
        String url = (String)r.get("url");
        String codeOffset = url.contains("urn:oid:")?url.substring("urn:oid:".length()):"";
        String description = (String)r.get("description");
        List l = (List) r.get("concept");
        l.forEach(o -> {
            SpecializationInfo specializationInfo = new SpecializationInfo();
            if (codeOffset.isEmpty()) {
                specializationInfo.setCode((String)((Map)o).get("code"));
            }
            else {
                specializationInfo.setCode(codeOffset+":"+(String)((Map)o).get("code"));
            }
            specializationInfo.setDisplay((String)((Map)o).get("display"));
            specializationInfo.setDefinition((String)((Map)o).get("definition"));
            specializationInfo.setDescription(description);
            specializationInfo.setUrl(url);
            specializationStorage.put(specializationInfo.getCode(), specializationInfo);
        });
    }

    public synchronized boolean validHolder(String holder) {
        return holderStorage.containsKey(holder);
    }

    public synchronized ProfessionOIDInfoReduced getProfessionOIDInfo(String professionOID) {
        return professionOIDStorage.get(professionOID);
    }

    public synchronized HolderInfo getHolderInfo(String holder) {
        return holderStorage.get(holder);
    }

    public synchronized List<HolderInfo> getAllHolderInfo() {
        List<HolderInfo> elems = new ArrayList<>(holderStorage.values());
        Collections.sort(elems, (o1, o2) -> o1.getCode().compareTo(o2.getCode()));
        return elems;
    }

    public synchronized SpecializationInfo getSpecializationInfo(String code) {
        if (code.startsWith("urn")) {
            String[] parts = code.split("\\:");
            StringBuilder newCode = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                if (!newCode.isEmpty()) {
                    newCode.append(":");
                }
                newCode.append(parts[i]);
            }
            code = newCode.toString();
        }
        return specializationStorage.get(code);
    }

    public synchronized List<SpecializationInfo> getAllSpecializationInfo() {
        List<SpecializationInfo> elems = new ArrayList<>(specializationStorage.values());
        Collections.sort(elems, (o1, o2) -> {
            if (o1.getUrl().equals(o2.getUrl())) {
                return o1.getCode().compareTo(o2.getCode());
            }
            return o1.getUrl().compareTo(o2.getUrl());
        });
        return elems;
    }

    public synchronized List<ProfessionOIDInfoReduced> getAllProfessionOIDInfo() {
        List<ProfessionOIDInfoReduced> elems = new ArrayList<>(professionOIDStorage.values());
        Collections.sort(elems, (o1, o2) -> o1.getCode().compareTo(o2.getCode()));
        return elems;
    }

    public synchronized ProfessionOIDInfo createProfessionOIDInfo(ProfessionOIDInfoReduced professionOIDInfoReduced) {
        ProfessionOIDInfo professionOIDInfo = new ProfessionOIDInfo();
        professionOIDInfo.setCode(professionOIDInfoReduced.getCode());
        professionOIDInfo.setPractitionier(professionOIDInfoReduced.isPractitionier());
        professionOIDInfo.setDisplay(professionOIDInfoReduced.getDisplay());
        professionOIDInfo.setOrganization(professionOIDInfoReduced.isOrganization());
        professionOIDInfo.setEntryType(professionOIDInfoReduced.getEntryType());
        professionOIDInfo.setEntryTypeText(professionOIDInfoReduced.getEntryTypeText());
        professionOIDInfo.setEntryTypeId(professionOIDInfoReduced.getEntryTypeId());
        professionOIDInfo.setTspAntragTyp(professionOIDInfoReduced.getTspAntragTyp());
        professionOIDInfo.setTelematikIdInfos(getTelematikIdInfos(professionOIDInfoReduced));
        return professionOIDInfo;
    }

    private synchronized List<TelematikIdInfo> getTelematikIdInfos(ProfessionOIDInfoReduced professionOIDInfoReduced) {
        List<TelematikIdInfo> res = new ArrayList<>();
        for (Iterator<String> iterator = telematikIdPatternStorage.keySet().iterator(); iterator.hasNext(); ) {
            String patternStr = iterator.next();
            TelematikIdPattern telematikIdPattern = telematikIdPatternStorage.get(patternStr);
            String[] ids = telematikIdPattern.getProfessionOIDs().split(",");
            for (int i = 0; i < ids.length; i++) {
                if (professionOIDInfoReduced.getCode().equals(ids[i])) {
                    TelematikIdInfo telematikIdInfo = new TelematikIdInfo();
                    telematikIdInfo.setTelematikIdPattern(telematikIdPattern);
                    ProfessionOIDInfoReduced professionOIDInfo = professionOIDStorage.get(ids[i]);
                    telematikIdInfo.getProfessionOIDInfos().add(professionOIDInfo);
                    res.add(telematikIdInfo);
                }
            }
        }
        return res;
    }

    public synchronized TelematikIdInfo getTelematikIdInfoByPattern(String telematikIdPatternStr) {
        TelematikIdPattern telematikIdPattern = telematikIdPatternStorage.get(telematikIdPatternStr);
        TelematikIdInfo telematikIdInfo = new TelematikIdInfo();
        telematikIdInfo.setTelematikIdPattern(telematikIdPattern);
        String[] ids = telematikIdPattern.getProfessionOIDs().split(",");
        for (int i = 0; i < ids.length; i++) {
            ProfessionOIDInfoReduced professionOIDInfo = professionOIDStorage.get(ids[i]);
            telematikIdInfo.getProfessionOIDInfos().add(professionOIDInfo);
        }
        return telematikIdInfo;
    }

    public synchronized TelematikIdInfo getTelematikIdInfo(String telematikId) {
        for (Iterator<String> iterator = telematikIdPatternStorage.keySet().iterator(); iterator.hasNext(); ) {
            String patternStr = iterator.next();
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(telematikId);
            if (matcher.find() && matcher.group().equals(telematikId)) {
                TelematikIdPattern telematikIdPattern = telematikIdPatternStorage.get(patternStr);
                TelematikIdInfo telematikIdInfo = new TelematikIdInfo();
                telematikIdInfo.setTelematikIdPattern(telematikIdPattern);
                telematikIdInfo.setTelematikId(telematikId);
                String[] ids = telematikIdPattern.getProfessionOIDs().split(",");
                for (int i = 0; i < ids.length; i++) {
                    ProfessionOIDInfoReduced professionOIDInfo = professionOIDStorage.get(ids[i]);
                    telematikIdInfo.getProfessionOIDInfos().add(professionOIDInfo);
                }
                return telematikIdInfo;
            }
        }
        return null;
    }

    public synchronized List<TelematikIdInfo> getAllTelematikIdInfo() {
        List<TelematikIdInfo> result = new ArrayList<>();
        for (Iterator<String> iterator = telematikIdPatternStorage.keySet().iterator(); iterator.hasNext(); ) {
            String patternStr = iterator.next();
            TelematikIdPattern telematikIdPattern = telematikIdPatternStorage.get(patternStr);
            TelematikIdInfo telematikIdInfo = new TelematikIdInfo();
            telematikIdInfo.setTelematikIdPattern(telematikIdPattern);
            telematikIdInfo.setTelematikId("");
            String[] ids = telematikIdPattern.getProfessionOIDs().split(",");
            for (int i = 0; i < ids.length; i++) {
                ProfessionOIDInfoReduced professionOIDInfo = professionOIDStorage.get(ids[i]);
                telematikIdInfo.getProfessionOIDInfos().add(professionOIDInfo);
            }
            result.add(telematikIdInfo);
        }
        Collections.sort(result, (o1, o2) -> o1.getTelematikIdPattern().getPattern().compareTo(o2.getTelematikIdPattern().getPattern()));
        return result;
    }
}
