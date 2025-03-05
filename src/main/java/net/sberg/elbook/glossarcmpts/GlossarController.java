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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.common.AbstractWebController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GlossarController extends AbstractWebController {

    private static final String SEARCHTYPE_TID = "telematikId";
    private static final String SEARCHTYPE_PID = "professionOID";
    private static final String SEARCHTYPE_HOLDER = "holder";

    private final GlossarService glossarService;

    @RequestMapping(value = "/glossar", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String glossarView() {
        return "glossar/glossar";
    }

    @RequestMapping(value = "/glossar/uebersicht/professionoid/**", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersichtProfessionOID(Model model, HttpServletRequest request) throws Exception {
        String professionOids = request.getRequestURI().split(request.getContextPath() + "/glossar/uebersicht/professionoid/")[1];
        model.addAttribute("searchType", SEARCHTYPE_PID);
        model.addAttribute("searchValue", professionOids);
        List<ProfessionOIDInfo> res = new ArrayList<>();

        for (int i = 0; i < professionOids.split(",").length; i++) {
            ProfessionOIDInfo professionOIDInfo = glossarService.createProfessionOIDInfo(glossarService.getProfessionOIDInfo(professionOids.split(",")[i]));
            res.add(professionOIDInfo);
        }
        model.addAttribute("professionOIDInfos", res);
        return "glossar/glossar";
    }

    @RequestMapping(value = "/glossar/uebersicht/telematikidinfo/**", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersichtTelematikIDInfo(Model model, HttpServletRequest request) throws Exception {
        String telematikIds = request.getRequestURI().split(request.getContextPath() + "/glossar/uebersicht/telematikidinfo/")[1];
        model.addAttribute("searchType", SEARCHTYPE_TID);
        model.addAttribute("searchValue", telematikIds);
        return "glossar/glossar";
    }

    @RequestMapping(value = "/glossar/uebersicht/holderinfo/**", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersichtHolderInfo(Model model, HttpServletRequest request) throws Exception {
        String holder = request.getRequestURI().split(request.getContextPath() + "/glossar/uebersicht/holderinfo/")[1];
        model.addAttribute("searchType", SEARCHTYPE_HOLDER);
        model.addAttribute("searchValue", holder);
        return "glossar/glossar";
    }

    @RequestMapping(value = "/glossar/uebersicht", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersicht(Model model, String searchValue, String searchType) throws Exception {
        if (searchType.equals(SEARCHTYPE_TID)) {
            if (searchValue.equals(GlossarService.SEARCHVALUE_ALL_ELEMS)) {
                model.addAttribute("telematikIdInfos", glossarService.getAllTelematikIdInfo());
                model.addAttribute("telematikIdInfoVerfuegbar", true);
            }
            else {
                List<TelematikIdInfo> res = new ArrayList<>();
                String[] elems = searchValue.split(",");
                for (int i = 0; i < elems.length; i++) {
                    TelematikIdInfo telematikIdInfo = glossarService.getTelematikIdInfo(elems[i].trim());
                    if (telematikIdInfo != null) {
                        res.add(telematikIdInfo);
                    }
                }
                model.addAttribute("telematikIdInfos", res);
                model.addAttribute("telematikIdInfoVerfuegbar", !res.isEmpty());
            }
        }
        else if (searchType.equals(SEARCHTYPE_PID)) {
            model.addAttribute("telematikIdInfoVerfuegbar", false);
            if (searchValue.equals("")) {
                model.addAttribute("professionOIDInfos", new ArrayList<>());
            }
            else if (searchValue.equals(GlossarService.SEARCHVALUE_ALL_ELEMS)) {
                List<ProfessionOIDInfo> professionOIDInfos = glossarService.getAllProfessionOIDInfo().stream().map(professionOIDInfoReduced -> glossarService.createProfessionOIDInfo(professionOIDInfoReduced)).collect(Collectors.toList());
                model.addAttribute("professionOIDInfos", professionOIDInfos);
            }
            else {
                List<ProfessionOIDInfo> res = new ArrayList<>();
                String[] elems = searchValue.split(",");
                for (int i = 0; i < elems.length; i++) {
                    ProfessionOIDInfoReduced professionOIDInfoReduced = glossarService.getProfessionOIDInfo(elems[i].trim());
                    if (professionOIDInfoReduced != null) {
                        res.add(glossarService.createProfessionOIDInfo(professionOIDInfoReduced));
                    }
                }
                model.addAttribute("professionOIDInfos", res);
            }
        }
        else if (searchType.equals(SEARCHTYPE_HOLDER)) {
            model.addAttribute("telematikIdInfoVerfuegbar", false);
            if (searchValue.equals("")) {
                model.addAttribute("holderInfos", new ArrayList<>());
            }
            else if (searchValue.equals(GlossarService.SEARCHVALUE_ALL_ELEMS)) {
                List<HolderInfo> holderInfos = glossarService.getAllHolderInfo();
                model.addAttribute("holderInfos", holderInfos);
            }
            else {
                List<HolderInfo> res = new ArrayList<>();
                String[] elems = searchValue.split(",");
                for (int i = 0; i < elems.length; i++) {
                    HolderInfo holderInfo = glossarService.getHolderInfo(elems[i].trim());
                    if (holderInfo != null) {
                        res.add(holderInfo);
                    }
                }
                model.addAttribute("holderInfos", res);
            }
        }
        else {
            model.addAttribute("telematikIdInfoVerfuegbar", false);
            model.addAttribute("holderInfos", new ArrayList<>());
            model.addAttribute("professionOIDInfos", new ArrayList<>());
        }

        return "glossar/glossarUebersicht";
    }

    @Operation(description = "Anhand der übergebenen TelematikId werden die Informationen zurückgegeben.",
            responses = { @ApiResponse( responseCode = "200", description = "Anhand der übergebenen TelematikId werden die Informationen zurückgegeben."  )})
    @RequestMapping(value = "/api/glossar/telematikidinfo", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Map<String, TelematikIdInfo> apiGlossarTelematikIdIndo(@RequestBody List<String> telematikIds) throws Exception {
        Map<String, TelematikIdInfo> res = new HashMap<>();
        for (Iterator<String> iterator = telematikIds.iterator(); iterator.hasNext(); ) {
            String telematikId = iterator.next();
            res.put(telematikId, glossarService.getTelematikIdInfo(telematikId));
        }
        return res;
    }

    @Operation(description = "Anhand der übergebenen ProfessionOID werden die Informationen zurückgegeben. In dem übergebenen Array können Sie auch nur ein Element vom Wert '*' angeben. Dann bekommen Sie alle verfügbaren Werte.",
            responses = { @ApiResponse( responseCode = "200", description = "Anhand der übergebenen ProfessionOID werden die Informationen zurückgegeben."  )})
    @RequestMapping(value = "/api/glossar/professionoidinfo", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Map<String, ProfessionOIDInfo> apiGlossarProfessionOIDInfo(@RequestBody List<String> professionOIDs) throws Exception {
        Map<String, ProfessionOIDInfo> res = new HashMap<>();

        if (professionOIDs.size() == 1 && professionOIDs.get(0).equals(GlossarService.SEARCHVALUE_ALL_ELEMS)) {
            glossarService.getAllProfessionOIDInfo().forEach(professionOIDInfo -> res.put(professionOIDInfo.getCode(), glossarService.createProfessionOIDInfo(professionOIDInfo)));
        }
        else {
            for (Iterator<String> iterator = professionOIDs.iterator(); iterator.hasNext(); ) {
                String professionOID = iterator.next();
                res.put(professionOID, glossarService.createProfessionOIDInfo(glossarService.getProfessionOIDInfo(professionOID)));
            }
        }
        return res;
    }

    @Operation(description = "Anhand des übergebenen Holder werden die Informationen zurückgegeben. In dem übergebenen Array können Sie auch nur ein Element vom Wert '*' angeben. Dann bekommen Sie alle verfügbaren Werte.",
            responses = { @ApiResponse( responseCode = "200", description = "Anhand des übergebenen Holder werden die Informationen zurückgegeben."  )})
    @RequestMapping(value = "/api/glossar/holderinfo", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Map<String, HolderInfo> apiGlossarHolderInfo(@RequestBody List<String> holder) throws Exception {
        Map<String, HolderInfo> res = new HashMap<>();

        if (holder.size() == 1 && holder.get(0).equals(GlossarService.SEARCHVALUE_ALL_ELEMS)) {
            glossarService.getAllHolderInfo().forEach(holderInfo -> res.put(holderInfo.getCode(), holderInfo));
        }
        else {
            for (Iterator<String> iterator = holder.iterator(); iterator.hasNext(); ) {
                String holderV = iterator.next();
                res.put(holderV, glossarService.getHolderInfo(holderV));
            }
        }
        return res;
    }
}
