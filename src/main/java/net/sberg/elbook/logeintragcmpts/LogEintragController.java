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
package net.sberg.elbook.logeintragcmpts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.common.AbstractWebController;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class LogEintragController extends AbstractWebController {

    private final JdbcGenericDao genericDao;

    @RequestMapping(value = "/logeintrag", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String logEintragView() {
        return "logeintrag/logeintrag";
    }

    @RequestMapping(value = "/logeintrag/uebersicht", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersicht(Model model, String searchValue) throws Exception {
        List<LogEintrag> entries = new ArrayList<>();
        if (!searchValue.equals("")) {
            entries = genericDao.selectMany(
                "select * from LogEintrag where (telematikId like ? or businessId like ?)",
                LogEintrag.class.getName(),
                null,
                Arrays.asList(
                        new DaoPlaceholderProperty("telematikId", "%"+searchValue+"%"),
                        new DaoPlaceholderProperty("businessId", "%"+searchValue+"%")
                )
            );
        }
        model.addAttribute("eintraege", entries);
        return "logeintrag/logeintragUebersicht";
    }

    @RequestMapping(value = "/logeintrag/lade/{logeintragId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String lade(Model model, @PathVariable int logeintragId) throws Exception {
        LogEintrag entry = (LogEintrag) genericDao.selectOne(
                LogEintrag.class.getName(),
                null,
                Arrays.asList(
                        new DaoPlaceholderProperty("id", logeintragId)
                )
        );

        List<LogEintragArtikel> artikel = genericDao.selectMany(
                LogEintragArtikel.class.getName(),
                null,
                Arrays.asList(
                        new DaoPlaceholderProperty("logEintragId", entry.getId())
                )
        );
        entry.setArtikel(artikel);

        model.addAttribute("eintrag", entry);
        return "logeintrag/logeintragDetails";
    }


}
