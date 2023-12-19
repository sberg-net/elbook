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
package net.sberg.elbook.reportcmpts;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.sberg.elbook.authcomponents.AuthUserDetails;
import net.sberg.elbook.common.FileData;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.common.StringUtils;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final JdbcGenericDao genericDao;
    private final ReportService reportService;

    @Value("${reportService.active}")
    private boolean active;
    @Value("${elbook.encryptionKeys}")
    private String[] ENC_KEYS;

    @RequestMapping(value = "/report", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String reportView() {
        return "report/report";
    }

    @RequestMapping(value = "/report/uebersicht", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersicht(Authentication authentication, Model model) throws Exception {
        if (active) {
            AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
            List<Report> reports = genericDao.selectMany(
                    Report.class.getName(),
                    null,
                    List.of(new DaoPlaceholderProperty("mandantId", authUserDetails.getMandant().getId())));

            for (Report report : reports) {
                File f = new File(ICommonConstants.BASE_DIR + "reports" + File.separator + report.getMandantId() + File.separator + report.getId());
                if (!f.exists() || Objects.requireNonNull(f.listFiles()).length == 0) {
                    report.setEditierbar(true);
                }
            }

            model.addAttribute("reports", reports);
        } else {
            model.addAttribute("reports", new ArrayList<>());
        }

        return active ? "report/reportUebersichtAktiv" : "report/reportUebersichtNichtAktiv";
    }

    @RequestMapping(value = "/report/lade/{reportId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String lade(Model model, @PathVariable int reportId) throws Exception {
        Report report;
        if (reportId == -1) {
            report = new Report();
        } else {
            report = (Report) genericDao.selectOne(Report.class.getName(), null,
                    List.of(new DaoPlaceholderProperty("id", reportId)));
        }
        model.addAttribute("report", report);

        if (reportId == -1) {
            report.setEditierbar(true);
            model.addAttribute("dateien", new ArrayList<>());
        } else {
            File f = new File(ICommonConstants.BASE_DIR + "reports" + File.separator + report.getMandantId() + File.separator + report.getId());
            if (!f.exists() || Objects.requireNonNull(f.listFiles()).length == 0) {
                report.setEditierbar(true);
                model.addAttribute("dateien", new ArrayList<>());
            } else {
                File[] files = f.listFiles();
                List<FileData> fileDataList = new ArrayList<>();
                assert files != null;
                for (File file : files) {
                    fileDataList.add(new FileData(file.getName(), StringUtils.encrypt(ENC_KEYS, file.getName())));
                }
                model.addAttribute("dateien", fileDataList);
            }
        }

        return "report/reportFormular";
    }

    @RequestMapping(value = "/report/loeschen/{reportId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String loeschen(Authentication authentication, @PathVariable int reportId) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();

        //delete reports
        FileSystemUtils.deleteRecursively(new File(ICommonConstants.BASE_DIR + "reports"
                + File.separator + authUserDetails.getMandant().getId() + File.separator + reportId));

        Report report = (Report) genericDao.selectOne(Report.class.getName(), null,
                List.of(new DaoPlaceholderProperty("id", reportId)));
        genericDao.delete(report, Optional.empty());
        return "ok";
    }

    @RequestMapping(value = "/report/speichern", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String speichern(Authentication authentication, @RequestBody Report report) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();

        Report dbReport;
        boolean insert = false;
        if (report.getId() == 0) {
            insert = true;
            dbReport = new Report();
            dbReport.setErstelltAm(LocalDateTime.now());
            dbReport.setMandantId(authUserDetails.getMandant().getId());
        } else {
            dbReport = (Report) genericDao.selectOne(Report.class.getName(), null,
                    List.of(new DaoPlaceholderProperty("id", report.getId())));
            dbReport.setGeaendertAm(LocalDateTime.now());
        }

        dbReport.setGueltigBis(report.getGueltigBis());
        dbReport.setDescriptor(report.getDescriptor());

        if (insert) {
            genericDao.insert(dbReport, Optional.empty());
        } else {
            genericDao.update(dbReport, Optional.empty());
        }

        return "ok";
    }

    @SuppressWarnings("resource")
    @RequestMapping(value = "/report/starte/{reportId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void starte(HttpServletResponse response, Authentication authentication, @PathVariable int reportId) throws Exception {
        Report report = (Report) genericDao.selectOne(Report.class.getName(), null,
                List.of(new DaoPlaceholderProperty("id", reportId)));
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        File f = reportService.start(authUserDetails.getMandant(), report, false, true);

        response.setHeader("Content-Disposition", "attachment; filename=" + f.getName());
        response.setContentType("application/csv");

        InputStream inputStream = new FileInputStream(f);
        inputStream.transferTo(response.getOutputStream());
        response.flushBuffer();
    }

    @SuppressWarnings("resource")
    @RequestMapping(value = "/report/ladedatei/{reportId}/{hash}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void ladeDatei(HttpServletResponse response, @PathVariable int reportId, @PathVariable String hash) throws Exception {
        Report report = (Report) genericDao.selectOne(Report.class.getName(), null,
                List.of(new DaoPlaceholderProperty("id", reportId)));
        String name = StringUtils.decrypt(ENC_KEYS, hash);
        File f = new File(ICommonConstants.BASE_DIR + "reports" + File.separator + report.getMandantId()
                + File.separator + reportId + File.separator + name);

        response.setHeader("Content-Disposition", "attachment; filename=" + f.getName());
        response.setContentType("application/csv");

        InputStream inputStream = new FileInputStream(f);
        inputStream.transferTo(response.getOutputStream());
        response.flushBuffer();
    }

}