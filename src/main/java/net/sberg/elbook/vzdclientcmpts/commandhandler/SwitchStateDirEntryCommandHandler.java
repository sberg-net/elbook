package net.sberg.elbook.vzdclientcmpts.commandhandler;

import de.gematik.vzd.api.DirectoryEntryAdministrationApi;
import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.EnumCommand;
import net.sberg.elbook.vzdclientcmpts.command.SwitchStateDirEntryCommand;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.ICommandResultCallbackHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;

public class SwitchStateDirEntryCommandHandler extends AbstractCommandHandler {

    private final DirectoryEntryAdministrationApi directoryEntryAdministrationApi;
    private Logger log = LoggerFactory.getLogger(SwitchStateDirEntryCommandHandler.class);

    public SwitchStateDirEntryCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        directoryEntryAdministrationApi = new DirectoryEntryAdministrationApi(client);
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

            ResponseEntity<Object> response = directoryEntryAdministrationApi.stateSwitchDirectoryEntryWithHttpInfo(switchStateDirEntryCommand.getUid(), switchStateDirEntryCommand.getActive());

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
