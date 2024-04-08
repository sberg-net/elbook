package net.sberg.elbook.vzdclientcmpts.commandhandler;

import de.gematik.vzd.api.V1_12_6.DirectoryEntrySynchronizationApi;
import de.gematik.vzd.model.V1_12_6.DirectoryEntry;
import de.gematik.vzd.model.V1_12_6.ReadDirectoryEntryforSyncResponse;
import net.sberg.elbook.verzeichnisdienstcmpts.VzdEntryWrapper;
import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.EnumCommand;
import net.sberg.elbook.vzdclientcmpts.command.PagingInfo;
import net.sberg.elbook.vzdclientcmpts.command.ReadDirSyncEntryFaAttributesCommand;
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

public class ReadDirSyncEntryFaAttributesCommandHandler extends AbstractCommandHandler {

    private static final String CHECK_EMPTY_STRING = "**";

    private Logger log = LoggerFactory.getLogger(ReadDirSyncEntryFaAttributesCommandHandler.class);
    private DirectoryEntrySynchronizationApi directoryEntrySynchronizationApi;

    public ReadDirSyncEntryFaAttributesCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        directoryEntrySynchronizationApi = new DirectoryEntrySynchronizationApi(client);
    }

    @Override
    public boolean checkValidation() {
        if (!(command instanceof ReadDirSyncEntryFaAttributesCommand)) {
            throw new IllegalStateException("command not type of ReadDirSyncEntryFaAttributesCommand");
        }

        ReadDirSyncEntryFaAttributesCommand readDirSyncEntryFaAttributesCommand = (ReadDirSyncEntryFaAttributesCommand)command;

        boolean check = false;

        List<String> params = new ArrayList<>();

        params.add(readDirSyncEntryFaAttributesCommand.getMail());
        params.add(readDirSyncEntryFaAttributesCommand.getKomLeData());
        params.add(readDirSyncEntryFaAttributesCommand.getKimData());

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

            ReadDirSyncEntryFaAttributesCommand readDirSyncEntryFaAttributesCommand = (ReadDirSyncEntryFaAttributesCommand)command;
            
            String mail = checkOnEmptySearchValue(readDirSyncEntryFaAttributesCommand.getMail());
            String komLeData = checkOnEmptySearchValue(readDirSyncEntryFaAttributesCommand.getKomLeData());
            String kimData = checkOnEmptySearchValue(readDirSyncEntryFaAttributesCommand.getKimData());

            PagingInfo pagingInfo = readDirSyncEntryFaAttributesCommand.getPagingInfo();

            boolean searchResultsAvailable = false;
            int size = 0;
            while (true) {
                ResponseEntity response = null;
                boolean pagingMode = true;
                if (pagingInfo == null || pagingInfo.getPagingCookie() == null) {
                    response = directoryEntrySynchronizationApi.searchDirectoryFAAttributesForSyncPagingWithHttpInfo(mail, komLeData, kimData, null, null);
                    pagingMode = false;
                }
                else {
                    response = directoryEntrySynchronizationApi.searchDirectoryFAAttributesForSyncPagingWithHttpInfo(mail, komLeData, kimData, pagingInfo.getPagingSize(), pagingInfo.getPagingCookie());
                }
                if (response.getStatusCode() == HttpStatus.OK) {
                    List<DirectoryEntry> directoryEntries = null;
                    if (response.getBody() instanceof ReadDirectoryEntryforSyncResponse) {
                        directoryEntries = ((ReadDirectoryEntryforSyncResponse) response.getBody()).getDirectoryEntries();
                        String pagingCookie = ((ReadDirectoryEntryforSyncResponse) response.getBody()).getSearchControlValue().getCookie();
                        int pagingSize = ((ReadDirectoryEntryforSyncResponse) response.getBody()).getSearchControlValue().getSize();
                        if (pagingInfo == null) {
                            pagingInfo = new PagingInfo();
                        }
                        pagingInfo.setPagingSize(pagingSize);
                        pagingInfo.setPagingCookie(pagingCookie);
                        if (pagingCookie.equals("")) {
                            pagingMode = false;
                        }
                        if (pagingInfo.isOneTimePaging()) {
                            pagingMode = false;
                            commandResultCallbackHandler.handlePagingInfo(command, pagingInfo);
                        }
                    }
                    else {
                        directoryEntries = (List<DirectoryEntry>) response.getBody();
                    }
                    if (directoryEntries.isEmpty()) {
                        if (!searchResultsAvailable) {
                            commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS));
                        }
                        break;
                    }
                    else {
                        searchResultsAvailable = true;
                        for (Iterator<DirectoryEntry> iterator = directoryEntries.iterator(); iterator.hasNext(); ) {
                            DirectoryEntry directoryEntry = iterator.next();
                            commandResultCallbackHandler.handleDirectoryEntry(command, new VzdEntryWrapper(directoryEntry));
                            size++;
                            if (pagingInfo.getMaxSize() != -1 && size > pagingInfo.getMaxSize()) {
                                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.TOO_MANY_RESULTS));
                                pagingMode = false;
                                break;
                            }
                        }
                    }
                    if (!pagingMode) {
                        break;
                    }
                }
                else {
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                    log.error("read directory entry execution failed. Response status was: "
                            + response.getStatusCode() + "\n" + command);
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "read directory entry execution failed. Response status was: "
                            + response.getStatusCode() + "\n" + command));
                    break;
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
                log.error("error on executing command: " + EnumCommand.READ_DIR_SYNC_ENTRY.getSpecName() + "\n" + command + "\n" + serverResult, e);
                commandResultCallbackHandler.handle(command, e);
            }
        }
        catch (Exception e) {
            log.error("error on executing command: " + EnumCommand.READ_DIR_SYNC_ENTRY.getSpecName() + "\n" + command, e);
            commandResultCallbackHandler.handle(command, e);
        }
    }
}
