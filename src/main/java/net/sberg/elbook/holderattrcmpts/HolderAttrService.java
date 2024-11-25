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
package net.sberg.elbook.holderattrcmpts;

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
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.logeintragcmpts.EnumLogEintragArtikelTyp;
import net.sberg.elbook.logeintragcmpts.LogEintragService;
import net.sberg.elbook.mandantcmpts.Mandant;
import net.sberg.elbook.stammdatenzertimportcmpts.EncZertifikat;
import net.sberg.elbook.tspcmpts.*;
import net.sberg.elbook.verzeichnisdienstcmpts.DirectoryEntrySaveContainer;
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
public class HolderAttrService {

    private static final Logger log = LoggerFactory.getLogger(HolderAttrService.class);

    @Autowired
    private VerzeichnisdienstService verzeichnisdienstService;
    @Autowired
    private LogEintragService logEintragService;
    @Autowired
    private GlossarService glossarService;
    @Value("${elbook.encryptionKeys}")
    private String[] ENC_KEYS;

    private CompletableFuture<HolderAttrErgebnis> importieren(
        HolderAttrCommandContainer holderAttrCommandContainer,
        HolderAttrCommand holderAttrCommand,
        Mandant mandant,
        TiVZDProperties tiVZDProperties,
        ExecutorService executorService
        ) {

        return CompletableFuture.supplyAsync(() -> {
            log.info("START - handle the command for the mandantId: " + mandant.getId() + ", Telematik-ID: " + holderAttrCommand.getTelematikID());

            HolderAttrErgebnis holderAttrErgebnis = new HolderAttrErgebnis();

            try {
                VzdEntryWrapper vzdObject = verzeichnisdienstService.ladeByTelematikId(mandant, holderAttrCommand.getTelematikID());
                if (vzdObject == null) {
                    holderAttrErgebnis.setError(true);
                    holderAttrErgebnis.getLog().add("vzdn entry with the id: "+holderAttrCommand.getTelematikID()+" not available");
                }
                else {
                    DirectoryEntrySaveContainer directoryEntrySaveContainer = holderAttrCommand.createModDirEntryCommand(glossarService, tiVZDProperties, vzdObject, log, holderAttrCommandContainer, holderAttrErgebnis);
                    if (!holderAttrErgebnis.isError()) {
                        verzeichnisdienstService.speichern(tiVZDProperties, directoryEntrySaveContainer);
                        holderAttrErgebnis.getLog().add("holder des verzeichnisdienseintrags wurde geändert mit der id=" + vzdObject.extractDirectoryEntryUid() + " und der telematikid=" + holderAttrCommand.getTelematikID());

                        logEintragService.handle(
                            mandant,
                            vzdObject.extractDirectoryEntryTelematikId(),
                            "",
                            vzdObject.extractDirectoryEntryUid(),
                            null,
                            EnumLogEintragArtikelTyp.VZD_HOLDER_CHANGED
                        );
                    }
                }
            } catch (Exception e) {
                log.error("error on handling the command for mandantId: " + mandant.getId() + ", Telematik-ID: " + holderAttrCommand.getTelematikID(), e);
                holderAttrErgebnis.setError(true);
                holderAttrErgebnis.getLog().add("technischer fehler beim importieren: " + e.getMessage());
            }

            log.info("END - handle the command for the mandantId: " + mandant.getId() + ", Telematik-ID: " + holderAttrCommand.getTelematikID());

            return holderAttrErgebnis;
        }, executorService);
    }

    public List<HolderAttrErgebnis> execute(Mandant mandant, HolderAttrCommandContainer holderAttrCommandContainer) {
        List<HolderAttrErgebnis> result = new ArrayList<>();
        TiVZDProperties tiVZDProperties = null;

        try {
            tiVZDProperties = mandant.createAndGetTiVZDProperties(verzeichnisdienstService);
        }
        catch (Exception e) {
            log.error("error on importing the commands with the size: "+holderAttrCommandContainer.getCommands().size()+ " - mandant: "+mandant.getId(), e);
            HolderAttrErgebnis holderAttrErgebnis = new HolderAttrErgebnis();
            holderAttrErgebnis.setError(true);
            holderAttrErgebnis.getLog().add("Auf den Verzeichnisdienst kann momentan nicht zugegriffen werden.");
            result.add(holderAttrErgebnis);
            return result;
        }

        if (tiVZDProperties.getAuthSecret() == null || tiVZDProperties.getAuthSecret().trim().isEmpty() || tiVZDProperties.getAuthId() == null || tiVZDProperties.getAuthId().trim().isEmpty()) {

            if (holderAttrCommandContainer.getVzdAuthId() != null
                &&
                !holderAttrCommandContainer.getVzdAuthId().trim().isEmpty()
                &&
                holderAttrCommandContainer.getVzdAuthSecret() != null
                &&
                !holderAttrCommandContainer.getVzdAuthSecret().trim().isEmpty()) {

                tiVZDProperties.setAuthId(holderAttrCommandContainer.getVzdAuthId());
                tiVZDProperties.setAuthSecret(holderAttrCommandContainer.getVzdAuthSecret());

            }
            else {
                log.error("vzd credentials not saved: " + mandant.getId());
                HolderAttrErgebnis holderAttrErgebnis = new HolderAttrErgebnis();
                holderAttrErgebnis.setError(true);
                holderAttrErgebnis.getLog().add("bitte speichern sie die vzd credentials über die weboberfläche -> menüpunkt verzeichnisdienst");
                result.add(holderAttrErgebnis);
                return result;
            }
        }

        List futureList = Collections.synchronizedList(new ArrayList());
        log.info("START - handle all commands for the mandantId: " + mandant.getId());

        int threadAnzahl = mandant.getThreadAnzahl() > 0 ? mandant.getThreadAnzahl() : 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadAnzahl);

        for (Iterator<HolderAttrCommand> iterator = holderAttrCommandContainer.getCommands().iterator(); iterator.hasNext(); ) {
            HolderAttrCommand holderAttrCommand = iterator.next();

            futureList.add(importieren(
                holderAttrCommandContainer,
                holderAttrCommand,
                mandant,
                tiVZDProperties,
                executorService
            ));
        }

        CompletableFuture<HolderAttrErgebnis>[] arr = new CompletableFuture[futureList.size()];
        CompletableFuture.allOf((CompletableFuture<HolderAttrErgebnis>[]) futureList.toArray(arr)).join();
        executorService.shutdown();

        result = (List<HolderAttrErgebnis>) futureList.stream().map(f -> {
            try {
                return ((CompletableFuture) f).get();
            } catch (Exception e) {
                log.error("error on handle the completable-futures for the commands with the mandantId: "+mandant.getId(), e);
            }
            return null;
        }).collect(Collectors.toList());

        log.info("END - handle all commands for the mandantId: " + mandant.getId());

        return result;
    }

}
