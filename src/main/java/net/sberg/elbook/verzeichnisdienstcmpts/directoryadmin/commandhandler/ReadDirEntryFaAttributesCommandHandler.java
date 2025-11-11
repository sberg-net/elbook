package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.commandhandler;

import de.gematik.vzd.api.V1_12_8.ApplicationDataAdministrationApi;
import de.gematik.vzd.model.V1_12_8.DirectoryEntry;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.VzdEntryWrapper;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.ClientImpl;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.EnumCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.ReadDirEntryFaAttributesCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.AbstractCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.ICommandResultCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReadDirEntryFaAttributesCommandHandler extends AbstractCommandHandler {

    private static final String CHECK_EMPTY_STRING = "**";

    private Logger log = LoggerFactory.getLogger(ReadDirEntryFaAttributesCommandHandler.class);
    private ApplicationDataAdministrationApi applicationDataAdministrationApi;

    public ReadDirEntryFaAttributesCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        applicationDataAdministrationApi = new ApplicationDataAdministrationApi(client);
    }

    @Override
    public boolean checkValidation() {
        if (!(command instanceof ReadDirEntryFaAttributesCommand)) {
            throw new IllegalStateException("command not type of ReadDirEntryFaAttributesCommand");
        }

        ReadDirEntryFaAttributesCommand readDirEntryFaAttributesCommand = (ReadDirEntryFaAttributesCommand)command;

        boolean check = false;

        List<String> params = new ArrayList<>();

        params.add(readDirEntryFaAttributesCommand.getMail());
        params.add(readDirEntryFaAttributesCommand.getKomLeData());
        params.add(readDirEntryFaAttributesCommand.getKimData());

        for (String parameter : params) {
            if (!org.apache.commons.lang3.StringUtils.isBlank(parameter)) {
                check = true;
            }
        }

        if (!check) {
            log.error("Missing arguments for read: "+command.command());
        }

        return check;
    }

    private String checkOnEmptySearchValue(String searchValue) {
        if (searchValue != null && searchValue.equals(CHECK_EMPTY_STRING)) {
            return "";
        }
        return searchValue;
    }

    public void executeCommand() {
        try {
            client.validateToken();

            ReadDirEntryFaAttributesCommand readDirEntryFaAttributesCommand = (ReadDirEntryFaAttributesCommand)command;

            String mail = checkOnEmptySearchValue(readDirEntryFaAttributesCommand.getMail());
            String komLeData = checkOnEmptySearchValue(readDirEntryFaAttributesCommand.getKomLeData());
            String kimData = checkOnEmptySearchValue(readDirEntryFaAttributesCommand.getKimData());

            ResponseEntity<List<DirectoryEntry>> response =
                    applicationDataAdministrationApi.searchDirectoryFAAttributesWithHttpInfo(mail, komLeData, kimData);
            if (response.getStatusCode() == HttpStatus.OK) {
                if (response.getBody().isEmpty()) {
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS));
                }
                else {
                    for (Iterator<DirectoryEntry> iterator = response.getBody().iterator(); iterator.hasNext(); ) {
                        DirectoryEntry directoryEntry = iterator.next();
                        commandResultCallbackHandler.handleDirectoryEntry(command, new VzdEntryWrapper(directoryEntry));
                    }
                }
            }
            else {
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                log.error("read directory entry execution failed. Response status was: "
                        + response.getStatusCode() + "\n" + command);
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "read directory entry execution failed. Response status was: "
                        + response.getStatusCode() + command));
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
                log.error("error on executing command: " + EnumCommand.READ_DIR_ENTRY.getSpecName() + "\n" + command + "\n" + serverResult, e);
                commandResultCallbackHandler.handle(command, e);
            }
        }
        catch (Exception e) {
            log.error("error on executing command: " + EnumCommand.READ_DIR_ENTRY.getSpecName() + "\n" + command, e);
            commandResultCallbackHandler.handle(command, e);
        }
    }
}
