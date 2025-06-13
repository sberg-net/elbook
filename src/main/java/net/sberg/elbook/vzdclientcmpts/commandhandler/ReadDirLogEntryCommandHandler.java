package net.sberg.elbook.vzdclientcmpts.commandhandler;

import de.gematik.vzd.api.V1_12_7.LogApi;
import de.gematik.vzd.model.V1_12_7.LogEntry;
import net.sberg.elbook.common.StringUtils;
import net.sberg.elbook.verzeichnisdienstcmpts.VzdEntryWrapper;
import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.EnumCommand;
import net.sberg.elbook.vzdclientcmpts.command.ReadDirLogEntryCommand;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.ICommandResultCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReadDirLogEntryCommandHandler extends AbstractCommandHandler {

    private Logger log = LoggerFactory.getLogger(ReadDirLogEntryCommandHandler.class);
    private LogApi logApiV1_12_7;
    private de.gematik.vzd.api.V1_12_6.LogApi logApiV1_12_6;

    public ReadDirLogEntryCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        if (client.getTiVZDProperties().getApiVersion().equals(TiVZDProperties.API_VERSION_V1_12_7)) {
            logApiV1_12_7 = new LogApi(client);
        }
        else if (client.getTiVZDProperties().getApiVersion().equals(TiVZDProperties.API_VERSION_V1_12_6)) {
            logApiV1_12_6 = new de.gematik.vzd.api.V1_12_6.LogApi(client);
        }
        else {
            throw new IllegalStateException("unknown api version: "+client.getTiVZDProperties().getInfoObject().getVersion());
        }
    }

    @Override
    public boolean checkValidation() {
        if (!(command instanceof ReadDirLogEntryCommand)) {
            throw new IllegalStateException("command not type of ReadDirEntryCommand");
        }

        ReadDirLogEntryCommand readDirLogEntryCommand = (ReadDirLogEntryCommand)command;

        boolean check = false;

        List<String> params = new ArrayList<>();
        params.add(readDirLogEntryCommand.getUid());
        params.add(readDirLogEntryCommand.getTelematikId());
        params.add(readDirLogEntryCommand.getLogTimeFrom());
        params.add(readDirLogEntryCommand.getLogTimeTo());
        params.add(readDirLogEntryCommand.getOperation());

        if (readDirLogEntryCommand.getNoDataChanged() != null) {
            check = true;
        }

        String holder = (readDirLogEntryCommand.getHolder() != null && !readDirLogEntryCommand.getHolder().isEmpty())?readDirLogEntryCommand.getHolder().get(0):null;
        if (holder != null) {
            check = true;
        }

        for (String parameter : params) {
            if (!org.apache.commons.lang3.StringUtils.isBlank(parameter)) {
                check = true;
            }
        }

        if (!check) {
            log.error("Missing argument " + command.command()
                + "\n" + command);
        }
        return check;
    }

    public void executeCommand() {
        try {
            client.validateToken();

            ReadDirLogEntryCommand readDirLogEntryCommand = (ReadDirLogEntryCommand)command;

            String uid = readDirLogEntryCommand.getUid();
            String telematikId = readDirLogEntryCommand.getTelematikId();
            String logtimeTo = readDirLogEntryCommand.getLogTimeTo();
            String logtimeFrom = readDirLogEntryCommand.getLogTimeFrom();
            String operation = readDirLogEntryCommand.getOperation();
            Boolean noDataChanged = readDirLogEntryCommand.getNoDataChanged();
            String holder = !StringUtils.listToString(readDirLogEntryCommand.getHolder()).equals("")?StringUtils.listToString(readDirLogEntryCommand.getHolder()):null;

            if (logApiV1_12_7 != null) {
                ResponseEntity<List<LogEntry>> response = logApiV1_12_7.readLogWithHttpInfo(uid, telematikId, holder, logtimeFrom, logtimeTo, operation, noDataChanged);

                if (response.getStatusCode() == HttpStatus.OK) {
                    if (response.getBody().isEmpty()) {
                        commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS));
                    } else {
                        for (Iterator<LogEntry> iterator = response.getBody().iterator(); iterator.hasNext(); ) {
                            LogEntry logEntry = iterator.next();
                            commandResultCallbackHandler.handleLogEntry(command, new VzdEntryWrapper(logEntry));
                        }
                    }
                } else {
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                    log.error("read directory log entry execution failed. Response status was: "
                            + response.getStatusCode() + "\n" + command);
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "read directory log entry execution failed. Response status was: "
                            + response.getStatusCode() + "\n" + command));
                }
            }
            else if (logApiV1_12_6 != null) {
                ResponseEntity<List<de.gematik.vzd.model.V1_12_6.LogEntry>> response = logApiV1_12_6.readLogWithHttpInfo(uid, telematikId, holder, logtimeFrom, logtimeTo, operation, noDataChanged);

                if (response.getStatusCode() == HttpStatus.OK) {
                    if (response.getBody().isEmpty()) {
                        commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS));
                    } else {
                        for (Iterator<de.gematik.vzd.model.V1_12_6.LogEntry> iterator = response.getBody().iterator(); iterator.hasNext(); ) {
                            de.gematik.vzd.model.V1_12_6.LogEntry logEntry = iterator.next();
                            commandResultCallbackHandler.handleLogEntry(command, new VzdEntryWrapper(logEntry));
                        }
                    }
                } else {
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                    log.error("read directory log entry execution failed. Response status was: "
                            + response.getStatusCode() + "\n" + command);
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "read directory log entry execution failed. Response status was: "
                            + response.getStatusCode() + "\n" + command));
                }
            }
        }
        catch (RestClientResponseException e) {
            AbstractCommandResultCallbackHandler.ServerResult serverResult = new AbstractCommandResultCallbackHandler.ServerResult(e.getStatusCode().value(), e.getResponseHeaders());
            commandResultCallbackHandler.handle(command, serverResult);

            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS));
            }
            else if (e.getStatusCode().value() == HttpStatus.BAD_REQUEST.value()) {
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.BAD_REQUEST));
            }
            else {
                log.error("error on executing command: " + EnumCommand.READ_DIR_LOG_ENTRY.getSpecName() + "\n" + command + "\n" + serverResult, e);
                commandResultCallbackHandler.handle(command, e);
            }
        }
        catch (Exception e) {
            log.error("error on executing command: " + EnumCommand.READ_DIR_LOG_ENTRY.getSpecName() + "\n" + command, e);
            commandResultCallbackHandler.handle(command, e);
        }
    }
}
