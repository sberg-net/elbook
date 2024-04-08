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
package net.sberg.elbook.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.mandantcmpts.Mandant;
import net.sberg.elbook.reportcmpts.Report;
import net.sberg.elbook.reportcmpts.ReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportJob {


    private final JdbcGenericDao genericDao;
    private final ReportService reportService;

    @Value("${reportService.active}")
    private boolean active;

    @Value("${elbook.encryptionKeys}")
    private String[] ENC_KEYS;

    @SuppressWarnings("rawtypes")
    @Scheduled(cron="${reportJob.scheduledDef}")
    public void execute() {
        try {

            if (!active) {
                return;
            }

            Map<Integer, Mandant> mandantMap = new HashMap<>();
            List reports = genericDao.selectMany(Report.class.getName(), null, null);
            LocalDateTime now = LocalDateTime.now();
            LocalDate nowDate = LocalDate.now();
            for (Object o : reports) {
                Report report = (Report) o;

                if (report.getLetzteAusfuehrungAm() != null
                        &&
                        now.minusDays(report.getIntervall().getIntervalInDays()).isBefore(report.getLetzteAusfuehrungAm())) {
                    continue;
                }
                if (nowDate.isAfter(report.getGueltigBis())) {
                    continue;
                }

                if (!mandantMap.containsKey(report.getMandantId())) {
                    Mandant mandant = (Mandant) genericDao.selectOne(
                            Mandant.class.getName(),
                            null,
                            List.of(new DaoPlaceholderProperty("id", report.getMandantId()))
                    );
                    if (mandant != null && mandant.getGueltigBis().isAfter(LocalDate.now())) {
                        Mandant superMandant = null;
                        if (mandant.getMandantId() > 0) {
                            superMandant = (Mandant) genericDao.selectOne(Mandant.class.getName(), null,
                                    List.of(new DaoPlaceholderProperty("id", mandant.getMandantId())));
                            superMandant.decrypt(ENC_KEYS);
                        }
                        mandant.decrypt(ENC_KEYS);
                        if (superMandant != null
                                &&
                                superMandant.getVzdAuthId() != null
                                &&
                                !superMandant.getVzdAuthId().trim().isEmpty()
                                &&
                                superMandant.getVzdAuthSecret() != null
                                &&
                                !superMandant.getVzdAuthSecret().trim().isEmpty()) {

                            mandant.setVzdAuthId(superMandant.getVzdAuthId());
                            mandant.setVzdAuthSecret(superMandant.getVzdAuthSecret());
                        }
                        if (superMandant != null) {
                            mandant.setGoldLizenz(superMandant.isGoldLizenz());
                            mandant.setSektor(superMandant.getSektor());
                        }
                        mandantMap.put(report.getMandantId(), mandant);
                    } else {
                        log.error("user not valid: " + Objects.requireNonNull(mandant).getNutzername()
                                + " - " + mandant.getId());
                        continue;
                    }
                }

                Mandant hMandant = mandantMap.get(report.getMandantId());
                new Thread(() -> {
                    try {
                        reportService.start(hMandant, report, true, true);
                    } catch (Exception e) {
                        log.error("error on handling report: " + report.getId() + " - mandant: "
                                + hMandant.getId() + " - " + hMandant.getName(), e);
                    }
                }).start();
            }
        }
        catch (Exception e) {
            log.error("error on handling reports", e);
        }
    }
}
