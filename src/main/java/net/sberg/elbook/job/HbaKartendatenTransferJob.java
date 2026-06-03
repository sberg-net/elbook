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

import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.kartendatentransfer.KartendatenTransfer;
import net.sberg.elbook.kartendatentransfer.KartendatenTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;

@Service
public class HbaKartendatenTransferJob {

    private static final Logger log = LoggerFactory.getLogger(HbaKartendatenTransferJob.class);

    @Autowired
    private JdbcGenericDao genericDao;
    @Autowired
    private KartendatenTransferService kartendatenTransferService;
    @Value("${kartendatenTransferService.gueltigBisTage}")
    private int gueltigBisTage;

    @Scheduled(cron="${hbaKartendatenTransferJob.scheduledDef}")
    public void execute() {
        try {
            handleExpiredData();
        }
        catch (Exception e) {
            log.error("error on handling expired data", e);
        }
    }

    private void handleExpiredData() throws Exception {
        List<KartendatenTransfer> daten = genericDao.selectMany(
                "select * from KartendatenTransfer where gueltigBis < CURRENT_TIMESTAMP",
                KartendatenTransfer.class.getName(),
                null,
                null
        );
        for (Iterator<KartendatenTransfer> iterator = daten.iterator(); iterator.hasNext(); ) {
            KartendatenTransfer kartendatenTransfer =  iterator.next();
            kartendatenTransferService.delete(kartendatenTransfer);
        }
        /*
        context filesystem
         */
        LocalDateTime dt = LocalDateTime.now().minusDays(gueltigBisTage);
        File f = new File(ICommonConstants.BASE_DIR + "datentransfer");
        File[] files = f.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            Path path = Paths.get(files[i].getAbsolutePath());
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            LocalDateTime convertedFileTime = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
            if (!convertedFileTime.isAfter(dt)) {
                files[i].delete();
            }
        }
    }

}
