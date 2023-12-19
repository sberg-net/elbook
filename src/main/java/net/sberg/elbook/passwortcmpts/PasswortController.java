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
package net.sberg.elbook.passwortcmpts;

import lombok.RequiredArgsConstructor;
import net.sberg.elbook.authcomponents.AuthUserDetails;
import net.sberg.elbook.common.AbstractWebController;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.mandantcmpts.Mandant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PasswortController extends AbstractWebController {

    private final JdbcGenericDao genericDao;
    private final PasswordEncoder passwordEncoder;

    @Value("${elbook.encryptionKeys}")
    private String[] ENC_KEYS;

    @RequestMapping(value = "/passwort/aendern", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String aendern(Authentication authentication, @RequestBody PasswortAendern passwortAendern) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        if (!passwordEncoder.matches(passwortAendern.getAltesPasswort(), authUserDetails.getMandant().getPasswort())) {
            throw new IllegalStateException("Das alte Passwort ist nicht korrekt!");
        }
        if (passwortAendern.getAltesPasswort().equals(passwortAendern.getNeuesPasswort())) {
            throw new IllegalStateException("Das alte Passwort stimmt mit dem neuen Passwort überein!");
        }

        Mandant dbMandant = (Mandant) genericDao.selectOne(Mandant.class.getName(), null,
                List.of(new DaoPlaceholderProperty("id", authUserDetails.getMandant().getId())));
        dbMandant.decrypt(ENC_KEYS);

        dbMandant.setPasswort(passwordEncoder.encode(passwortAendern.getNeuesPasswort()));
        dbMandant.encrypt(ENC_KEYS);
        genericDao.update(dbMandant, Optional.empty());

        return "ok";
    }

    @RequestMapping(value = "/passwort/zuruecksetzen/{mandantId}/{nutzername}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String ladenZuruecksetzen(Model model, @PathVariable int mandantId, @PathVariable String nutzername){
        model.addAttribute("id", mandantId);
        model.addAttribute("nutzername", nutzername);
        return "passwort/zuruecksetzen";
    }

    @RequestMapping(value = "/passwort/zuruecksetzen", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String zuruecksetzen(@RequestBody PasswortZuruecksetzen passwortZuruecksetzen) throws Exception {
        Mandant dbMandant = (Mandant) genericDao.selectOne(
                Mandant.class.getName(),
                null,
                Arrays.asList(
                        new DaoPlaceholderProperty("id", passwortZuruecksetzen.getMandantId()),
                        new DaoPlaceholderProperty("nutzername", passwortZuruecksetzen.getNutzername())
                )
        );
        dbMandant.decrypt(ENC_KEYS);

        dbMandant.setPasswort(passwordEncoder.encode(passwortZuruecksetzen.getNeuesPasswort()));
        dbMandant.encrypt(ENC_KEYS);
        genericDao.update(dbMandant, Optional.empty());

        return "ok";
    }
}
