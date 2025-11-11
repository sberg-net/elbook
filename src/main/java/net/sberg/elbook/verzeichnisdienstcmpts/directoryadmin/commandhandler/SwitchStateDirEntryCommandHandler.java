package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.commandhandler;

import de.gematik.vzd.api.V1_12_8.DirectoryEntryAdministrationApi;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.ClientImpl;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.TiVZDProperties;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.EnumCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.SwitchStateDirEntryCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.AbstractCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.ICommandResultCallbackHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;

public class SwitchStateDirEntryCommandHandler extends AbstractCommandHandler {

    private DirectoryEntryAdministrationApi directoryEntryAdministrationApiV1_12_8;
    private Logger log = LoggerFactory.getLogger(SwitchStateDirEntryCommandHandler.class);

    public SwitchStateDirEntryCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        if (client.getTiVZDProperties().getApiVersion().equals(TiVZDProperties.API_VERSION_V1_12_8)) {
            directoryEntryAdministrationApiV1_12_8 = new DirectoryEntryAdministrationApi(client);
        }
        else {
            throw new IllegalStateException("unknown api version: "+client.getTiVZDProperties().getInfoObject().getVersion());
        }
    }

    @Override
    public boolean checkValidation() {
        if (!(command instanceof SwitchStateDirEntryCommand)) {
            throw new IllegalStateException("command not type of SwitchStateDirEntryCommand");
        }

        SwitchStateDirEntryCommand switchStateDirEntryCommand = (SwitchStateDirEntryCommand)command;

        if (switchStateDirEntryCommand.getActive() == null) {
            log.error("Missing argument -> active for command " + command.command());
            return false;
        }

        if (StringUtils.isBlank(command.getUid())) {
            log.error("Missing argument -> uid for command " + command.command());
            return false;
        }

        return true;
    }

    public void executeCommand() {
        try {
            client.validateToken();

            SwitchStateDirEntryCommand switchStateDirEntryCommand = (SwitchStateDirEntryCommand)command;

            ResponseEntity<Object> response = null;
            if (directoryEntryAdministrationApiV1_12_8 != null) {
                response = directoryEntryAdministrationApiV1_12_8.stateSwitchDirectoryEntryWithHttpInfo(switchStateDirEntryCommand.getUid(), switchStateDirEntryCommand.getActive());
            }

            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("Switch state directory entry execution successful operated\n" + response.getBody());
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(true, "Switch state directory entry execution succesful"));
            }
            else {
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "Switch state directory entry execution failed. Response-Status was: " + response
                        .getStatusCode() + "\n" + command));
            }
        }
        catch (RestClientResponseException e) {
            AbstractCommandResultCallbackHandler.ServerResult serverResult = new AbstractCommandResultCallbackHandler.ServerResult(e.getStatusCode().value(), e.getResponseHeaders());
            commandResultCallbackHandler.handle(command, serverResult);
            log.error("error on executing command: " + EnumCommand.SWITCH_STATE_DIR_ENTRY.getSpecName() + "\n" + command + "\n" + serverResult, e);
            commandResultCallbackHandler.handle(command, e);
        }
        catch (Exception e) {
            log.error("error on executing command: " + EnumCommand.SWITCH_STATE_DIR_ENTRY.getSpecName() + "\n" + command, e);
            commandResultCallbackHandler.handle(command, e);
        }
    }

}
