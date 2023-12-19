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
package net.sberg.elbook.verzeichnisdienstcmpts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.common.AbstractWebController;
import net.sberg.elbook.vzdclientcmpts.ClientDetails;
import net.sberg.elbook.vzdclientcmpts.TiVZDConnector;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ClientConnectionController extends AbstractWebController {

    private final TiVZDConnector tiVZDConnector;

    @RequestMapping(value = "/clientconnection", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String clientConnectionView() {
        return "clientconnection/clientconnection";
    }

    @RequestMapping(value = "/clientconnection/uebersicht", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersicht(Model model, String searchValue) {
        try {
            List<ClientDetails> connections = tiVZDConnector.getClientDetails();
            if (searchValue != null && !searchValue.trim().isEmpty()) {
                connections = connections.stream().filter(s -> s.getId().contains(searchValue)).collect(Collectors.toList());
            }
            model.addAttribute("connections", connections);
        }
        catch (Exception e) {
            log.error("error on loading the overview", e);
            model.addAttribute("fehlernachricht", "Fehler beim Laden der Verzeichnisdienst-Connections. Bitte probieren Sie es erneut!");
            model.addAttribute("connections", new ArrayList<>());
        }
        return "clientconnection/clientconnectionUebersicht";
    }

    @RequestMapping(value = "/clientconnection/loeschen/{id}/{mutex}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public boolean loeschen(@PathVariable String id, @PathVariable Integer mutex) throws Exception {
        return tiVZDConnector.deleteConnection(id, mutex);
    }

}
