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
package net.sberg.elbook.holderattrcmpts;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.authcomponents.AuthUserDetails;
import net.sberg.elbook.common.AbstractWebController;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HolderAttrController extends AbstractWebController {

    private final HolderAttrService holderAttrService;

    @RequestMapping(value = "/holderattribut", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String holderattributView() {
        return "holderattribut/holderattribut";
    }

    @RequestMapping(value = "/holderattribut/jsondatei", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<HolderAttrErgebnis> jsondatei(Authentication authentication, @RequestParam(name = "holderAttrFile", required = false) MultipartFile holderAttrFile
    ) throws Exception {
        HolderAttrCommandContainer holderAttrCommandContainer = new ObjectMapper().readValue(holderAttrFile.getInputStream(), HolderAttrCommandContainer.class);
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        return holderAttrService.execute(authUserDetails.getMandant(), holderAttrCommandContainer);
    }

    @Operation(security = @SecurityRequirement(name = "basicAuth"), description = "Aktualisieren des Holder-Attributs mit Einträgen.",
            responses = { @ApiResponse( responseCode = "200", description = "Der Request wird synchron gestartet. Sie erhalten eine Liste mit den Ergebnisobjekten zurück."  )})
    @RequestMapping(value = "/api/holderattr/starten/sync", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<HolderAttrErgebnis> apiHolderattrStartenSync(Authentication authentication, @RequestBody HolderAttrCommandContainer holderAttrCommandContainer) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        log.info("apiHolderattrStartenSync execute 4: "+authUserDetails.getMandant().getId()+" - "+authUserDetails.getMandant().getName());
        return holderAttrService.execute(authUserDetails.getMandant(), holderAttrCommandContainer);
    }
}
