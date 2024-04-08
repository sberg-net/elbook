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
package net.sberg.elbook.kartendatentransfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.authcomponents.AuthUserDetails;
import net.sberg.elbook.common.AbstractWebController;
import net.sberg.elbook.common.FileUtils;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@Slf4j
@RequiredArgsConstructor
public class KartendatenTransferController extends AbstractWebController {

    private final JdbcGenericDao genericDao;
    private final KartendatenTransferService kartendatenTransferService;
    @Qualifier("hbakartendatenPdf")
    private final JasperReport hbakartendatenPdf;

    @RequestMapping(value = "/hbakartendatentransfer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String hbaKartenDatenTransferView() {
        return "hbakartendatentransfer/hbakartendatentransfer";
    }

    @SuppressWarnings("resource")
    @RequestMapping(value = "/hbakartendaten/report/{hashCode}/{aktivierungsCode}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void hbakartendatenReport(HttpServletResponse response, @PathVariable String aktivierungsCode, @PathVariable int hashCode) throws Exception {

        KartendatenTransfer kartendatenTransfer = (KartendatenTransfer) genericDao.selectOne(
                KartendatenTransfer.class.getName(),
                null,
                Arrays.asList(
                        new DaoPlaceholderProperty("aktivierungsCode", aktivierungsCode),
                        new DaoPlaceholderProperty("hashCode", hashCode)
                )
        );
        if (kartendatenTransfer.getGueltigBis().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Daten sind nicht mehr gültig");
        }

        File datentransferF = new File(ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_" + kartendatenTransfer.getMandantId() + "_" + kartendatenTransfer.getId() + ".json");
        HbaKartendatenTransferCommandContainer hbaKartendatenTransferCommandContainer = new ObjectMapper().readValue(datentransferF, HbaKartendatenTransferCommandContainer.class);
        JRDataSource dataSource = new JRBeanCollectionDataSource(hbaKartendatenTransferCommandContainer.getCommands());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("absender", hbaKartendatenTransferCommandContainer.getAbsender());
        params.put("empfaenger", hbaKartendatenTransferCommandContainer.getEmpfaenger());
        params.put("empfaengerKartenherausgeber", hbaKartendatenTransferCommandContainer.getEmpfaengerKartenherausgeber().getHrText());
        params.put("absenderKartenherausgeber", hbaKartendatenTransferCommandContainer.getAbsenderKartenherausgeber().getHrText());
        params.put("versendetAm", kartendatenTransfer.getVersendetAm().format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss")));
        params.put("gueltigBis", kartendatenTransfer.getGueltigBis().format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss")));

        String fileNameStr = "datentransfer"+ "_" + kartendatenTransfer.getId() + ".pdf";
        String longFileNameStr = System.getProperty("java.io.tmpdir") + File.separator + fileNameStr;

        JasperPrint jasperPrint = JasperFillManager.fillReport(hbakartendatenPdf, params, dataSource);
        File longFileName = new File(longFileNameStr);

        JasperExportManager.exportReportToPdfFile(jasperPrint, longFileName.getAbsolutePath());
        response.setHeader("Content-Disposition", "attachment; filename=" + fileNameStr);
        response.setContentType("application/pdf");

        InputStream inputStream = new FileInputStream(longFileName);
        inputStream.transferTo(response.getOutputStream());
        response.flushBuffer();

    }

    @RequestMapping(value = "/hbakartendaten/{hashCode}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String hbakartendaten(Model model, @PathVariable int hashCode) throws Exception {
        model.addAttribute("hashCode", hashCode);
        return "hbakartendaten/hbakartendaten";
    }

    @SuppressWarnings("resource")
    @RequestMapping(value = "/hbakartendaten/herunterladen/{hashCode}/{aktivierungsCode}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void hbakartendatenHerunterladen(HttpServletResponse response, @PathVariable String aktivierungsCode, @PathVariable int hashCode) throws Exception {
        KartendatenTransfer kartendatenTransfer = (KartendatenTransfer) genericDao.selectOne(
                KartendatenTransfer.class.getName(),
                null,
                Arrays.asList(
                        new DaoPlaceholderProperty("aktivierungsCode", aktivierungsCode),
                        new DaoPlaceholderProperty("hashCode", hashCode)
                )
        );
        if (kartendatenTransfer.getGueltigBis().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Daten sind nicht mehr gültig");
        }

        File f = new File(ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_" + kartendatenTransfer.getMandantId() + "_" + kartendatenTransfer.getId() + ".json");

        response.setHeader("Content-Disposition", "attachment; filename=" + f.getName());
        response.setContentType("application/json");

        InputStream inputStream = new FileInputStream(f);
        inputStream.transferTo(response.getOutputStream());
        response.flushBuffer();
    }

    @RequestMapping(value = "/hbakartendaten/{id}/lade/{personId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String hbakartendatenPersonLade(Model model, @PathVariable int id, @PathVariable int personId) throws Exception {
        KartendatenTransfer kartendatenTransfer = (KartendatenTransfer) genericDao.selectOne(KartendatenTransfer.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", id)));
        File f = new File(ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_"+kartendatenTransfer.getMandantId()+"_"+kartendatenTransfer.getId()+".json");
        HbaKartendatenTransferCommandContainer hbaKartendatenTransferCommandContainer = new ObjectMapper().readValue(f, HbaKartendatenTransferCommandContainer.class);
        model.addAttribute("person", personId > -1?hbaKartendatenTransferCommandContainer.getCommands().get(personId): new HbaKartendatenTransferCommand());
        model.addAttribute("datentransfer", kartendatenTransfer);
        model.addAttribute("personId", personId);
        return "hbakartendaten/hbakartendatenPersonFormular";
    }

    @RequestMapping(value = "/hbakartendaten/personenuebersicht", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String hbakartendatenPersonenUebersicht(Model model, @RequestBody Map data) throws Exception {
        KartendatenTransfer kartendatenTransfer = (KartendatenTransfer) genericDao.selectOne(KartendatenTransfer.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("hashCode", (String) data.get("hashCode"))));

        if (kartendatenTransfer == null || kartendatenTransfer.getGueltigBis().isBefore(LocalDateTime.now())) {
            model.addAttribute("ungueltig", true);
            return "hbakartendaten/hbakartendatenPersonenUebersichtAuthFormular";
        }

        if (!data.containsKey("aktivierungsCode")) {
            model.addAttribute("ungueltig", false);
            model.addAttribute("hashCode", data.get("hashCode"));
            return "hbakartendaten/hbakartendatenPersonenUebersichtAuthFormular";
        }
        else {

            String aktivierungsCode = (String)data.get("aktivierungsCode");
            if (!aktivierungsCode.equals(kartendatenTransfer.getAktivierungsCode())) {
                model.addAttribute("ungueltig", false);
                model.addAttribute("hashCode", data.get("hashCode"));
                model.addAttribute("fehlerNachricht", "Der Aktivierungscode ist falsch");
                return "hbakartendaten/hbakartendatenPersonenUebersichtAuthFormular";
            }

            File f = new File(ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_" + kartendatenTransfer.getMandantId() + "_" + kartendatenTransfer.getId() + ".json");
            HbaKartendatenTransferCommandContainer hbaKartendatenTransferCommandContainer = new ObjectMapper().readValue(f, HbaKartendatenTransferCommandContainer.class);

            List<HbaKartendatenTransferCommandReduziert> personen = new ArrayList<>();
            int idx = 0;
            for (Iterator<HbaKartendatenTransferCommand> iterator = hbaKartendatenTransferCommandContainer.getCommands().iterator(); iterator.hasNext(); ) {
                HbaKartendatenTransferCommand command = iterator.next();
                HbaKartendatenTransferCommandReduziert hbaKartendatenTransferCommandReduziert = new HbaKartendatenTransferCommandReduziert();
                hbaKartendatenTransferCommandReduziert.setPerson(command.getVorname() + " " + command.getNachname());
                hbaKartendatenTransferCommandReduziert.setId(idx);
                hbaKartendatenTransferCommandReduziert.setTyp(command.getTyp().getHrText());
                if (command.isUnbekanntVerzogen()) {
                    hbaKartendatenTransferCommandReduziert.setAnschrift("Unbekannt verzogen");
                }
                else {
                    hbaKartendatenTransferCommandReduziert.setAnschrift(command.getStrasse() + " " + command.getHausnummer() + ", " + command.getPostleitzahl() + " " + command.getWohnort());
                }
                personen.add(hbaKartendatenTransferCommandReduziert);
                idx++;
            }

            model.addAttribute("personen", personen);
            model.addAttribute("datentransfer", kartendatenTransfer);
            model.addAttribute("absenderKartenherausgeber", hbaKartendatenTransferCommandContainer.getAbsenderKartenherausgeber());

            if (kartendatenTransfer.getGelesenAm() == null) {
                kartendatenTransfer.setGelesenAm(LocalDateTime.now());
                genericDao.update(kartendatenTransfer, Optional.empty());
            }

            return "hbakartendaten/hbakartendatenPersonenUebersicht";
        }
    }

    @Operation(security = @SecurityRequirement(name = "basicAuth"), description = "Start des HBA-Datentransfers an den angegebenen Empfänger",
            responses = { @ApiResponse( responseCode = "200", description = "Ergebnis mit allen relevanten Informationen"  )})
    @RequestMapping(value = "/api/hbakartendaten/transferstart", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public HbaKartendatenTransferErgebnis apiTransferstart(Authentication authentication, @RequestBody HbaKartendatenTransferCommandContainer hbaKartendatenTransferCommandContainer) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();

        log.info("apiTransferstart execute 4: "+authUserDetails.getMandant().getId()+" - "+authUserDetails.getMandant().getName());

        KartendatenTransfer kartendatenTransfer = new KartendatenTransfer();
        kartendatenTransfer.setCount(hbaKartendatenTransferCommandContainer.getCommands().size());
        kartendatenTransfer.setAbsender(hbaKartendatenTransferCommandContainer.getAbsender());
        kartendatenTransfer.setEmpfaenger(hbaKartendatenTransferCommandContainer.getEmpfaenger());
        kartendatenTransfer.setMandantId(authUserDetails.getMandant().getId());
        kartendatenTransfer.setEmpfaengerKartenherausgeber(hbaKartendatenTransferCommandContainer.getEmpfaengerKartenherausgeber());
        kartendatenTransfer.setAbsenderKartenherausgeber(hbaKartendatenTransferCommandContainer.getAbsenderKartenherausgeber());
        kartendatenTransfer.setErstelltAm(LocalDateTime.now());
        kartendatenTransfer.setGueltigBis(kartendatenTransferService.createGueltigBis(kartendatenTransfer));
        kartendatenTransfer.setVersendetAm(LocalDateTime.now());

        if (hbaKartendatenTransferCommandContainer.getAktivierungsCode() == null || hbaKartendatenTransferCommandContainer.getAktivierungsCode().trim().isEmpty()) {
            kartendatenTransfer.setAktivierungsCode(kartendatenTransferService.createAktivierungsCode());
        }
        else {
            kartendatenTransfer.setAktivierungsCode(hbaKartendatenTransferCommandContainer.getAktivierungsCode());
        }

        kartendatenTransfer.setKartenTyp(EnumAntragTyp.HBA);

        kartendatenTransfer = (KartendatenTransfer) genericDao.insert(kartendatenTransfer, Optional.empty());
        kartendatenTransfer.setHashCode(kartendatenTransferService.createHashcode(kartendatenTransfer));

        hbaKartendatenTransferCommandContainer.setAktivierungsCode(kartendatenTransfer.getAktivierungsCode());
        hbaKartendatenTransferCommandContainer.setHashCode(kartendatenTransfer.getHashCode());
        hbaKartendatenTransferCommandContainer.setGueltigBis(kartendatenTransfer.getGueltigBis());
        hbaKartendatenTransferCommandContainer.setVersendetAm(kartendatenTransfer.getVersendetAm());

        genericDao.update(kartendatenTransfer, Optional.empty());

        FileUtils.writeToFile(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(hbaKartendatenTransferCommandContainer), ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_"+authUserDetails.getMandant().getId()+"_"+kartendatenTransfer.getId()+".json");

        HbaKartendatenTransferErgebnis hbaKartendatenTransferErgebnis = new HbaKartendatenTransferErgebnis();
        hbaKartendatenTransferErgebnis.setAbsender(kartendatenTransfer.getAbsender());
        hbaKartendatenTransferErgebnis.setEmpfaenger(kartendatenTransfer.getEmpfaenger());
        hbaKartendatenTransferErgebnis.setKartenTyp(kartendatenTransfer.getKartenTyp());
        hbaKartendatenTransferErgebnis.setAktivierungsCode(kartendatenTransfer.getAktivierungsCode());
        hbaKartendatenTransferErgebnis.setHashCode(kartendatenTransfer.getHashCode());
        hbaKartendatenTransferErgebnis.setEmpfaengerKartenherausgeber(kartendatenTransfer.getEmpfaengerKartenherausgeber());
        hbaKartendatenTransferErgebnis.setGueltigBis(kartendatenTransfer.getGueltigBis());
        hbaKartendatenTransferErgebnis.setErstelltAm(kartendatenTransfer.getErstelltAm());
        hbaKartendatenTransferErgebnis.setVersendetAm(kartendatenTransfer.getVersendetAm());
        hbaKartendatenTransferErgebnis.setUrl(kartendatenTransferService.getUrl(kartendatenTransfer));
        hbaKartendatenTransferErgebnis.setDownloadUrl(kartendatenTransferService.getDownloadUrl(kartendatenTransfer));

        kartendatenTransferService.createHBADatenTransferMail(hbaKartendatenTransferCommandContainer, kartendatenTransfer, authUserDetails.getMandant(), hbaKartendatenTransferCommandContainer.isSendMailWithActivationCode());

        return hbaKartendatenTransferErgebnis;
    }

    @RequestMapping(value = "/hbakartendatentransfer/senden/{id}/{sendMailWithActivationCode}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String hbakartendatentransferSenden(Authentication authentication, @PathVariable int id, @PathVariable boolean sendMailWithActivationCode) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        KartendatenTransfer kartendatenTransfer = (KartendatenTransfer) genericDao.selectOne(KartendatenTransfer.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", id)));
        kartendatenTransfer.setVersendetAm(LocalDateTime.now());
        genericDao.update(kartendatenTransfer, Optional.empty());

        File f = new File(ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_"+kartendatenTransfer.getMandantId()+"_"+kartendatenTransfer.getId()+".json");
        HbaKartendatenTransferCommandContainer hbaKartendatenTransferCommandContainer = new ObjectMapper().readValue(f, HbaKartendatenTransferCommandContainer.class);
        hbaKartendatenTransferCommandContainer.setSendMailWithActivationCode(sendMailWithActivationCode);
        hbaKartendatenTransferCommandContainer.setVersendetAm(kartendatenTransfer.getVersendetAm());
        FileUtils.writeToFile(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(hbaKartendatenTransferCommandContainer), f.getAbsolutePath());

        kartendatenTransferService.createHBADatenTransferMail(hbaKartendatenTransferCommandContainer, kartendatenTransfer, authUserDetails.getMandant(), sendMailWithActivationCode);
        return "ok";
    }

    @RequestMapping(value = "/hbakartendatentransfer/lade/{id}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String hbakartendatentransferLade(Model model, @PathVariable int id) throws Exception {
        KartendatenTransfer kartendatenTransfer = null;
        if (id == -1) {
            kartendatenTransfer = new KartendatenTransfer();
        } else {
            kartendatenTransfer = (KartendatenTransfer) genericDao.selectOne(KartendatenTransfer.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", id)));
        }
        model.addAttribute("kartendatenTransfer", kartendatenTransfer);
        model.addAttribute("url", kartendatenTransferService.getUrl(kartendatenTransfer));
        return "hbakartendatentransfer/hbakartendatentransferFormular";
    }

    @RequestMapping(value = "/hbakartendatentransfer/speichern", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String hbakartendatentransferSpeichern(Authentication authentication, @RequestBody KartendatenTransfer kartendatenTransfer) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        KartendatenTransfer dbKartendatenTransfer = null;
        boolean insert = false;
        if (kartendatenTransfer.getId() == 0) {
            insert = true;
            dbKartendatenTransfer = new KartendatenTransfer();
            dbKartendatenTransfer.setErstelltAm(LocalDateTime.now());
            dbKartendatenTransfer.setKartenTyp(EnumAntragTyp.HBA);
            dbKartendatenTransfer.setAktivierungsCode((kartendatenTransfer.getAktivierungsCode() == null || kartendatenTransfer.getAktivierungsCode().trim().isEmpty())?kartendatenTransferService.createAktivierungsCode():kartendatenTransfer.getAktivierungsCode());
            dbKartendatenTransfer.setMandantId(authUserDetails.getMandant().getId());
            dbKartendatenTransfer.setGueltigBis(kartendatenTransferService.createGueltigBis(dbKartendatenTransfer));
        } else {
            dbKartendatenTransfer = (KartendatenTransfer) genericDao.selectOne(KartendatenTransfer.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", kartendatenTransfer.getId())));
        }

        dbKartendatenTransfer.setEmpfaengerKartenherausgeber(kartendatenTransfer.getEmpfaengerKartenherausgeber());
        dbKartendatenTransfer.setAbsenderKartenherausgeber(kartendatenTransfer.getAbsenderKartenherausgeber());
        dbKartendatenTransfer.setEmpfaenger(kartendatenTransfer.getEmpfaenger());
        dbKartendatenTransfer.setAbsender(kartendatenTransfer.getAbsender());

        if (insert) {
            dbKartendatenTransfer = (KartendatenTransfer) genericDao.insert(dbKartendatenTransfer, Optional.empty());
            dbKartendatenTransfer.setHashCode(kartendatenTransferService.createHashcode(dbKartendatenTransfer));
            genericDao.update(dbKartendatenTransfer, Optional.empty());
        }
        else {
            genericDao.update(dbKartendatenTransfer, Optional.empty());
        }

        //write file
        File f = new File(ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_"+authUserDetails.getMandant().getId()+"_"+dbKartendatenTransfer.getId()+".json");
        HbaKartendatenTransferCommandContainer hbaKartendatenTransferCommandContainer = null;
        if (f.exists()) {
            hbaKartendatenTransferCommandContainer = new ObjectMapper().readValue(f, HbaKartendatenTransferCommandContainer.class);
        }
        else {
            hbaKartendatenTransferCommandContainer = new HbaKartendatenTransferCommandContainer();
        }
        hbaKartendatenTransferCommandContainer.setAbsender(dbKartendatenTransfer.getAbsender());
        hbaKartendatenTransferCommandContainer.setEmpfaenger(dbKartendatenTransfer.getEmpfaenger());
        hbaKartendatenTransferCommandContainer.setEmpfaengerKartenherausgeber(dbKartendatenTransfer.getEmpfaengerKartenherausgeber());
        hbaKartendatenTransferCommandContainer.setAbsenderKartenherausgeber(dbKartendatenTransfer.getAbsenderKartenherausgeber());
        hbaKartendatenTransferCommandContainer.setSektor(authUserDetails.getMandant().getSektor());
        hbaKartendatenTransferCommandContainer.setAktivierungsCode(dbKartendatenTransfer.getAktivierungsCode());
        hbaKartendatenTransferCommandContainer.setHashCode(dbKartendatenTransfer.getHashCode());
        hbaKartendatenTransferCommandContainer.setGueltigBis(dbKartendatenTransfer.getGueltigBis());

        FileUtils.writeToFile(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(hbaKartendatenTransferCommandContainer), f.getAbsolutePath());

        return "ok";
    }

    @RequestMapping(value = "/hbakartendatentransfer/uebersicht", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String hbakartendatentransferUebersicht(Authentication authentication, Model model, String searchValue) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        List<KartendatenTransferReduziert> daten = null;
        if (searchValue.equals("")) {
            daten = genericDao.selectMany(
                    "select id,mandantId,count,aktivierungsCode,empfaengerKartenherausgeber,erstelltAm,gelesenAm,versendetAm,gueltigBis,(gueltigBis < CURRENT_TIMESTAMP) abgelaufen from KartendatenTransfer where mandantId = ?",
                    KartendatenTransferReduziert.class.getName(),
                    null,
                    Arrays.asList(
                            new DaoPlaceholderProperty("mandantId", authUserDetails.getMandant().getId())
                    )
            );
        }
        else {
            daten = genericDao.selectMany(
                    "select id,mandantId,count,aktivierungsCode,empfaengerKartenherausgeber,erstelltAm,gelesenAm,versendetAm,gueltigBis,(gueltigBis < CURRENT_TIMESTAMP) abgelaufen from KartendatenTransfer where mandantId = ? and (aktivierungsCode like ? or hashCode like ?)",
                    KartendatenTransferReduziert.class.getName(),
                    null,
                    Arrays.asList(
                            new DaoPlaceholderProperty("mandantId", authUserDetails.getMandant().getId()),
                            new DaoPlaceholderProperty("aktivierungsCode", "%"+searchValue+"%"),
                            new DaoPlaceholderProperty("hashCode", "%"+searchValue+"%")
                    )
            );
        }

        //load persons
        for (Iterator<KartendatenTransferReduziert> iterator = daten.iterator(); iterator.hasNext(); ) {
            StringBuilder personBuilder = new StringBuilder();
            KartendatenTransferReduziert kartendatenTransferReduziert = iterator.next();
            File f = new File(ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_"+kartendatenTransferReduziert.getMandantId()+"_"+kartendatenTransferReduziert.getId()+".json");
            HbaKartendatenTransferCommandContainer hbaKartendatenTransferCommandContainer = new ObjectMapper().readValue(f, HbaKartendatenTransferCommandContainer.class);
            for (Iterator<HbaKartendatenTransferCommand> iterator2 = hbaKartendatenTransferCommandContainer.getCommands().iterator(); iterator2.hasNext(); ) {
                HbaKartendatenTransferCommand command = iterator2.next();
                if (personBuilder.length() > 0) {
                    personBuilder.append(", ");
                }
                personBuilder.append(command.getVorname()+" "+command.getNachname());
            }
            kartendatenTransferReduziert.setPersonen(personBuilder.toString());
        }

        model.addAttribute("datentransfer", daten);
        return "hbakartendatentransfer/hbakartendatentransferUebersicht";
    }

    @RequestMapping(value = "/hbakartendatentransfer/loeschen/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String hbakartendatentransferLoeschen(@PathVariable int id) throws Exception {
        KartendatenTransfer kartendatenTransfer = (KartendatenTransfer) genericDao.selectOne(KartendatenTransfer.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", id)));
        kartendatenTransferService.delete(kartendatenTransfer);
        return "ok";
    }

    @RequestMapping(value = "/hbakartendatentransfer/{id}/personenuebersicht", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String hbakartendatentransferPersonenUebersicht(Model model, @PathVariable int id) throws Exception {
        KartendatenTransfer kartendatenTransfer = (KartendatenTransfer) genericDao.selectOne(KartendatenTransfer.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", id)));

        File f = new File(ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_"+kartendatenTransfer.getMandantId()+"_"+kartendatenTransfer.getId()+".json");
        HbaKartendatenTransferCommandContainer hbaKartendatenTransferCommandContainer = new ObjectMapper().readValue(f, HbaKartendatenTransferCommandContainer.class);

        List<HbaKartendatenTransferCommandReduziert> personen = new ArrayList<>();
        int idx = 0;
        for (Iterator<HbaKartendatenTransferCommand> iterator = hbaKartendatenTransferCommandContainer.getCommands().iterator(); iterator.hasNext(); ) {
            HbaKartendatenTransferCommand command =  iterator.next();
            HbaKartendatenTransferCommandReduziert hbaKartendatenTransferCommandReduziert = new HbaKartendatenTransferCommandReduziert();
            hbaKartendatenTransferCommandReduziert.setPerson(command.getVorname()+" "+command.getNachname());
            hbaKartendatenTransferCommandReduziert.setId(idx);
            hbaKartendatenTransferCommandReduziert.setTyp(command.getTyp().getHrText());
            if (command.isUnbekanntVerzogen()) {
                hbaKartendatenTransferCommandReduziert.setAnschrift("Unbekannt verzogen");
            }
            else {
                hbaKartendatenTransferCommandReduziert.setAnschrift(command.getStrasse() + " " + command.getHausnummer() + ", " + command.getPostleitzahl() + " " + command.getWohnort());
            }
            personen.add(hbaKartendatenTransferCommandReduziert);
            idx++;
        }

        model.addAttribute("personen", personen);
        model.addAttribute("personenAnlegen", !personen.isEmpty());
        model.addAttribute("datentransfer", kartendatenTransfer);

        return "hbakartendatentransfer/hbakartendatentransferPersonenUebersicht";
    }

    @RequestMapping(value = "/hbakartendatentransfer/{id}/lade/{personId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String hbakartendatentransferPersonLade(Model model, @PathVariable int id, @PathVariable int personId) throws Exception {
        KartendatenTransfer kartendatenTransfer = (KartendatenTransfer) genericDao.selectOne(KartendatenTransfer.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", id)));
        File f = new File(ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_"+kartendatenTransfer.getMandantId()+"_"+kartendatenTransfer.getId()+".json");
        HbaKartendatenTransferCommandContainer hbaKartendatenTransferCommandContainer = new ObjectMapper().readValue(f, HbaKartendatenTransferCommandContainer.class);
        model.addAttribute("person", personId > -1?hbaKartendatenTransferCommandContainer.getCommands().get(personId): new HbaKartendatenTransferCommand());
        model.addAttribute("datentransfer", kartendatenTransfer);
        model.addAttribute("personId", personId);
        return "hbakartendatentransfer/hbakartendatentransferPersonFormular";
    }

    @RequestMapping(value = "/hbakartendatentransfer/{id}/loeschen/{personId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String hbakartendatentransferPersonLoeschen(@PathVariable int id, @PathVariable int personId) throws Exception {
        KartendatenTransfer kartendatenTransfer = (KartendatenTransfer) genericDao.selectOne(KartendatenTransfer.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", id)));
        File f = new File(ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_"+kartendatenTransfer.getMandantId()+"_"+kartendatenTransfer.getId()+".json");
        HbaKartendatenTransferCommandContainer hbaKartendatenTransferCommandContainer = new ObjectMapper().readValue(f, HbaKartendatenTransferCommandContainer.class);
        hbaKartendatenTransferCommandContainer.getCommands().remove(personId);
        FileUtils.writeToFile(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(hbaKartendatenTransferCommandContainer), f.getAbsolutePath());

        kartendatenTransfer.setCount(hbaKartendatenTransferCommandContainer.getCommands().size());
        genericDao.update(kartendatenTransfer, Optional.empty());

        return "ok";
    }

    @RequestMapping(value = "/hbakartendatentransfer/{id}/speichern/{personId}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String hbakartendatentransferPersonSpeichern(@PathVariable int id, @PathVariable int personId, @RequestBody HbaKartendatenTransferCommand hbaKartendatenTransferCommand) throws Exception {
        KartendatenTransfer kartendatenTransfer = (KartendatenTransfer) genericDao.selectOne(KartendatenTransfer.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", id)));
        File f = new File(ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_"+kartendatenTransfer.getMandantId()+"_"+kartendatenTransfer.getId()+".json");
        HbaKartendatenTransferCommandContainer hbaKartendatenTransferCommandContainer = new ObjectMapper().readValue(f, HbaKartendatenTransferCommandContainer.class);
        if (personId > -1) {
            hbaKartendatenTransferCommandContainer.getCommands().remove(personId);
            hbaKartendatenTransferCommandContainer.getCommands().add(personId, hbaKartendatenTransferCommand);
        }
        else {
            hbaKartendatenTransferCommandContainer.getCommands().add(hbaKartendatenTransferCommand);
            kartendatenTransfer.setCount(hbaKartendatenTransferCommandContainer.getCommands().size());
            genericDao.update(kartendatenTransfer, Optional.empty());
        }
        FileUtils.writeToFile(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(hbaKartendatenTransferCommandContainer), f.getAbsolutePath());
        return "ok";
    }
}
