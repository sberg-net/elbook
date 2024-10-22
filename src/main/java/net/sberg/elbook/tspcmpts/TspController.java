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
package net.sberg.elbook.tspcmpts;

import de.gematik.ws.cm.pers.hba_smc_b.v1.AntragStatusKey;
import de.gematik.ws.cm.pers.hba_smc_b.v1.HbaAntragExport;
import de.gematik.ws.cm.pers.hba_smc_b.v1.SmcbAntragExport;
import de.gematik.ws.sst.v1.GetHbaAntraegeExportResponseType;
import de.gematik.ws.sst.v1.GetSmcbAntraegeExportResponseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.authcomponents.AuthService;
import net.sberg.elbook.authcomponents.AuthUserDetails;
import net.sberg.elbook.common.AbstractWebController;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.DaoProjectionBean;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TspController extends AbstractWebController {

    private final JdbcGenericDao genericDao;
    @Value("${elbook.encryptionKeys}")
    private String[] ENC_KEYS;

    @RequestMapping(value = "/tsp", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String tspView() {
        return "tsp/tsp";
    }

    @RequestMapping(value = "/tsp/lade/{tspId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String lade(Model model, @PathVariable int tspId) throws Exception {
        Tsp tsp = null;
        if (tspId == -1) {
            tsp = new Tsp();
        } else {
            tsp = (Tsp) genericDao.selectOne(Tsp.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", tspId)));
            tsp.decrypt(ENC_KEYS);
        }
        model.addAttribute("tsp", tsp);
        return "tsp/tspFormular";
    }

    @RequestMapping(value = "/tsp/loeschen/{tspId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String loeschen(@PathVariable int tspId) throws Exception {
        Tsp tsp = (Tsp) genericDao.selectOne(Tsp.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", tspId)));
        FileSystemUtils.deleteRecursively(new File(tsp.createKeystoreDirectory()));
        genericDao.delete(tsp, Optional.empty());
        return "ok";
    }

    @RequestMapping(value = "/tsp/uebersicht", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersicht(Authentication authentication, Model model) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        List<TspReduziert> tspen = genericDao.selectMany(
            "select id,tspName from Tsp where mandantId = ?",
            TspReduziert.class.getName(),
            null,
            Arrays.asList(new DaoPlaceholderProperty("mandantId", authUserDetails.getMandant().getId())));
        model.addAttribute("tspen", tspen);
        return "tsp/tspUebersicht";
    }

    @RequestMapping(value = "/tsp/check/{id}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public boolean check(@PathVariable int id) throws Exception {
        Tsp tsp = (Tsp)genericDao.selectOne(
            Tsp.class.getName(),
            null,
            Arrays.asList(new DaoPlaceholderProperty("id", id)));
        tsp.decrypt(ENC_KEYS);
        TspProperties tspProperties = tsp.create(EnumAntragTyp.SMCB);
        try {
            TspConnector tspConnector = new TspConnectorBuilder().build(tspProperties, tsp.getTspName().getQvda(), EnumAntragTyp.SMCB);
            tspConnector.getSmcbAntraegeZertifikateFreigeschaltet(true, 1, 0);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @RequestMapping(value = "/tsp/loadsmcb/{id}/{vorgangsNr}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public SmcbAntragExport loadRequest(@PathVariable int id, @PathVariable String vorgangsNr) throws Exception {
        Tsp tsp = (Tsp)genericDao.selectOne(
            Tsp.class.getName(),
            null,
            Arrays.asList(new DaoPlaceholderProperty("id", id)));
        tsp.decrypt(ENC_KEYS);
        TspProperties tspProperties = tsp.create(EnumAntragTyp.SMCB);
        try {
            TspConnector tspConnector = new TspConnectorBuilder().build(tspProperties, tsp.getTspName().getQvda(), EnumAntragTyp.SMCB);
            GetSmcbAntraegeExportResponseType getSmcbAntraegeExportResponseType = tspConnector.getSmcbAntrag(vorgangsNr);
            if (getSmcbAntraegeExportResponseType.getSmcbAntraegeExport().getSmcbAntragExport().isEmpty()) {
                throw new IllegalStateException("not available");
            }
            return getSmcbAntraegeExportResponseType.getSmcbAntraegeExport().getSmcbAntragExport().get(0);
        }
        catch (Exception e) {
            throw e;
        }
    }

    @RequestMapping(value = "/tsp/loadhba/{id}/{vorgangsNr}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public HbaAntragExport loadHbaRequest(@PathVariable int id, @PathVariable String vorgangsNr) throws Exception {
        Tsp tsp = (Tsp)genericDao.selectOne(
            Tsp.class.getName(),
            null,
            Arrays.asList(new DaoPlaceholderProperty("id", id)));
        tsp.decrypt(ENC_KEYS);
        TspProperties tspProperties = tsp.create(EnumAntragTyp.HBA);
        try {
            TspConnector tspConnector = new TspConnectorBuilder().build(tspProperties, tsp.getTspName().getQvda(), EnumAntragTyp.HBA);
            GetHbaAntraegeExportResponseType getHbaAntraegeExportResponseType = tspConnector.getHbaAntrag(vorgangsNr);
            if (getHbaAntraegeExportResponseType.getHbaAntraegeExport().getHbaAntragExport().isEmpty()) {
                throw new IllegalStateException("not available");
            }
            return getHbaAntraegeExportResponseType.getHbaAntraegeExport().getHbaAntragExport().get(0);
        }
        catch (Exception e) {
            throw e;
        }
    }

    @RequestMapping(value = "/tsp/loadallrequest/{id}/{antragTyp}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Map<String, String> loadAllRequest(@PathVariable int id, @PathVariable EnumAntragTyp antragTyp) throws Exception {
        Tsp tsp = (Tsp)genericDao.selectOne(
            Tsp.class.getName(),
            null,
            Arrays.asList(new DaoPlaceholderProperty("id", id)));
        tsp.decrypt(ENC_KEYS);
        TspProperties tspProperties = tsp.create(antragTyp);
        try {
            TspConnector tspConnector = new TspConnectorBuilder().build(tspProperties, tsp.getTspName().getQvda(), antragTyp);

            Map<String, String> result = new HashMap<>();
            int limit = 100;

            if (antragTyp.equals(EnumAntragTyp.HBA)) {
                int offset = 0;
                while (true) {
                    GetHbaAntraegeExportResponseType getHbaAntraegeExportResponseType = tspConnector.getHbaAntraegeZertifikateFreigeschaltet(true, limit, offset);

                    for (Iterator<HbaAntragExport> iterator = getHbaAntraegeExportResponseType.getHbaAntraegeExport().getHbaAntragExport().iterator(); iterator.hasNext(); ) {
                        HbaAntragExport hbaAntragExport = iterator.next();
                        result.put(hbaAntragExport.getVorgangsNr(), hbaAntragExport.getFreigabedaten().getTelematikID());
                    }

                    if (!getHbaAntraegeExportResponseType.isAntraegeExportWeitereTreffer()) {
                        break;
                    }

                    offset = offset + limit;
                }
            }
            else if (antragTyp.equals(EnumAntragTyp.SMCB)) {
                int offset = 0;
                while (true) {
                    GetSmcbAntraegeExportResponseType getSmcbAntraegeExportResponseType = tspConnector.getSmcbAntraegeZertifikateFreigeschaltet(true, limit, offset);

                    for (Iterator<SmcbAntragExport> iterator = getSmcbAntraegeExportResponseType.getSmcbAntraegeExport().getSmcbAntragExport().iterator(); iterator.hasNext(); ) {
                        SmcbAntragExport smcbAntragExport = iterator.next();
                        result.put(smcbAntragExport.getVorgangsNr(), smcbAntragExport.getInstitution().getTelematikID().getValue());
                    }

                    if (!getSmcbAntraegeExportResponseType.isAntraegeExportWeitereTreffer()) {
                        break;
                    }

                    offset = offset + limit;
                }
            }
            else {
                throw new IllegalStateException("unknown antrag typ: "+antragTyp);
            }

            return result;
        }
        catch (Exception e) {
            throw e;
        }
    }

    @RequestMapping(value = "/tsp/speichern", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String speichern(
            Authentication authentication,
            @RequestParam int id,
            @RequestParam(required = false) String tspName,
            @RequestParam String hbaUri,
            @RequestParam String smcbUri,
            @RequestParam String keystoreType,
            @RequestParam String keystorePass,
            @RequestParam(required = false) MultipartFile keystoreFile
        ) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        Tsp dbTsp;
        boolean insert = false;
        if (id == 0) {
            insert = true;
            dbTsp = new Tsp();
            dbTsp.setAngelegtAm(LocalDateTime.now());
        } else {
            dbTsp = (Tsp) genericDao.selectOne(Tsp.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", id)));
        }
        dbTsp.setGeaendertAm(LocalDateTime.now());
        dbTsp.setHbaUri(hbaUri);
        dbTsp.setSmcbUri(smcbUri);
        if (keystoreFile != null) {
            dbTsp.setKeystoreFile(keystoreFile.getOriginalFilename());
        }
        dbTsp.setKeystorePass(keystorePass);
        dbTsp.setKeystoreType(EnumKeystoreType.valueOf(keystoreType));
        if (tspName != null) {
            dbTsp.setTspName(EnumTspName.valueOf(tspName));
        }
        dbTsp.setMandantId(authUserDetails.getMandant().getId());
        dbTsp.encrypt(ENC_KEYS);

        List ids = genericDao.selectMany(
            "select id from Tsp where tspName = ? and mandantId = ?",
            null,
            new DaoProjectionBean(null, null, true),
            Arrays.asList(
                new DaoPlaceholderProperty("tspName", dbTsp.getTspName()),
                new DaoPlaceholderProperty("mandantId", dbTsp.getMandantId())
            )
        );

        for (Iterator iterator = ids.iterator(); iterator.hasNext(); ) {
            Integer dbId =  (Integer)iterator.next();
            if (dbId != dbTsp.getId()) {
                throw new IllegalStateException("Tsp ist schon vorhanden");
            }
        }

        if (insert) {
            genericDao.insert(dbTsp, Optional.empty());
            File dir = new File(dbTsp.createKeystoreDirectory());
            dir.mkdirs();
            keystoreFile.transferTo(new File(dir.getAbsolutePath() + File.separator + keystoreFile.getOriginalFilename()));
        }
        else {
            genericDao.update(dbTsp, Optional.empty());
        }

        return "ok";
    }
}
