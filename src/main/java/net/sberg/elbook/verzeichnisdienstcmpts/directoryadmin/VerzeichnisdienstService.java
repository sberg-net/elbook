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
package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.batchjobcmpts.BatchJob;
import net.sberg.elbook.batchjobcmpts.EnumBatchJobName;
import net.sberg.elbook.batchjobcmpts.EnumBatchJobStatusCode;
import net.sberg.elbook.common.FileUtils;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.logeintragcmpts.EnumLogEintragArtikelTyp;
import net.sberg.elbook.logeintragcmpts.LogEintragService;
import net.sberg.elbook.mandantcmpts.Mandant;
import net.sberg.elbook.stammdatenzertimportcmpts.VerzeichnisdienstImportErgebnis;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.TiVZDConnector;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.TiVZDProperties;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.*;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.DefaultCommandResultCallbackHandler;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerzeichnisdienstService {

    private final JdbcGenericDao genericDao;
    private final LogEintragService logEintragService;
    private final TiVZDConnector tiVZDConnector;

    @PostConstruct
    private void markBatchjobAsSystemEndedByStart() throws Exception {
        genericDao.update(
        "update BatchJob set beendetAm = ?, statusCode = '" + EnumBatchJobStatusCode.SYSTEMENDED
            + "' where batchJobName = '" + EnumBatchJobName.STAMMDATEN_CERT_IMPORT
            + "' and statusCode = '" + EnumBatchJobStatusCode.RUNNING + "'",
            BatchJob.class.getName(),
            List.of(new DaoPlaceholderProperty("beendetAm", LocalDateTime.now()))
        );
    }

    @PreDestroy
    private void markBatchjobAsSystemEndedByEnd() throws Exception {
        genericDao.update(
                "update BatchJob set beendetAm = ?, statusCode = '" + EnumBatchJobStatusCode.SYSTEMENDED
                        + "' where batchJobName = '" + EnumBatchJobName.STAMMDATEN_CERT_IMPORT
                        + "' and statusCode = '" + EnumBatchJobStatusCode.RUNNING + "'",
                BatchJob.class.getName(),
                List.of(new DaoPlaceholderProperty("beendetAm", LocalDateTime.now()))
        );
    }

    public VzdEntryWrapper ladeByTelematikId(Mandant mandant, String telematikId) throws Exception {
        ReadDirEntryCommand readDirEntryCommand = new ReadDirEntryCommand();
        readDirEntryCommand.setTelematikId(telematikId);
        readDirEntryCommand.setBaseEntryOnly(false);
        return lade(mandant, readDirEntryCommand);
    }

    public VzdEntryWrapper ladeByUid(Mandant mandant, String uid) throws Exception {
        ReadDirEntryCommand readDirEntryCommand = new ReadDirEntryCommand();
        readDirEntryCommand.setUid(uid);
        readDirEntryCommand.setBaseEntryOnly(false);
        return lade(mandant, readDirEntryCommand);
    }

    @SuppressWarnings("unchecked")
    private VzdEntryWrapper lade(Mandant mandant, ReadDirEntryCommand readDirEntryCommand) throws Exception {
        TiVZDProperties tiVZDProperties = mandant.createAndGetTiVZDProperties(this);

        DefaultCommandResultCallbackHandler defaultCommandResultCallbackHandler =
                tiVZDConnector.executeCommand(readDirEntryCommand, tiVZDProperties);
        List<VzdEntryWrapper> entries = defaultCommandResultCallbackHandler.getDirectoryEntries(readDirEntryCommand.getId());
        if ((entries == null || entries.isEmpty())
                && !defaultCommandResultCallbackHandler.getExceptions(readDirEntryCommand.getId()).isEmpty()) {
            throw defaultCommandResultCallbackHandler.getExceptions(readDirEntryCommand.getId()).get(0);
        } else if ((entries == null || entries.isEmpty())
                && !defaultCommandResultCallbackHandler.getResultReasons(readDirEntryCommand.getId()).isEmpty()) {
            if (readDirEntryCommand.getTelematikId() != null && !readDirEntryCommand.getTelematikId().isEmpty()) {
                log.info("entry not found for the telematikId: " + readDirEntryCommand.getTelematikId());
            } else {
                log.info("entry not found for the id: " + readDirEntryCommand.getUid());
            }
            return null;
        }
        return entries != null ? entries.get(0) : null;
    }

    @SuppressWarnings("rawtypes")
    public List speichernZertifikat(TiVZDProperties tiVZDProperties, String certContent, String uid) throws Exception {
        AddDirCertCommand addDirCertCommand = new AddDirCertCommand();
        addDirCertCommand.setUid(uid);
        List<String> certContents = new ArrayList<>();
        certContents.add(certContent);
        addDirCertCommand.setCertContents(certContents);

        DefaultCommandResultCallbackHandler commandResultCallbackHandler =
                tiVZDConnector.executeCommand(addDirCertCommand, tiVZDProperties);
        List entries = commandResultCallbackHandler.getDistinguishedNames(addDirCertCommand.getId());
        if ((entries == null || entries.isEmpty())
                && !commandResultCallbackHandler.getExceptions(addDirCertCommand.getId()).isEmpty()) {
            throw commandResultCallbackHandler.getExceptions(addDirCertCommand.getId()).get(0);
        } else if ((entries == null || entries.isEmpty())
                && !commandResultCallbackHandler.getResultReasons(addDirCertCommand.getId()).isEmpty()) {
            throw new IllegalStateException("certificate not created for the id: " + uid);
        }
        return entries;
    }

    public String createCSVExport(Mandant mandant, List<DirectoryEntryContainer> searchResults) throws Exception {

        String uuid = UUID.randomUUID().toString();
        String fileName = ICommonConstants.BASE_DIR + "vzdCsvExport" + File.separator + mandant.getId()
                + File.separator + mandant.getId() + "_" + uuid + ".csv";
        FileUtils.checkExistsFileDir(fileName);

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8);

        ICSVWriter csvWriter = new CSVWriterBuilder(outputStreamWriter)
                .withSeparator(';')
                .withQuoteChar('"')
                .withEscapeChar('\\')
                .build();

        List<String[]> csvLines = new ArrayList<>();

        csvLines.add(new String[]{"UUID", "TelematikID"});
        for (DirectoryEntryContainer directoryEntryContainer : searchResults) {
            VzdEntryWrapper vzdEntryWrapper = directoryEntryContainer.getVzdEntryWrapper();
            csvLines.add(new String[]{vzdEntryWrapper.extractDirectoryEntryUid(), vzdEntryWrapper.extractDirectoryEntryTelematikId()});
        }
        csvWriter.writeAll(csvLines);

        csvWriter.flush();
        outputStreamWriter.flush();

        csvWriter.close();
        outputStreamWriter.close();

        return uuid;
    }

    public void loeschenZertifikat(TiVZDProperties tiVZDProperties, String uid, String certUid) throws Exception {
        DelDirCertCommand delDirCertCommand = new DelDirCertCommand();
        delDirCertCommand.setUid(uid);
        List<String> certUids = new ArrayList<>();
        certUids.add(certUid);
        delDirCertCommand.setCertUids(certUids);

        DefaultCommandResultCallbackHandler commandResultCallbackHandler = tiVZDConnector.executeCommand(delDirCertCommand, tiVZDProperties);

        List<AbstractCommandResultCallbackHandler.ResultReason> entries = commandResultCallbackHandler.getResultReasons(delDirCertCommand.getId());
        if ((entries == null || entries.isEmpty()) && !commandResultCallbackHandler.getExceptions(delDirCertCommand.getId()).isEmpty()) {
            throw commandResultCallbackHandler.getExceptions(delDirCertCommand.getId()).get(0);
        } else if (entries != null && entries.isEmpty()) {
            throw new IllegalStateException("certificate can not removed 4 the uid: " + delDirCertCommand.getUid() + " and the cert uid: " + certUid);
        }

        AbstractCommandResultCallbackHandler.ResultReason resultReason = Objects.requireNonNull(entries).get(0);
        if (!resultReason.isResult()) {
            throw new IllegalStateException("certificate can not removed 4 the uid: " + delDirCertCommand.getUid() + " and the cert uid: " + certUid);
        }
    }

    @SuppressWarnings("unchecked")
    public List<VzdEntryWrapper> ladeZertifikate(TiVZDProperties tiVZDProperties, String uid) throws Exception {
        ReadDirCertCommand readDirCertCommand = new ReadDirCertCommand();
        readDirCertCommand.setUid(uid);
        DefaultCommandResultCallbackHandler commandResultCallbackHandler = tiVZDConnector.executeCommand(readDirCertCommand, tiVZDProperties);
        List<VzdEntryWrapper> entries = commandResultCallbackHandler.getUserCertificates(readDirCertCommand.getId());
        if ((entries == null || entries.isEmpty()) && !commandResultCallbackHandler.getExceptions(readDirCertCommand.getId()).isEmpty()) {
            throw commandResultCallbackHandler.getExceptions(readDirCertCommand.getId()).get(0);
        } else if ((entries == null || entries.isEmpty())
                && !commandResultCallbackHandler.getResultReasons(readDirCertCommand.getId()).isEmpty()) {
            if (!commandResultCallbackHandler.getResultReasons(readDirCertCommand.getId()).get(0).getResultReason()
                    .equals(AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS)) {
                throw new IllegalStateException("entry certificats not found for the id: " + uid);
            } else {
                entries = new ArrayList<>();
            }
        }
        return entries;
    }

    @SuppressWarnings("unchecked")
    public VzdEntryWrapper ladeZertifikat(TiVZDProperties tiVZDProperties, String uid, String certUid) throws Exception {

        ReadDirCertCommand readDirCertCommand = new ReadDirCertCommand();
        readDirCertCommand.setCertUid(certUid);
        readDirCertCommand.setUid(uid);

        DefaultCommandResultCallbackHandler commandResultCallbackHandler = tiVZDConnector.executeCommand(readDirCertCommand, tiVZDProperties);
        List<VzdEntryWrapper> entries = commandResultCallbackHandler.getUserCertificates(readDirCertCommand.getId());
        if ((entries == null || entries.isEmpty()) && !commandResultCallbackHandler.getExceptions(readDirCertCommand.getId()).isEmpty()) {
            throw commandResultCallbackHandler.getExceptions(readDirCertCommand.getId()).get(0);
        } else if ((entries == null || entries.isEmpty()) && !commandResultCallbackHandler.getResultReasons(readDirCertCommand.getId()).isEmpty()) {
            throw new IllegalStateException("entry certificats not found for the id: " + uid);
        }
        return Objects.requireNonNull(entries).get(0);
    }

    public void loeschen(Mandant mandant, TiVZDProperties tiVZDProperties, String uid) throws Exception {
        VzdEntryWrapper vzdEntryWrapper = ladeByUid(mandant, uid);
        DirectoryEntryContainer directorEntryContainer = new DirectoryEntryContainer(vzdEntryWrapper, null, mandant);
        if (vzdEntryWrapper == null) {
            throw new IllegalStateException("Eintrag mit der uid: " + uid + " ist nicht vorhanden");
        } else if (directorEntryContainer.getFadMailAttrs() != null && !directorEntryContainer.getFadMailAttrs().isEmpty()) {
            throw new IllegalStateException("Eintrag mit der uid: " + uid + " kann nicht gelöscht werden, da KIM-Mails zugeordnet sind");
        } else if (directorEntryContainer.getFadKomLeDataAttrs() != null && !directorEntryContainer.getFadKomLeDataAttrs().isEmpty()) {
            throw new IllegalStateException("Eintrag mit der uid: " + uid + " kann nicht gelöscht werden, da KIM-Mails zugeordnet sind");
        } else if (directorEntryContainer.getFadKimDataAttrs() != null && !directorEntryContainer.getFadKimDataAttrs().isEmpty()) {
            throw new IllegalStateException("Eintrag mit der uid: " + uid + " kann nicht gelöscht werden, da KIM-Mails zugeordnet sind");
        } else if (!vzdEntryWrapper.checkDirectoryEntryHolder(tiVZDProperties.getAuthId())) {
            throw new IllegalStateException("Eintrag mit der uid: " + uid
                    + " kann nicht gelöscht werden, da der Besitzer: " + tiVZDProperties.getAuthId() + " nicht die Berechtigung hat");
        }

        DelDirEntryCommand delDirEntryCommand = new DelDirEntryCommand();
        delDirEntryCommand.setUid(uid);

        DefaultCommandResultCallbackHandler commandResultCallbackHandler = tiVZDConnector.executeCommand(delDirEntryCommand, tiVZDProperties);
        List<AbstractCommandResultCallbackHandler.ResultReason> entries = commandResultCallbackHandler.getResultReasons(delDirEntryCommand.getId());
        if ((entries == null || entries.isEmpty()) && !commandResultCallbackHandler.getExceptions(delDirEntryCommand.getId()).isEmpty()) {
            throw commandResultCallbackHandler.getExceptions(delDirEntryCommand.getId()).get(0);
        } else if (entries != null && entries.isEmpty()) {
            throw new IllegalStateException("entry can not removed 4 the uid: " + uid);
        }

        AbstractCommandResultCallbackHandler.ResultReason resultReason = Objects.requireNonNull(entries).get(0);
        if (!resultReason.isResult()) {
            throw new IllegalStateException("entry can not removed 4 the uid: " + uid);
        }
    }

    public DefaultCommandResultCallbackHandler info(TiVZDProperties tiVZDProperties, GetInfoCommand getInfoCommand) throws Exception {
        return tiVZDConnector.executeCommand(getInfoCommand, tiVZDProperties);
    }

    private List<VzdEntryWrapper> logEntries(TiVZDProperties tiVZDProperties, ReadDirLogEntryCommand readDirLogEntryCommand) throws Exception {
        DefaultCommandResultCallbackHandler commandResultCallbackHandler = tiVZDConnector.executeCommand(readDirLogEntryCommand, tiVZDProperties);

        List<VzdEntryWrapper> entries = commandResultCallbackHandler.getLogEntries(readDirLogEntryCommand.getId());
        if ((entries == null || entries.isEmpty()) && !commandResultCallbackHandler.getExceptions(readDirLogEntryCommand.getId()).isEmpty()) {
            throw commandResultCallbackHandler.getExceptions(readDirLogEntryCommand.getId()).get(0);
        } else if ((entries == null || entries.isEmpty())
                && !commandResultCallbackHandler.getResultReasons(readDirLogEntryCommand.getId()).isEmpty()) {
            if (!commandResultCallbackHandler.getResultReasons(readDirLogEntryCommand.getId()).get(0).getResultReason()
                    .equals(AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS)) {
                if (readDirLogEntryCommand.getUid() != null) {
                    throw new IllegalStateException("entry logs not found for the uid: " + readDirLogEntryCommand.getUid());
                }
                else {
                    throw new IllegalStateException("entry logs not found for the telematikId: " + readDirLogEntryCommand.getTelematikId());
                }
            } else {
                entries = new ArrayList<>();
            }
        }
        return entries;
    }

    @SuppressWarnings("unchecked")
    public List<VzdEntryWrapper> logEntriesForUid(TiVZDProperties tiVZDProperties, String uid) throws Exception {
        ReadDirLogEntryCommand readDirLogEntryCommand = new ReadDirLogEntryCommand();
        readDirLogEntryCommand.setUid(uid);
        return logEntries(tiVZDProperties, readDirLogEntryCommand);
    }

    public List<VzdEntryWrapper> logEntriesForTelematikId(TiVZDProperties tiVZDProperties, String telematikId) throws Exception {
        ReadDirLogEntryCommand readDirLogEntryCommand = new ReadDirLogEntryCommand();
        readDirLogEntryCommand.setTelematikId(telematikId);
        return logEntries(tiVZDProperties, readDirLogEntryCommand);
    }

    public DefaultCommandResultCallbackHandler uebersicht(TiVZDProperties tiVZDProperties, VzdOverviewSearchContainer vzdOverviewSearchContainer) throws Exception {
        if (vzdOverviewSearchContainer.getReadDirSyncEntryCommand() != null) {
            vzdOverviewSearchContainer.getReadDirSyncEntryCommand().setBaseEntryOnly(true);
            return tiVZDConnector.executeCommand(vzdOverviewSearchContainer.getReadDirSyncEntryCommand(), tiVZDProperties);
        }
        else {
            return tiVZDConnector.executeCommand(vzdOverviewSearchContainer.getReadDirSyncEntryFaAttributesCommand(), tiVZDProperties);
        }
    }

    @SuppressWarnings("rawtypes")
    public List speichern(TiVZDProperties tiVZDProperties, DirectoryEntrySaveContainer directorEntrySaveContainer) throws Exception {
        DefaultCommandResultCallbackHandler commandResultCallbackHandler =
                tiVZDConnector.executeCommand(directorEntrySaveContainer.isCreate() ? directorEntrySaveContainer.getAddDirEntryCommand()
                        : directorEntrySaveContainer.getModDirEntryCommand(), tiVZDProperties);

        AbstractCommand command = directorEntrySaveContainer.isCreate() ? directorEntrySaveContainer.getAddDirEntryCommand()
                : directorEntrySaveContainer.getModDirEntryCommand();
        List entries = directorEntrySaveContainer.isCreate() ? commandResultCallbackHandler.getDistinguishedNames(command.getId())
                : commandResultCallbackHandler.getResultReasons(command.getId());
        if ((entries == null || entries.isEmpty()) && !commandResultCallbackHandler.getExceptions(command.getId()).isEmpty()) {
            throw commandResultCallbackHandler.getExceptions(command.getId()).get(0);
        } else if ((entries == null || entries.isEmpty()) && command.command().equals(EnumCommand.ADD_DIR_ENTRY)
                && !commandResultCallbackHandler.getResultReasons(command.getId()).isEmpty()) {
            AbstractCommandResultCallbackHandler.ResultReason resultReason = (AbstractCommandResultCallbackHandler.ResultReason)
                    Objects.requireNonNull(entries).get(0);
            throw new IllegalStateException("entry can not created: " + resultReason.getResultReason());
        } else //noinspection EqualsBetweenInconvertibleTypes
            if (entries != null && !entries.isEmpty() && command.equals(EnumCommand.MOD_DIR_ENTRY)) {
            AbstractCommandResultCallbackHandler.ResultReason resultReason = (AbstractCommandResultCallbackHandler.ResultReason) entries.get(0);
            if (!resultReason.isResult()) {
                throw new IllegalStateException("entry can not saved 4 the uid: " + command.getUid());
            }
        }
        return entries;
    }

    public void statusAendern(TiVZDProperties tiVZDProperties, SwitchStateDirEntryCommand switchStateDirEntryCommand) throws Exception {
        DefaultCommandResultCallbackHandler commandResultCallbackHandler =
                tiVZDConnector.executeCommand(switchStateDirEntryCommand, tiVZDProperties);

        List<AbstractCommandResultCallbackHandler.ResultReason> reasons =
                commandResultCallbackHandler.getResultReasons(switchStateDirEntryCommand.getId());
        if (reasons == null || reasons.isEmpty()
                || !commandResultCallbackHandler.getExceptions(switchStateDirEntryCommand.getId()).isEmpty()) {
            throw commandResultCallbackHandler.getExceptions(switchStateDirEntryCommand.getId()).get(0);
        } else {
            AbstractCommandResultCallbackHandler.ResultReason resultReason = reasons.get(0);
            if (!resultReason.isResult()) {
                throw new IllegalStateException("Status kann nicht geändert werden: " + resultReason.getResultReason());
            }
            resultReason.isResult();
        }
    }

    @SuppressWarnings("rawtypes")
    public void pruefeZertifikate(
        Mandant mandant,
        TiVZDProperties tiVZDProperties,
        String uid,
        String telematikId,
        String verwaltungsId,
        List<String> newCerts,
        List<VzdEntryWrapper> vzdEntryWrappers,
        VerzeichnisdienstImportErgebnis verzeichnisdienstImportErgebnis,
        EnumLogEintragArtikelTyp artikelTyp) throws Exception {
        Map<String, VerzeichnisdienstZertifikat> vzdCertMap = new HashMap<>();
        boolean changed = false;
        for (VzdEntryWrapper vzdEntryWrapper : vzdEntryWrappers) {
            if (vzdEntryWrapper.extractUserCertificateContent() == null) {
                continue;
            }
            VerzeichnisdienstZertifikat verzeichnisdienstZertifikat = erstelle(vzdEntryWrapper);
            if (verzeichnisdienstZertifikat != null) {
                vzdCertMap.put(verzeichnisdienstZertifikat.getSerienNummer(), verzeichnisdienstZertifikat);
            }
        }
        for (String newCert : newCerts) {
            VerzeichnisdienstZertifikat verzeichnisdienstZertifikat = erstelle(newCert, null);
            if (!vzdCertMap.containsKey(Objects.requireNonNull(verzeichnisdienstZertifikat).getSerienNummer())) {

                if (verzeichnisdienstZertifikat.isBase64Encoded()) {
                    newCert = new String(Base64.getDecoder().decode(newCert), StandardCharsets.UTF_8);
                }

                try {
                    if (verzeichnisdienstZertifikat.isValid()) {
                        changed = true;
                        List dnNameCertL = speichernZertifikat(tiVZDProperties, newCert, uid);
                        VzdEntryWrapper distinguishedNameCert = (VzdEntryWrapper) dnNameCertL.get(0);
                        verzeichnisdienstImportErgebnis.getLog().add("zertifikat erfolgreich gespeichert: telematikid = "
                                + telematikId + ",uid = " + uid + ", certUid= " + distinguishedNameCert.extractDistinguishedNameCn());
                        logEintragService.handle(mandant, telematikId, verwaltungsId, uid, distinguishedNameCert.extractDistinguishedNameCn(), artikelTyp);
                        vzdCertMap.put(verzeichnisdienstZertifikat.getSerienNummer(), verzeichnisdienstZertifikat);
                    }
                    else {
                        verzeichnisdienstImportErgebnis.getLog().add("zertifikat ist nicht mehr gültig: " + newCert);
                    }

                } catch (Exception e) {
                    log.error("error on saving the certificate: telematikId -> " + telematikId + " - uid -> " + uid);
                    verzeichnisdienstImportErgebnis.getLog().add("fehler beim speichern des zertifikats: " + newCert);
                    verzeichnisdienstImportErgebnis.setError(true);
                    vzdCertMap.put(verzeichnisdienstZertifikat.getSerienNummer(), verzeichnisdienstZertifikat);
                }
            }
        }

        if (!verzeichnisdienstImportErgebnis.isError()) {
            if (!changed) {
                verzeichnisdienstImportErgebnis.getLog().add("zertifikate wurden nicht geändert: telematikid = "
                        + telematikId + ",uid = " + uid);
            } else {
                verzeichnisdienstImportErgebnis.setCertUpdate(true);
            }
        }

    }

    private VerzeichnisdienstZertifikat erstelle(String content, String telematikId) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = null;
        boolean base64Encoded = false;
        try {
            String certStr = "-----BEGIN CERTIFICATE-----" + "\n"
                    + content + "\n"
                    + "-----END CERTIFICATE-----";
            cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certStr.getBytes()));
        } catch (Exception e) {
            String certStr = "-----BEGIN CERTIFICATE-----" + "\n"
                    + new String(Base64.getDecoder().decode(content),StandardCharsets.UTF_8) + "\n"
                    + "-----END CERTIFICATE-----";
            try {
                cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certStr.getBytes()));
                base64Encoded = true;
            } catch (Exception ignored) {
            }
        }
        if (cert != null) {
            VerzeichnisdienstZertifikat zertifikat = new VerzeichnisdienstZertifikat();
            zertifikat.setAussteller(cert.getIssuerX500Principal().getName());
            zertifikat.setInhaber(cert.getSubjectX500Principal().toString());
            zertifikat.setSerienNummer(cert.getSerialNumber().toString());
            zertifikat.setVersion(String.valueOf(cert.getVersion()));
            zertifikat.setGueltigVon(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss")
                    .format(new Timestamp(cert.getNotBefore().getTime()).toLocalDateTime()));
            zertifikat.setGueltigBis(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss")
                    .format(new Timestamp(cert.getNotAfter().getTime()).toLocalDateTime()));
            zertifikat.setTelematikId(telematikId);
            zertifikat.setBase64Encoded(base64Encoded);

            zertifikat.setValid(true);
            if (new Timestamp(cert.getNotAfter().getTime()).toLocalDateTime().isBefore(LocalDateTime.now())) {
                zertifikat.setValid(false);
            }

            return zertifikat;
        }
        return null;
    }

    public VerzeichnisdienstZertifikat erstelle(VzdEntryWrapper vzdEntryWrapper) throws Exception {
        return erstelle(vzdEntryWrapper.extractUserCertificateContent(), vzdEntryWrapper.extractUserCertificateTelematikId());
    }
}
