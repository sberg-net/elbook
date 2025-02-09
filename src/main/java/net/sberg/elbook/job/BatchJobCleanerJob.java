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

import net.sberg.elbook.batchjobcmpts.BatchJob;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.DaoProjectionBean;
import net.sberg.elbook.jdbc.JdbcGenericDao;
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
import java.util.Optional;

@Service
public class BatchJobCleanerJob {

    private static final Logger log = LoggerFactory.getLogger(BatchJobCleanerJob.class);

    @Autowired
    private JdbcGenericDao genericDao;

    @Value("${batchJobCleanerJob.expiredDays}")
    private long expiredDays;

    @Scheduled(cron="${batchJobCleanerJob.scheduledDef}")
    public void execute() {
        try {
            handleExpiredData();
        }
        catch (Exception e) {
            log.error("error on handling expired data", e);
        }
    }

    private void handleExpiredData() throws Exception {
        LocalDateTime dt = LocalDateTime.now().minusDays(expiredDays);

        /*
        context db
         */
        List<BatchJob> daten = genericDao.selectMany(
            "select * from BatchJob where beendetAm <= ?",
            BatchJob.class.getName(),
            null,
            List.of(new DaoPlaceholderProperty("beendetAm", dt))
        );
        for (Iterator<BatchJob> iterator = daten.iterator(); iterator.hasNext(); ) {
            BatchJob batchJob =  iterator.next();
            File f = new File(ICommonConstants.BASE_DIR + "jobs" + File.separator + "ergebnisse_"+batchJob.getMandantId()+"_"+batchJob.getId()+".json");
            if (f.exists()) {
                f.delete();
            }
            genericDao.delete(batchJob, Optional.empty());
        }

        /*
        context filesystem
         */
        File f = new File(ICommonConstants.BASE_DIR + "jobs");
        File[] files = f.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        DaoProjectionBean daoProjectionBean = new DaoProjectionBean(null, null, true);
        for (int i = 0; i < files.length; i++) {
            Path path = Paths.get(files[i].getAbsolutePath());
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            LocalDateTime convertedFileTime = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
            int mandantId = Integer.parseInt(files[i].getName().split("_")[1]);
            int id = Integer.parseInt(files[i].getName().split("_")[2].split("\\.")[0]);
            Long count = (Long)genericDao.selectOne(
                "select count(*) from BatchJob where id = ? and mandantId = ?",
                null,
                daoProjectionBean,
                List.of(new DaoPlaceholderProperty("id", id), new DaoPlaceholderProperty("mandantId", mandantId))
            );
            if ((count == null || count.intValue() == 0) && !convertedFileTime.isAfter(dt)) {
                files[i].delete();
            }
        }
    }

}
