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
package net.sberg.elbook.stammdatenzertimportcmpts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.authcomponents.AuthUserDetails;
import net.sberg.elbook.batchjobcmpts.BatchJob;
import net.sberg.elbook.batchjobcmpts.BatchJobReduziert;
import net.sberg.elbook.batchjobcmpts.EnumBatchJobName;
import net.sberg.elbook.common.AbstractWebController;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StammdatenZertImportController extends AbstractWebController {

    private final JdbcGenericDao genericDao;
    private final StammdatenZertImportService stammdatenZertImportService;

    @RequestMapping(value = "/stammdatencertimport", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String stammDatenCertImportView() {
        return "stammdatencertimport/stammdatencertimport";
    }

    @RequestMapping(value = "/stammdatencertimport/uebersicht", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersicht(Authentication authentication, Model model) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        if (!authUserDetails.isAdmin()) {
            List<BatchJobReduziert> jobs = genericDao.selectMany(
                    "SELECT \n" +
                            "    b.id,\n" +
                            "    b.mandantId,\n" +
                            "    m.name as mandantName,\n" +
                            "    b.batchJobName,\n" +
                            "    b.statusCode,\n" +
                            "    b.anzahlDatensaetze,\n" +
                            "    b.anzahlDatensaetzeAbgearbeitet,\n" +
                            "    b.gestartetAm,\n" +
                            "    b.beendetAm\n" +
                            "FROM BatchJob b inner join Mandant m on b.mandantId = m.id where m.id = ? and b.batchJobName = ? order by b.gestartetAm desc",
                    BatchJobReduziert.class.getName(),
                    null,
                    Arrays.asList(
                            new DaoPlaceholderProperty("mandantId", authUserDetails.getMandant().getId()),
                            new DaoPlaceholderProperty("batchJobName", EnumBatchJobName.STAMMDATEN_CERT_IMPORT.name())
                    )
            );

            model.addAttribute("jobs", jobs);
            model.addAttribute("admin", false);
        }
        else {
            List<BatchJobReduziert> jobs = genericDao.selectMany(
                    "SELECT \n" +
                            "    b.id,\n" +
                            "    b.mandantId,\n" +
                            "    m.name as mandantName,\n" +
                            "    b.batchJobName,\n" +
                            "    b.statusCode,\n" +
                            "    b.anzahlDatensaetze,\n" +
                            "    b.anzahlDatensaetzeAbgearbeitet,\n" +
                            "    b.gestartetAm,\n" +
                            "    b.beendetAm\n" +
                            "FROM BatchJob b inner join Mandant m on b.mandantId = m.id where b.batchJobName = ? order by b.gestartetAm desc",
                    BatchJobReduziert.class.getName(),
                    null,
                    Arrays.asList(
                            new DaoPlaceholderProperty("batchJobName", EnumBatchJobName.STAMMDATEN_CERT_IMPORT.name())
                    )
            );
            model.addAttribute("jobs", jobs);
            model.addAttribute("admin", true);
        }

        return "stammdatencertimport/stammdatencertimportUebersicht";

    }

    @SuppressWarnings("resource")
    @RequestMapping(value = "/stammdatencertimport/herunterladen/ergebnisse/{jobId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void herunterladenErgebnisse(HttpServletResponse response, Authentication authentication, @PathVariable int jobId) throws Exception {
        BatchJob batchJob = (BatchJob) genericDao.selectOne(BatchJob.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", jobId)));

        AuthUserDetails authUserDetails = (AuthUserDetails)authentication.getPrincipal();
        if (!authUserDetails.isAdmin() && authUserDetails.getMandant().getId() != batchJob.getMandantId()) {
            log.error("batchjob stammdatenimport hacking attack: batchjob mandant - "+batchJob.getMandantId()+" - logged in mandant: "+((AuthUserDetails) authentication.getPrincipal()).getMandant().getId());
            throw new IllegalStateException("Ergebnisse sind nicht verfügbar");
        }

        String fileName = ICommonConstants.BASE_DIR + "jobs" + File.separator + "ergebnisse_"+batchJob.getMandantId()+"_"+batchJob.getId()+".json";

        if (!new File(fileName).exists()) {
            log.error("batchjob stammdatenimport file not found: "+fileName);
            throw new IllegalStateException("Ergebnisse sind nicht verfügbar");
        }

        response.setHeader("Content-Disposition", "attachment; filename=" + new File(fileName).getName());
        response.setContentType("application/json");

        InputStream inputStream = new FileInputStream(fileName);
        inputStream.transferTo(response.getOutputStream());
        response.flushBuffer();
    }

    @RequestMapping(value = "/stammdatencertimport/zusammenfassen/ergebnisse/{jobId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public VerzeichnisdienstImportErgebnisZusammenfassung zusammenfassenErgebnisse(@PathVariable int jobId) throws Exception {
        BatchJob batchJob = (BatchJob) genericDao.selectOne(BatchJob.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", jobId)));

        String fileName = ICommonConstants.BASE_DIR + "jobs" + File.separator + "ergebnisse_"+batchJob.getMandantId()+"_"+batchJob.getId()+".json";

        List<VerzeichnisdienstImportErgebnis> ergebnisse = new ObjectMapper().readValue(new File(fileName), new TypeReference<List<VerzeichnisdienstImportErgebnis>>(){});

        VerzeichnisdienstImportErgebnisZusammenfassung zusammenfassung = new VerzeichnisdienstImportErgebnisZusammenfassung();

        for (Iterator<VerzeichnisdienstImportErgebnis> iterator = ergebnisse.iterator(); iterator.hasNext(); ) {
            VerzeichnisdienstImportErgebnis ergebnis =  iterator.next();
            zusammenfassung.setCertUpdateCount(zusammenfassung.getCertUpdateCount() + (ergebnis.isCertUpdate()?1:0));
            zusammenfassung.setDeleteCount(zusammenfassung.getDeleteCount() + (ergebnis.isDelete()?1:0));
            zusammenfassung.setErrorCount(zusammenfassung.getErrorCount() + (ergebnis.isError()?1:0));
            zusammenfassung.setInsertCount(zusammenfassung.getInsertCount() + (ergebnis.isInsert()?1:0));
            zusammenfassung.setUpdateCount(zusammenfassung.getUpdateCount() + (ergebnis.isUpdate()?1:0));
        }

        return zusammenfassung;
    }

    @Operation(security = @SecurityRequirement(name = "basicAuth"), description = "Importieren der Stammdaten derart, dass die notwendigen Informationen in den Verzeichnisdienst übertragen werden. Dabei wird zwischen Einfüge- , Änderungs- und Löschoperationen unterschieden. Der Request wird synchron gestartet. Dabei wird ein Batchjob gestartet und gespeichert. Sie werden per Mail informiert, wenn der Batchjob fertig ist.",
            responses = { @ApiResponse( responseCode = "200", description = "Der Request wird synchron gestartet. Sie erhalten eine Liste mit den Ergebnisobjekten zurück."  )})
    @RequestMapping(value = "/api/stammdatencertimport/starten/sync", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<VerzeichnisdienstImportErgebnis> apiStartenSync(Authentication authentication, @RequestBody VerzeichnisdienstImportCommandContainer verzeichnisdienstImportCommandContainer) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        log.info("apiStartenSync execute 4: "+authUserDetails.getMandant().getId()+" - "+authUserDetails.getMandant().getName());

        if (verzeichnisdienstImportCommandContainer.isSyncWithTsps()) {
            List<VerzeichnisdienstImportErgebnis> result = new ArrayList<>();
            VerzeichnisdienstImportErgebnis verzeichnisdienstImportErgebnis = new VerzeichnisdienstImportErgebnis();
            verzeichnisdienstImportErgebnis.setError(true);
            verzeichnisdienstImportErgebnis.getLog().add("Es soll mit den Tsp's ein sync durchgeführt werden. Aus Performancegründen nutzen Sie bitte den async-Request.");
            result.add(verzeichnisdienstImportErgebnis);
            return result;
        }

        return stammdatenZertImportService.importieren(authUserDetails.getMandant(), verzeichnisdienstImportCommandContainer);
    }

    @Operation(security = @SecurityRequirement(name = "basicAuth"), description = "Importieren der Stammdaten derart, dass die notwendigen Informationen in den Verzeichnisdienst übertragen werden. Dabei wird zwischen Einfüge- , Änderungs- und Löschoperationen unterschieden. Der Request wird asynchron gestartet. Dabei wird ein Batchjob gestartet und gespeichert. Sie werden per Mail informiert, wenn der Batchjob fertig ist.",
            responses = { @ApiResponse( responseCode = "200", description = "Der Request wird asynchron gestartet. Sie erhalten ein 'ok' zurück."  )})
    @RequestMapping(value = "/api/stammdatencertimport/starten/async", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String apiStartenAsync(Authentication authentication, @RequestBody VerzeichnisdienstImportCommandContainer verzeichnisdienstImportCommandContainer) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        log.info("apiStartenAsync execute 4: "+authUserDetails.getMandant().getId()+" - "+authUserDetails.getMandant().getName());
        stammdatenZertImportService.importierenAsync(authUserDetails.getMandant(), verzeichnisdienstImportCommandContainer);
        return "ok";
    }
}
