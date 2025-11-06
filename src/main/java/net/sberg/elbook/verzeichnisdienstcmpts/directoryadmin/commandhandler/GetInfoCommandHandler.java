package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.commandhandler;

import de.gematik.vzd.api.V1_12_7.GetInfoDirectoryEntryAdministrationApi;
import de.gematik.vzd.model.V1_12_7.InfoObject;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.ClientImpl;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.EnumCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.AbstractCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.ICommandResultCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class GetInfoCommandHandler extends AbstractCommandHandler {

    private Logger log = LoggerFactory.getLogger(GetInfoCommandHandler.class);
    private GetInfoDirectoryEntryAdministrationApi getInfoDirectoryEntryAdministrationApi;

    public GetInfoCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        getInfoDirectoryEntryAdministrationApi = new GetInfoDirectoryEntryAdministrationApi(client);
    }

    @Override
    public boolean checkValidation() {
        return true;
    }

    @Override
    public void executeCommand() {
        try {
            client.validateToken();

            ResponseEntity<InfoObject> response = getInfoDirectoryEntryAdministrationApi.getInfoWithHttpInfo();

            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("Get info execution successful operated\n" + response.getBody());
                commandResultCallbackHandler.handleInfoObject(command, response.getBody());
            } else {
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "get info execution failed. Response-Status was: " + response.getStatusCode()));
            }
        }
        catch (Exception e) {
            log.error("error on executing command: " + EnumCommand.GET_INFO.getSpecName(), e);
            commandResultCallbackHandler.handle(command, e);
        }
    }
}
