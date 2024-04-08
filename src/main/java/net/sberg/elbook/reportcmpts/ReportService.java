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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import net.sberg.elbook.common.FileUtils;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.mandantcmpts.Mandant;
import net.sberg.elbook.verzeichnisdienstcmpts.VerzeichnisdienstService;
import net.sberg.elbook.verzeichnisdienstcmpts.VzdEntryWrapper;
import net.sberg.elbook.verzeichnisdienstcmpts.VzdOverviewSearchContainer;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.PagingInfo;
import net.sberg.elbook.vzdclientcmpts.command.ReadDirSyncEntryCommand;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.DefaultCommandResultCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private JdbcGenericDao genericDao;
    @Autowired
    private VerzeichnisdienstService verzeichnisdienstService;
    @Value("${reportService.concurrentMode}")
    private boolean concurrentMode;

    public File start(Mandant mandant, Report report, boolean saveFile, boolean saveDatetime) throws Exception {
        TiVZDProperties tiVZDProperties = mandant.createAndGetTiVZDProperties(verzeichnisdienstService);

        if (tiVZDProperties.getAuthSecret() == null || tiVZDProperties.getAuthSecret().trim().isEmpty() || tiVZDProperties.getAuthId() == null || tiVZDProperties.getAuthId().trim().isEmpty()) {
            log.error("vzd credentials not available 4 Mandant "+mandant.getId()+" - "+mandant.getName());
            throw new IllegalStateException("Bitte die VZD-Credentials unter dem Menüpunkt Verzeichnisdienst eingeben");
        }

        Map descr = new ObjectMapper().readValue(getClass().getResourceAsStream("/report/descriptions.json"), Map.class);
        ReportDescriptor reportDescriptor = new ObjectMapper().convertValue(descr.get(report.getDescriptor().name()), ReportDescriptor.class);

        List<ReportDescriptorKey> result = new ArrayList<>();
        if (!concurrentMode) {
            for (Iterator<String> iterator = reportDescriptor.getKuerzel().keySet().iterator(); iterator.hasNext(); ) {
                String telematikKuerzel =  iterator.next();

                ReportDescriptorKey reportDescriptorKey = reportDescriptor.getKuerzel().get(telematikKuerzel);

                result.add(
                    start(
                        report,
                        tiVZDProperties,
                        reportDescriptor,
                        reportDescriptorKey,
                        telematikKuerzel
                    )
                );
            }
        }
        else {
            List futureList = Collections.synchronizedList(new ArrayList());
            int threadAnzahl = mandant.getThreadAnzahl() > 0?mandant.getThreadAnzahl():3;
            ExecutorService executorService = Executors.newFixedThreadPool(threadAnzahl);

            for (Iterator<String> iterator = reportDescriptor.getKuerzel().keySet().iterator(); iterator.hasNext(); ) {
                String telematikKuerzel =  iterator.next();

                ReportDescriptorKey reportDescriptorKey = reportDescriptor.getKuerzel().get(telematikKuerzel);

                futureList.add(start(
                    report,
                    tiVZDProperties,
                    reportDescriptor,
                    reportDescriptorKey,
                    telematikKuerzel,
                    executorService
                ));
            }

            CompletableFuture<ReportDescriptorKey>[] arr = new CompletableFuture[futureList.size()];
            CompletableFuture.allOf((CompletableFuture<ReportDescriptorKey>[])futureList.toArray(arr)).join();
            executorService.shutdown();

            result = (List<ReportDescriptorKey>)futureList.stream().map(f -> {
                try {
                    return ((CompletableFuture)f).get();
                }
                catch (Exception e) {
                    log.error("error on handle the completable-futures for the ReportService with the id: "+report.getId(), e);
                }
                return null;
            }).collect(Collectors.toList());
        }

        File f = createFile(report, merge(result), saveFile);

        if (saveDatetime) {
            report.setLetzteAusfuehrungAm(LocalDateTime.now());
            genericDao.update(report, Optional.empty());
        }

        return f;
    }

    private String createFileName(Report report) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
        return report.getDescriptor().name() + df.format(new Date()) + ".csv";
    }

    private ReportDescriptorKey start(
        Report report,
        TiVZDProperties tiVZDProperties,
        ReportDescriptor reportDescriptor,
        ReportDescriptorKey reportDescriptorKey,
        String telematikKuerzel
    ) {
        try {
            ReadDirSyncEntryCommand readDirSyncEntryCommand = new ReadDirSyncEntryCommand();
            readDirSyncEntryCommand.setTelematikIdSubstr(reportDescriptor.getPrefix()+telematikKuerzel);

            PagingInfo pagingInfo = new PagingInfo();
            pagingInfo.setOneTimePaging(false);
            pagingInfo.setPagingSize(200);
            pagingInfo.setPagingCookie("");
            readDirSyncEntryCommand.setPagingInfo(pagingInfo);

            VzdOverviewSearchContainer vzdOverviewSearchContainer = new VzdOverviewSearchContainer();
            vzdOverviewSearchContainer.setReadDirSyncEntryCommand(readDirSyncEntryCommand);

            DefaultCommandResultCallbackHandler commandResultCallbackHandler = verzeichnisdienstService.uebersicht(tiVZDProperties, vzdOverviewSearchContainer);

            List<VzdEntryWrapper> entries = commandResultCallbackHandler.getDirectoryEntries(readDirSyncEntryCommand.getId());

            if ((entries == null || entries.isEmpty()) && !commandResultCallbackHandler.getExceptions(readDirSyncEntryCommand.getId()).isEmpty()) {
                reportDescriptorKey.setError(true);
                reportDescriptorKey.setCount(0);
                log.error("error on handle the ReportService with the id: "+report.getId()+", the reportDescriptor: "+reportDescriptor.getId()+", the reportDescriptorKey: "+reportDescriptorKey.getKey()+reportDescriptorKey.getSubKey()+", telematikKuerzel: "+telematikKuerzel);
            }
            else if ((entries == null || entries.isEmpty()) && !commandResultCallbackHandler.getResultReasons(readDirSyncEntryCommand.getId()).isEmpty()) {
                reportDescriptorKey.setCount(0);
            }
            else {
                reportDescriptorKey.setCount(entries.size());
            }
        }
        catch (Exception e) {
            log.error("error on handle the completable-futures for the ReportService with the id: "+report.getId()+", the reportDescriptor: "+reportDescriptor.getId()+", the reportDescriptorKey: "+reportDescriptorKey.getKey()+reportDescriptorKey.getSubKey()+", telematikKuerzel: "+telematikKuerzel, e);
            reportDescriptorKey.setError(true);
            reportDescriptorKey.setCount(0);
        }
        return reportDescriptorKey;
    }

    private CompletableFuture<ReportDescriptorKey> start(
            Report report,
            TiVZDProperties tiVZDProperties,
            ReportDescriptor reportDescriptor,
            ReportDescriptorKey reportDescriptorKey,
            String telematikKuerzel,
            ExecutorService executorService
    ) {
        return CompletableFuture.supplyAsync(() -> start(report, tiVZDProperties, reportDescriptor, reportDescriptorKey, telematikKuerzel), executorService);
    }

    private List<ReportDescriptorKey> merge(List<ReportDescriptorKey> countMap) throws Exception {
        List<ReportDescriptorKey> result = new ArrayList<>();
        Map<String, ReportDescriptorKey> hMap = new HashMap<>();
        for (Iterator<ReportDescriptorKey> iterator = countMap.iterator(); iterator.hasNext(); ) {
            ReportDescriptorKey reportDescriptorKey = iterator.next();
            if (!hMap.containsKey(reportDescriptorKey.getKey())) {
                hMap.put(reportDescriptorKey.getKey(), reportDescriptorKey);
                result.add(reportDescriptorKey);
            }
            else {
                ReportDescriptorKey parentDescrKey = hMap.get(reportDescriptorKey.getKey());
                parentDescrKey.getChildren().add(reportDescriptorKey);
                if (reportDescriptorKey.isError()) {
                    parentDescrKey.setError(true);
                }
                parentDescrKey.setCount(parentDescrKey.getCount()+reportDescriptorKey.getCount());
            }
        }
        return result;
    }

    private File createFile(Report report, List<ReportDescriptorKey> keys, boolean saveFile) throws Exception {

        String fileName = null;
        if (saveFile) {
            fileName = ICommonConstants.BASE_DIR + "reports" + File.separator + report.getMandantId() + File.separator + report.getId() + File.separator + createFileName(report);
            FileUtils.checkExistsFileDir(fileName);
        }
        else {
            fileName = System.getProperty("java.io.tmpdir") + File.separator + createFileName(report);
        }

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");

        ICSVWriter csvWriter = new CSVWriterBuilder(outputStreamWriter)
                .withSeparator(';')
                .withQuoteChar('"')
                .withEscapeChar('\\')
                .build();

        List<String[]> csvLines = new ArrayList<String[]>();

        csvLines.add(new String[] {"Schlüssel", "Wert", "Fehler aufgetreten"});
        for (Iterator<ReportDescriptorKey> iterator = keys.iterator(); iterator.hasNext(); ) {
            ReportDescriptorKey key =  iterator.next();
            csvLines.add(new String[] {key.getKey(), String.valueOf(key.getCount()), key.isError()?"Ja":"Nein"});
        }
        csvWriter.writeAll(csvLines);

        csvWriter.flush();
        outputStreamWriter.flush();

        csvWriter.close();
        outputStreamWriter.close();

        File file = new File(fileName);

        return file;
    }

}