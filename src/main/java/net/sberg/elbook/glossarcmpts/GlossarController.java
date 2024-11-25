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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.authcomponents.AuthUserDetails;
import net.sberg.elbook.batchjobcmpts.BatchJob;
import net.sberg.elbook.common.AbstractWebController;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.common.StringUtils;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.DaoProjectionBean;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.logeintragcmpts.LogEintrag;
import net.sberg.elbook.logeintragcmpts.LogEintragArtikel;
import net.sberg.elbook.mandantcmpts.*;
import net.sberg.elbook.reportcmpts.Report;
import net.sberg.elbook.stammdatenzertimportcmpts.VerzeichnisdienstImportCommandContainer;
import net.sberg.elbook.stammdatenzertimportcmpts.VerzeichnisdienstImportErgebnis;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GlossarController extends AbstractWebController {

    private final GlossarService glossarService;

    @RequestMapping(value = "/glossar", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String glossarView() {
        return "glossar/glossar";
    }

    @RequestMapping(value = "/glossar/uebersicht", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersicht(Model model, String telematikId) throws Exception {
        if (telematikId.equals("")) {
            model.addAttribute("telematikIdInfo", null);
            model.addAttribute("verfuegbar", false);
            model.addAttribute("suche", false);
        }
        else {
            TelematikIdInfo telematikIdInfo = glossarService.get(telematikId);
            model.addAttribute("telematikIdInfo", telematikIdInfo);
            model.addAttribute("verfuegbar", telematikIdInfo != null);
            model.addAttribute("suche", true);
        }
        return "glossar/glossarUebersicht";
    }

    @Operation(description = "Anhand der übergebenen TelematikId werden die Informationen zurückgegeben.",
            responses = { @ApiResponse( responseCode = "200", description = "Anhand der übergebenen TelematikId werden die Informationen zurückgegeben."  )})
    @RequestMapping(value = "/api/glossar", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Map<String, TelematikIdInfo> api(@RequestBody List<String> telematikIds) throws Exception {
        Map<String, TelematikIdInfo> res = new HashMap<>();
        for (Iterator<String> iterator = telematikIds.iterator(); iterator.hasNext(); ) {
            String telematikId = iterator.next();
            res.put(telematikId, glossarService.get(telematikId));
        }
        return res;
    }
}
