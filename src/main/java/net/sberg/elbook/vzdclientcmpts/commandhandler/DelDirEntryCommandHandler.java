package net.sberg.elbook.vzdclientcmpts.commandhandler;

import de.gematik.vzd.api.V1_12_7.DirectoryEntryAdministrationApi;
import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.DelDirEntryCommand;
import net.sberg.elbook.vzdclientcmpts.command.EnumCommand;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.ICommandResultCallbackHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DelDirEntryCommandHandler extends AbstractCommandHandler {

    private Logger log = LoggerFactory.getLogger(DelDirEntryCommandHandler.class);
    private DirectoryEntryAdministrationApi directoryEntryAdministrationApiV1_12_7;
    private de.gematik.vzd.api.V1_12_6.DirectoryEntryAdministrationApi directoryEntryAdministrationApiV1_12_6;

    public DelDirEntryCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        if (client.getTiVZDProperties().getApiVersion().equals(TiVZDProperties.API_VERSION_V1_12_7)) {
            directoryEntryAdministrationApiV1_12_7 = new DirectoryEntryAdministrationApi(client);
        }
        else if (client.getTiVZDProperties().getApiVersion().equals(TiVZDProperties.API_VERSION_V1_12_6)) {
            directoryEntryAdministrationApiV1_12_6 = new de.gematik.vzd.api.V1_12_6.DirectoryEntryAdministrationApi(client);
        }
        else {
            throw new IllegalStateException("unknown api version: "+client.getTiVZDProperties().getInfoObject().getVersion());
        }
    }

    @Override
    public boolean checkValidation() {
        if (!(command instanceof DelDirEntryCommand)) {
            throw new IllegalStateException("command not type of DelDirEntryCommand");
        }
        if (StringUtils.isBlank(command.getUid())) {
            log.error(
                "Missing argument -> uid for command " + command.command());
            return false;
        }

        return true;
    }

    public void executeCommand() {
        try {
            client.validateToken();

            ResponseEntity<Object> response = null;
            if (directoryEntryAdministrationApiV1_12_7 != null) {
                response = directoryEntryAdministrationApiV1_12_7.deleteDirectoryEntryWithHttpInfo(command.getUid());
            }
            if (directoryEntryAdministrationApiV1_12_6 != null) {
                response = directoryEntryAdministrationApiV1_12_6.deleteDirectoryEntryWithHttpInfo(command.getUid());
            }

            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("Delete directory entry execution successful operated for " + command.getUid());
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(true, ""));
            }
            else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                log.debug(command.getUid() + " could not be found");
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, command.getUid() + " could not be found"));
            }
            else {
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "Delete directory entry execution failed. Response-Status was: "
                        + response.getStatusCode() + "\n"
                        + command.getUid()));
            }
        }
        catch (Exception e) {
            log.error("error on executing command: " + EnumCommand.DEL_DIR_ENTRY.getSpecName() + "\n" + command.getUid(), e);
            commandResultCallbackHandler.handle(command, e);
        }
    }

}
