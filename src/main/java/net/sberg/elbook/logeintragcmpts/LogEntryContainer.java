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
package net.sberg.elbook.logeintragcmpts;

import lombok.Data;
import net.sberg.elbook.common.DateTimeUtils;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.VzdEntryWrapper;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.EnumCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class LogEntryContainer {

    private static final Logger log = LoggerFactory.getLogger(LogEntryContainer.class);

    private VzdEntryWrapper logEntry;
    private String logDateTime;
    private String operation;

    private LogEntryContainer() {}

    public LogEntryContainer(VzdEntryWrapper logEntry) throws Exception {
        this.logEntry = logEntry;
        fill(logEntry);
    }

    private void fill(VzdEntryWrapper logEntry) throws Exception {
        if (logEntry.extractLogEntryLogTime() != null) {
            LocalDateTime localDateTime = DateTimeUtils.convertFrom(logEntry.extractLogEntryLogTime()).toLocalDateTime();
            setLogDateTime(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(localDateTime));
        }
        EnumCommand command = EnumCommand.getEntry(logEntry.extractLogEntryOperation());
        operation = command.getHrText();
    }

    public String createSummary() {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(logDateTime+": ");
        resultBuilder.append(logEntry.extractLogEntryClientID()+", ");
        resultBuilder.append(operation+", ");
        resultBuilder.append("noDataChanged - "+logEntry.extractLogEntryNoDataChanged());
        return resultBuilder.toString();
    }
}
