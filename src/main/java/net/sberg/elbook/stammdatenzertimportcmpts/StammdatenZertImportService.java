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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.ws.cm.pers.hba_smc_b.v1.HbaAntragExport;
import de.gematik.ws.cm.pers.hba_smc_b.v1.SmcbAntragExport;
import de.gematik.ws.sst.v1.GetHbaAntraegeExportResponseType;
import de.gematik.ws.sst.v1.GetSmcbAntraegeExportResponseType;
import net.sberg.elbook.batchjobcmpts.BatchJob;
import net.sberg.elbook.batchjobcmpts.EnumBatchJobName;
import net.sberg.elbook.batchjobcmpts.EnumBatchJobStatusCode;
import net.sberg.elbook.common.FileUtils;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.common.MailCreatorAndSender;
import net.sberg.elbook.glossarcmpts.GlossarService;
import net.sberg.elbook.glossarcmpts.TelematikIdInfo;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.logeintragcmpts.EnumLogEintragArtikelTyp;
import net.sberg.elbook.logeintragcmpts.LogEintragService;
import net.sberg.elbook.mandantcmpts.Mandant;
import net.sberg.elbook.tspcmpts.*;
import net.sberg.elbook.verzeichnisdienstcmpts.VerzeichnisdienstService;
import net.sberg.elbook.verzeichnisdienstcmpts.VzdEntryWrapper;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class StammdatenZertImportService {

    private static final Logger log = LoggerFactory.getLogger(StammdatenZertImportService.class);

    @Autowired
    private JdbcGenericDao genericDao;
    @Autowired
    private GlossarService glossarService;
    @Autowired
    private VerzeichnisdienstService verzeichnisdienstService;
    @Autowired
    private LogEintragService logEintragService;
    @Autowired
    private MailCreatorAndSender mailCreatorAndSender;
    @Value("${elbook.encryptionKeys}")
    private String[] ENC_KEYS;

    private void createBatchJobMail(BatchJob batchJob, Mandant mandant) throws Exception {
        SimpleMailMessage msg = new SimpleMailMessage();
        if (mailCreatorAndSender.isTestMode()) {
            msg.setTo(mailCreatorAndSender.getTestRecipients().split(","));
        }
        else {
            msg.setTo(mandant.getMail());
        }
        msg.setFrom("software@sberg.net");
        msg.setSubject("Elbook: Einlesen der Import-Commands und Anlegen der Verzeichnisdiensteinträge - "+batchJob.getStatusCode()+" - ID = "+batchJob.getId());

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("Guten Tag,\n");
        bodyBuilder.append("Es wurden von "+batchJob.getAnzahlDatensaetze()+" Datensätzen wurden "+batchJob.getAnzahlDatensaetzeAbgearbeitet()+" abgearbeitet");
        bodyBuilder.append("\nViele Grüße\nIhr\nelBook-Team\n");

        msg.setText(bodyBuilder.toString());
        mailCreatorAndSender.write(msg, "batchjob_"+batchJob.getBatchJobName()+"_"+batchJob.getId()+"_"+mandant.getId()+"_"+System.nanoTime());
    }

    public void importierenAsync(Mandant mandant, VerzeichnisdienstImportCommandContainer verzeichnisdienstImportCommandContainer) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                importieren(mandant, verzeichnisdienstImportCommandContainer);
            }
        }).start();
    }

    private CompletableFuture<VerzeichnisdienstImportErgebnis> importieren(
            BatchJob batchJob,
            VerzeichnisdienstImportCommandContainer verzeichnisdienstImportCommandContainer,
            VerzeichnisdienstImportCommand verzeichnisdienstImportCommand,
            Mandant mandant,
            TiVZDProperties tiVZDProperties,
            Object mutex,
            List<Integer> finished,
            ExecutorService executorService
            ) {

        return CompletableFuture.supplyAsync(() -> {
            log.info("START - handle the verzeichnisdienstImportCommand for the batchjob " + batchJob.getId() + ", mandantId: " + mandant.getId() + ", Verwaltungs-ID: " + verzeichnisdienstImportCommand.getVerwaltungsId());

            synchronized (mutex) {
                try {
                    finished.add(1);
                    batchJob.setAnzahlDatensaetzeAbgearbeitet(finished.size());
                    genericDao.update(batchJob, Optional.empty());
                } catch (Exception ee) {
                    log.error("error on updating the STAMMDATEN_CERT_IMPORT-batchjob with the id: " + batchJob.getId(), ee);
                }
            }

            verzeichnisdienstImportCommand.setBundesland(mandant.getBundesland());
            VerzeichnisdienstImportErgebnis verzeichnisdienstImportErgebnis = new VerzeichnisdienstImportErgebnis();
            verzeichnisdienstImportErgebnis.fill(verzeichnisdienstImportCommand);
            verzeichnisdienstImportErgebnis.setSilentMode(verzeichnisdienstImportCommandContainer.isSilentMode());

            try {

                if (verzeichnisdienstImportErgebnis.isIgnore()) {
                    verzeichnisdienstImportErgebnis.getLog().add("eintrag wird ignoriert");
                    return verzeichnisdienstImportErgebnis;
                }

                if (verzeichnisdienstImportCommand.getTelematikIdInfo() == null) {
                    verzeichnisdienstImportErgebnis.getLog().add("telematikIdInfo konnte nicht abgefragt werden");
                    verzeichnisdienstImportErgebnis.setError(true);
                    return verzeichnisdienstImportErgebnis;
                }

                boolean emptyVzdUid = verzeichnisdienstImportCommand.getVzdUid() == null || verzeichnisdienstImportCommand.getVzdUid().trim().isEmpty();
                String telematikId = verzeichnisdienstImportCommand.getTelematikID();
                boolean emptyTelematikId = telematikId == null || telematikId.trim().isEmpty();
                boolean emptyBusinessId = verzeichnisdienstImportCommand.getVerwaltungsId() == null || verzeichnisdienstImportCommand.getVerwaltungsId().trim().isEmpty();

                VzdEntryWrapper vzdObject;

                if (emptyVzdUid) {

                    if (emptyTelematikId) {
                        verzeichnisdienstImportErgebnis.setError(true);
                        verzeichnisdienstImportErgebnis.getLog().add("bitte setzen sie die telematikID");
                        return verzeichnisdienstImportErgebnis;
                    }

                    vzdObject = verzeichnisdienstService.ladeByTelematikId(mandant, verzeichnisdienstImportCommand.getTelematikID());
                    if (vzdObject == null) {
                        verzeichnisdienstImportErgebnis.setInsert(true);
                    }

                }
                else {
                    vzdObject = verzeichnisdienstService.ladeByUid(mandant, verzeichnisdienstImportCommand.getVzdUid());
                    if (vzdObject == null) {
                        verzeichnisdienstImportErgebnis.setInsert(true);
                        verzeichnisdienstImportErgebnis.getLog().add("verzeichnisdienseintrag mit der id=" + verzeichnisdienstImportCommand.getVzdUid() + " kann nicht gefunden werden");
                    }
                }

                //DELETE
                if (verzeichnisdienstImportCommand.toDelete(vzdObject)) {

                    verzeichnisdienstImportErgebnis.setInsert(false);
                    verzeichnisdienstImportErgebnis.setUpdate(false);
                    verzeichnisdienstImportErgebnis.setDelete(true);

                    if (vzdObject == null) {
                        verzeichnisdienstImportErgebnis.getLog().add("löschen nicht möglich, da der verzeichnisdiensteintrag nicht gefunden werden kann");
                        verzeichnisdienstImportErgebnis.setError(true);
                    }
                    else {
                        if (!verzeichnisdienstImportErgebnis.isSilentMode()) {
                            verzeichnisdienstService.loeschen(mandant, tiVZDProperties, vzdObject.extractDirectoryEntryUid());

                            verzeichnisdienstImportErgebnis.setVzdUid(vzdObject.extractDirectoryEntryUid());

                            logEintragService.handle(
                                mandant,
                                vzdObject.extractDirectoryEntryTelematikId(),
                                emptyBusinessId ? "" : verzeichnisdienstImportCommand.getVerwaltungsId(),
                                vzdObject.extractDirectoryEntryUid(),
                                null,
                                EnumLogEintragArtikelTyp.CARD_ISSUER_DELETE
                            );

                            verzeichnisdienstImportErgebnis.getLog().add("verzeichnisdienseintrag wurde gelöscht mit der id=" + vzdObject.extractDirectoryEntryUid() + " und der telematikid=" + vzdObject.extractDirectoryEntryTelematikId());
                        }
                        else {
                            verzeichnisdienstImportErgebnis.getLog().add("SILENTMODE -> verzeichnisdienseintrag wurde gelöscht mit der id=" + vzdObject.extractDirectoryEntryUid() + " und der telematikid=" + vzdObject.extractDirectoryEntryTelematikId());
                        }
                    }

                    log.info("END - handle the verzeichnisdienstImportCommand for the batchjob " + batchJob.getId() + ", mandantId: " + mandant.getId() + ", Verwaltungs-ID: " + verzeichnisdienstImportCommand.getVerwaltungsId());
                    return verzeichnisdienstImportErgebnis;
                }

                //INSERT
                if (verzeichnisdienstImportErgebnis.isInsert()) {
                    if (!verzeichnisdienstImportErgebnis.isSilentMode()) {
                        List dnNameL = verzeichnisdienstService.speichern(tiVZDProperties, verzeichnisdienstImportCommand.createAddDirEntryCommand(tiVZDProperties));
                        VzdEntryWrapper distinguishedName = (VzdEntryWrapper) dnNameL.get(0);

                        verzeichnisdienstImportErgebnis.getLog().add("verzeichnisdienseintrag wurde eingefügt mit der id=" + distinguishedName.extractDistinguishedNameUid() + " und der telematikid=" + verzeichnisdienstImportCommand.getTelematikID());
                        verzeichnisdienstImportErgebnis.setVzdUid(distinguishedName.extractDistinguishedNameUid());

                        logEintragService.handle(
                            mandant,
                            verzeichnisdienstImportCommand.getTelematikID(),
                            emptyBusinessId ? "" : verzeichnisdienstImportCommand.getVerwaltungsId(),
                            distinguishedName.extractDistinguishedNameUid(),
                            null,
                            EnumLogEintragArtikelTyp.CARD_ISSUER_INSERT
                        );

                        if (!verzeichnisdienstImportCommand.getEncZertifikat().isEmpty()) {
                            verzeichnisdienstService.pruefeZertifikate(
                                mandant,
                                tiVZDProperties,
                                distinguishedName.extractDistinguishedNameUid(),
                                verzeichnisdienstImportCommand.getTelematikID(),
                                emptyBusinessId ? "" : verzeichnisdienstImportCommand.getVerwaltungsId(),
                                verzeichnisdienstImportCommand.getEncZertifikat().stream().map(EncZertifikat::getContent).collect(Collectors.toList()),
                                new ArrayList<>(),
                                verzeichnisdienstImportErgebnis,
                                EnumLogEintragArtikelTyp.CARD_ISSUER_CERT_INSERT
                            );
                        }
                    }
                    else {
                        verzeichnisdienstImportErgebnis.getLog().add("SILENTMODE -> verzeichnisdienseintrag wurde eingefügt mit der telematikid=" + verzeichnisdienstImportCommand.getTelematikID());
                    }

                    log.info("END - handle the verzeichnisdienstImportCommand for the batchjob " + batchJob.getId() + ", mandantId: " + mandant.getId() + ", Verwaltungs-ID: " + verzeichnisdienstImportCommand.getVerwaltungsId());
                    return verzeichnisdienstImportErgebnis;
                }

                //UPDATE
                if (!verzeichnisdienstImportErgebnis.isDelete() && !verzeichnisdienstImportErgebnis.isInsert() && verzeichnisdienstImportCommand.toUpdate(vzdObject)) {

                    verzeichnisdienstImportErgebnis.setUpdate(true);

                    verzeichnisdienstImportCommand.setVzdUid(vzdObject.extractDirectoryEntryUid());
                    verzeichnisdienstImportCommand.setTelematikID(vzdObject.extractDirectoryEntryTelematikId());

                    if (!verzeichnisdienstImportErgebnis.isSilentMode()) {
                        verzeichnisdienstService.speichern(tiVZDProperties, verzeichnisdienstImportCommand.createModDirEntryCommand(tiVZDProperties, vzdObject));
                        verzeichnisdienstImportErgebnis.getLog().add("verzeichnisdienseintrag wurde geändert mit der id=" + verzeichnisdienstImportCommand.getVzdUid() + " und der telematikid=" + verzeichnisdienstImportCommand.getTelematikID());
                    }
                    else {
                        verzeichnisdienstImportErgebnis.getLog().add("SILENTMODE -> verzeichnisdienseintrag wurde geändert mit der id=" + verzeichnisdienstImportCommand.getVzdUid() + " und der telematikid=" + verzeichnisdienstImportCommand.getTelematikID());
                    }

                    verzeichnisdienstImportErgebnis.setTelematikID(vzdObject.extractDirectoryEntryTelematikId());
                    verzeichnisdienstImportErgebnis.setVzdUid(vzdObject.extractDirectoryEntryUid());

                    if (!verzeichnisdienstImportErgebnis.isSilentMode()) {
                        logEintragService.handle(
                            mandant,
                            vzdObject.extractDirectoryEntryTelematikId(),
                            emptyBusinessId ? "" : verzeichnisdienstImportCommand.getVerwaltungsId(),
                            vzdObject.extractDirectoryEntryUid(),
                            null,
                            EnumLogEintragArtikelTyp.CARD_ISSUER_UPDATE
                        );
                    }

                    if (!verzeichnisdienstImportErgebnis.isSilentMode() && !verzeichnisdienstImportCommand.getEncZertifikat().isEmpty()) {
                        List<VzdEntryWrapper> vzdCerts = verzeichnisdienstService.ladeZertifikate(tiVZDProperties, vzdObject.extractDirectoryEntryUid());

                        verzeichnisdienstService.pruefeZertifikate(
                            mandant,
                            tiVZDProperties,
                            vzdObject.extractDirectoryEntryUid(),
                            vzdObject.extractDirectoryEntryTelematikId(),
                            emptyBusinessId ? "" : verzeichnisdienstImportCommand.getVerwaltungsId(),
                            verzeichnisdienstImportCommand.getEncZertifikat().stream().map(EncZertifikat::getContent).collect(Collectors.toList()),
                            vzdCerts,
                            verzeichnisdienstImportErgebnis,
                            EnumLogEintragArtikelTyp.CARD_ISSUER_CERT_INSERT
                        );

                    }

                    log.info("END - handle the verzeichnisdienstImportCommand for the batchjob " + batchJob.getId() + ", mandantId: " + mandant.getId() + ", Verwaltungs-ID: " + verzeichnisdienstImportCommand.getVerwaltungsId());
                    return verzeichnisdienstImportErgebnis;
                }

                verzeichnisdienstImportErgebnis.setVzdUid(vzdObject.extractDirectoryEntryUid());
                verzeichnisdienstImportErgebnis.setTelematikID(vzdObject.extractDirectoryEntryTelematikId());

                if (!verzeichnisdienstImportErgebnis.isDelete() && !verzeichnisdienstImportErgebnis.isInsert() && !verzeichnisdienstImportErgebnis.isUpdate() && !verzeichnisdienstImportCommand.getEncZertifikat().isEmpty()) {
                    List<VzdEntryWrapper> vzdCerts = verzeichnisdienstService.ladeZertifikate(tiVZDProperties, vzdObject.extractDirectoryEntryUid());

                    verzeichnisdienstService.pruefeZertifikate(
                        mandant,
                        tiVZDProperties,
                        vzdObject.extractDirectoryEntryUid(),
                        vzdObject.extractDirectoryEntryTelematikId(),
                        emptyBusinessId ? "" : verzeichnisdienstImportCommand.getVerwaltungsId(),
                        verzeichnisdienstImportCommand.getEncZertifikat().stream().map(EncZertifikat::getContent).collect(Collectors.toList()),
                        vzdCerts,
                        verzeichnisdienstImportErgebnis,
                        EnumLogEintragArtikelTyp.CARD_ISSUER_CERT_INSERT
                    );
                }
            } catch (Exception e) {
                log.error("error on handling the verzeichnisdienstImportCommand for the batchjob " + batchJob.getId() + ", mandantId: " + mandant.getId() + ", Verwaltungs-ID: " + verzeichnisdienstImportCommand.getVerwaltungsId(), e);
                verzeichnisdienstImportErgebnis.setError(true);
                verzeichnisdienstImportErgebnis.getLog().add("technischer fehler beim importieren: " + e.getMessage());
            }

            log.info("END - handle the verzeichnisdienstImportCommand for the batchjob " + batchJob.getId() + ", mandantId: " + mandant.getId() + ", Verwaltungs-ID: " + verzeichnisdienstImportCommand.getVerwaltungsId());
            return verzeichnisdienstImportErgebnis;
        }, executorService);
    }

    private VerzeichnisdienstImportCommandContainer handleTspSync(Mandant mandant, VerzeichnisdienstImportCommandContainer verzeichnisdienstImportCommandContainer) throws Exception {
        if (!verzeichnisdienstImportCommandContainer.isSyncWithTsps()) {
            return verzeichnisdienstImportCommandContainer;
        }

        TelematikIdInfo telematikIdInfo = verzeichnisdienstImportCommandContainer.getCommands().get(0).getTelematikIdInfo();

        verzeichnisdienstImportCommandContainer.getCommands().forEach(verzeichnisdienstImportCommand -> verzeichnisdienstImportCommand.setToIgnore(true));

        List<Tsp> tsps = genericDao.selectMany(
            Tsp.class.getName(),
            null,
            List.of(
                new DaoPlaceholderProperty("mandantId", mandant.getId())
            )
        );

        for (Iterator<Tsp> iterator = tsps.iterator(); iterator.hasNext(); ) {
            Tsp tsp = iterator.next();
            tsp.decrypt(ENC_KEYS);

            int limit = 100;

            if (telematikIdInfo.getProfessionOIDInfos().get(0).getTspAntragTyp().equals(EnumAntragTyp.SMCB)) {
                TspProperties tspProperties = tsp.create(EnumAntragTyp.SMCB);
                TspConnector tspConnector = new TspConnectorBuilder().build(tspProperties, tsp.getTspName().getQvda(), EnumAntragTyp.SMCB);

                int offset = 0;
                List<SmcbAntragExport> smcbAntragExports = new ArrayList<>();
                while (true) {
                    GetSmcbAntraegeExportResponseType getSmcbAntraegeExportResponseType = tspConnector.getSmcbAntraegeZertifikateFreigeschaltet(false, limit, offset);
                    smcbAntragExports.addAll(getSmcbAntraegeExportResponseType.getSmcbAntraegeExport().getSmcbAntragExport());
                    if (!getSmcbAntraegeExportResponseType.isAntraegeExportWeitereTreffer()) {
                        break;
                    }
                    offset = offset + limit;
                }
                verzeichnisdienstImportCommandContainer.syncSmcb(telematikIdInfo, smcbAntragExports);
            }
            else {
                TspProperties tspProperties = tsp.create(EnumAntragTyp.HBA);
                TspConnector tspConnector = new TspConnectorBuilder().build(tspProperties, tsp.getTspName().getQvda(), EnumAntragTyp.HBA);

                int offset = 0;
                List<HbaAntragExport> hbaAntragExports = new ArrayList<>();
                while (true) {
                    GetHbaAntraegeExportResponseType getHbaAntraegeExportResponseType = tspConnector.getHbaAntraegeZertifikateFreigeschaltet(false, limit, offset);
                    hbaAntragExports.addAll(getHbaAntraegeExportResponseType.getHbaAntraegeExport().getHbaAntragExport());
                    if (!getHbaAntraegeExportResponseType.isAntraegeExportWeitereTreffer()) {
                        break;
                    }
                    offset = offset + limit;
                }
                verzeichnisdienstImportCommandContainer.syncHba(telematikIdInfo, hbaAntragExports);
            }

        }
        return verzeichnisdienstImportCommandContainer;
    }

    public List<VerzeichnisdienstImportErgebnis> importieren(Mandant mandant, VerzeichnisdienstImportCommandContainer verzeichnisdienstImportCommandContainer) {
        List<VerzeichnisdienstImportErgebnis> result = new ArrayList<>();
        TiVZDProperties tiVZDProperties = null;

        try {
            tiVZDProperties = mandant.createAndGetTiVZDProperties(verzeichnisdienstService);
        }
        catch (Exception e) {
            log.error("error on importing the commands with the size: "+verzeichnisdienstImportCommandContainer.getCommands().size()+ " - mandant: "+mandant.getId(), e);
            VerzeichnisdienstImportErgebnis verzeichnisdienstImportErgebnis = new VerzeichnisdienstImportErgebnis();
            verzeichnisdienstImportErgebnis.setError(true);
            verzeichnisdienstImportErgebnis.getLog().add("Auf den Verzeichnisdienst kann momentan nicht zugegriffen werden.");
            result.add(verzeichnisdienstImportErgebnis);
            return result;
        }

        if (tiVZDProperties.getAuthSecret() == null || tiVZDProperties.getAuthSecret().trim().isEmpty() || tiVZDProperties.getAuthId() == null || tiVZDProperties.getAuthId().trim().isEmpty()) {

            if (verzeichnisdienstImportCommandContainer.getVzdAuthId() != null
                &&
                !verzeichnisdienstImportCommandContainer.getVzdAuthId().trim().isEmpty()
                &&
                verzeichnisdienstImportCommandContainer.getVzdAuthSecret() != null
                &&
                !verzeichnisdienstImportCommandContainer.getVzdAuthSecret().trim().isEmpty()) {

                tiVZDProperties.setAuthId(verzeichnisdienstImportCommandContainer.getVzdAuthId());
                tiVZDProperties.setAuthSecret(verzeichnisdienstImportCommandContainer.getVzdAuthSecret());

            }
            else {
                log.error("vzd credentials not saved: " + mandant.getId());
                VerzeichnisdienstImportErgebnis verzeichnisdienstImportErgebnis = new VerzeichnisdienstImportErgebnis();
                verzeichnisdienstImportErgebnis.setError(true);
                verzeichnisdienstImportErgebnis.getLog().add("bitte speichern sie die vzd credentials über die weboberfläche -> menüpunkt verzeichnisdienst");
                result.add(verzeichnisdienstImportErgebnis);
                return result;
            }
        }

        try {
            if (!verzeichnisdienstImportCommandContainer.isSyncWithTsps()) {
                verzeichnisdienstImportCommandContainer.merge();
            }
        }
        catch (Exception e) {
            log.error("error on importing / merge the commands with the size: "+verzeichnisdienstImportCommandContainer.getCommands().size()+ " - mandant: "+mandant.getId(), e);
            VerzeichnisdienstImportErgebnis verzeichnisdienstImportErgebnis = new VerzeichnisdienstImportErgebnis();
            verzeichnisdienstImportErgebnis.setError(true);
            verzeichnisdienstImportErgebnis.getLog().add("Mergen der Daten fehlgeschlagen. Bitte achten Sie darauf, dass die Telematik-ID immer gesetzt ist!");
            result.add(verzeichnisdienstImportErgebnis);
            return result;
        }

        try {
            for (Iterator<VerzeichnisdienstImportCommand> iterator = verzeichnisdienstImportCommandContainer.getCommands().iterator(); iterator.hasNext(); ) {
                VerzeichnisdienstImportCommand verzeichnisdienstImportCommand = iterator.next();
                verzeichnisdienstImportCommand.setTelematikIdInfo(glossarService.getTelematikIdInfo(verzeichnisdienstImportCommand.getTelematikID()));
            }
        }
        catch (Exception e) {
            log.error("error on importing / take the telematikIdInfo: "+verzeichnisdienstImportCommandContainer.getCommands().size()+ " - mandant: "+mandant.getId(), e);
            VerzeichnisdienstImportErgebnis verzeichnisdienstImportErgebnis = new VerzeichnisdienstImportErgebnis();
            verzeichnisdienstImportErgebnis.setError(true);
            verzeichnisdienstImportErgebnis.getLog().add("Holen TelematikIdInfo ist fehlgeschlagen. Bitte achten Sie darauf, dass die Telematik-ID immer gesetzt ist!");
            result.add(verzeichnisdienstImportErgebnis);
            return result;
        }

        BatchJob batchJob = new BatchJob();
        try {
            batchJob.setBatchJobName(EnumBatchJobName.STAMMDATEN_CERT_IMPORT);
            batchJob.setGestartetAm(LocalDateTime.now());
            batchJob.setMandantId(mandant.getId());
            batchJob.setStatusCode(EnumBatchJobStatusCode.RUNNING);
            batchJob.setAnzahlDatensaetze(verzeichnisdienstImportCommandContainer.getCommands().size());
            genericDao.insert(batchJob, Optional.empty());
        }
        catch (Exception e) {
            log.error("error on importing the commands with the size: "+verzeichnisdienstImportCommandContainer.getCommands().size()+ " - mandant: "+mandant.getId(), e);
            VerzeichnisdienstImportErgebnis verzeichnisdienstImportErgebnis = new VerzeichnisdienstImportErgebnis();
            verzeichnisdienstImportErgebnis.setError(true);
            verzeichnisdienstImportErgebnis.getLog().add("batchjob kann nicht eingefügt werden!!! technischer fehler");
            result.add(verzeichnisdienstImportErgebnis);
            return result;
        }

        boolean tspSyncSuccess = true;
        try {
            verzeichnisdienstImportCommandContainer = handleTspSync(mandant, verzeichnisdienstImportCommandContainer);
        }
        catch (Exception e) {
            log.error("error on importing the commands with the size: "+verzeichnisdienstImportCommandContainer.getCommands().size()+ " - mandant: "+mandant.getId()+" - tsp sync failed", e);
            VerzeichnisdienstImportErgebnis verzeichnisdienstImportErgebnis = new VerzeichnisdienstImportErgebnis();
            verzeichnisdienstImportErgebnis.setError(true);
            verzeichnisdienstImportErgebnis.getLog().add("TSP Sync ist fehlgeschlagen");
            result.add(verzeichnisdienstImportErgebnis);
            tspSyncSuccess = false;
        }

        if (!verzeichnisdienstImportCommandContainer.isSyncWithTsps() || tspSyncSuccess) {
            List futureList = Collections.synchronizedList(new ArrayList());
            log.info("START - handle all verzeichnisdienstImportCommands for the batchjob: " + batchJob.getId() + ", mandantId: " + mandant.getId());

            int threadAnzahl = mandant.getThreadAnzahl() > 0 ? mandant.getThreadAnzahl() : 3;
            ExecutorService executorService = Executors.newFixedThreadPool(threadAnzahl);

            Object mutex = new Object();
            List<Integer> finished = Collections.synchronizedList(new ArrayList<>());

            for (Iterator<VerzeichnisdienstImportCommand> iterator = verzeichnisdienstImportCommandContainer.getCommands().iterator(); iterator.hasNext(); ) {
                VerzeichnisdienstImportCommand verzeichnisdienstImportCommand = iterator.next();

                futureList.add(importieren(
                    batchJob,
                    verzeichnisdienstImportCommandContainer,
                    verzeichnisdienstImportCommand,
                    mandant,
                    tiVZDProperties,
                    mutex,
                    finished,
                    executorService
                ));
            }

            CompletableFuture<VerzeichnisdienstImportErgebnis>[] arr = new CompletableFuture[futureList.size()];
            CompletableFuture.allOf((CompletableFuture<VerzeichnisdienstImportErgebnis>[]) futureList.toArray(arr)).join();
            executorService.shutdown();

            result = (List<VerzeichnisdienstImportErgebnis>) futureList.stream().map(f -> {
                try {
                    return ((CompletableFuture) f).get();
                } catch (Exception e) {
                    log.error("error on handle the completable-futures for the STAMMDATEN_CERT_IMPORT-batchjob with the id: " + batchJob.getId(), e);
                }
                return null;
            }).collect(Collectors.toList());
        }

        log.info("END - handle all verzeichnisdienstImportCommands for the batchjob: " + batchJob.getId() + ", mandantId: " + mandant.getId());

        batchJob.setStatusCode(EnumBatchJobStatusCode.SUCCESS);
        batchJob.setBeendetAm(LocalDateTime.now());

        try {
            FileUtils.writeToFile(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result), ICommonConstants.BASE_DIR + "jobs" + File.separator + "ergebnisse_"+mandant.getId()+"_"+batchJob.getId()+".json");
            genericDao.update(batchJob, Optional.empty());
            createBatchJobMail(batchJob, mandant);
        }
        catch (Exception ee) {
            log.error("error on updating the STAMMDATEN_CERT_IMPORT-batchjob with the id: "+batchJob.getId(), ee);
        }

        return result;
    }

}
