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
    private final Map<String, ProfessionOIDInfo> professionOIDStorage = new Hashtable<>();
    private final Map<String, TelematikIdPattern> telematikIdPatternStorage = new Hashtable<>();

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
            telematikIdPatternStorage.put(telematikIdPattern.getPattern(), telematikIdPattern);
        });

        //read CodeSystem-OrganizationProfessionOID.json
        r = new ObjectMapper().readValue(getClass().getResourceAsStream("/glossar/CodeSystem-OrganizationProfessionOID.json"), Map.class);
        l = (List) r.get("concept");
        l.forEach(o -> {
            ProfessionOIDInfo professionOIDInfo = new ProfessionOIDInfo();
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
            ProfessionOIDInfo professionOIDInfo = new ProfessionOIDInfo();
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
    }

    public synchronized boolean validHolder(String holder) {
        return holderStorage.containsKey(holder);
    }

    public synchronized ProfessionOIDInfo getProfessionOIDInfo(String professionOID) {
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

    public synchronized List<ProfessionOIDInfo> getAllProfessionOIDInfo() {
        List<ProfessionOIDInfo> elems = new ArrayList<>(professionOIDStorage.values());
        Collections.sort(elems, (o1, o2) -> o1.getCode().compareTo(o2.getCode()));
        return elems;
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
                    ProfessionOIDInfo professionOIDInfo = professionOIDStorage.get(ids[i]);
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
                ProfessionOIDInfo professionOIDInfo = professionOIDStorage.get(ids[i]);
                telematikIdInfo.getProfessionOIDInfos().add(professionOIDInfo);
            }
            result.add(telematikIdInfo);
        }
        Collections.sort(result, (o1, o2) -> o1.getTelematikIdPattern().getPattern().compareTo(o2.getTelematikIdPattern().getPattern()));
        return result;
    }
}
