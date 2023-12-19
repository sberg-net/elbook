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

import net.sberg.elbook.common.MailCreatorAndSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MailJob {

    private static final Logger log = LoggerFactory.getLogger(MailJob.class);

    @Autowired
    private MailCreatorAndSender mailCreatorAndSender;

    @Scheduled(cron="${mailCreatorAndSenderService.scheduledDef}")
    public void execute() {
        try {
            mailCreatorAndSender.send();
        } catch (Exception e) {
            log.error("error on executing the mail job: " + e.getMessage(), e);
        }
    }
}
