package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.commandhandler;

import de.gematik.vzd.api.V1_12_8.DirectoryEntrySynchronizationApi;
import de.gematik.vzd.model.V1_12_8.DirectoryEntry;
import de.gematik.vzd.model.V1_12_8.ReadDirectoryEntryforSyncResponse;
import net.sberg.elbook.common.StringUtils;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.VzdEntryWrapper;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.ClientImpl;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.TiVZDProperties;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.*;
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

public class ReadDirSyncEntryCommandHandler extends AbstractCommandHandler {

    private static final String CHECK_EMPTY_STRING = "**";

    private Logger log = LoggerFactory.getLogger(ReadDirSyncEntryCommandHandler.class);
    private DirectoryEntrySynchronizationApi directoryEntrySynchronizationApiV1_12_8;

    public ReadDirSyncEntryCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        if (client.getTiVZDProperties().getApiVersion().equals(TiVZDProperties.API_VERSION_V1_12_8)) {
            directoryEntrySynchronizationApiV1_12_8 = new DirectoryEntrySynchronizationApi(client);
        }
        else {
            throw new IllegalStateException("unknown api version: "+client.getTiVZDProperties().getInfoObject().getVersion());
        }
    }

    @Override
    public boolean checkValidation() {
        if (!(command instanceof ReadDirSyncEntryCommand)) {
            throw new IllegalStateException("command not type of ReadDirEntryCommand");
        }

        ReadDirSyncEntryCommand readDirSyncEntryCommand = (ReadDirSyncEntryCommand)command;

        boolean check = false;

        List<String> params = new ArrayList<>();

        params.add(readDirSyncEntryCommand.getUid());
        params.add(readDirSyncEntryCommand.getTelematikId());
        params.add(readDirSyncEntryCommand.getTelematikIdSubstr());
        params.add(StringUtils.listToString(readDirSyncEntryCommand.getDomainId()));
        params.add(StringUtils.listToString(readDirSyncEntryCommand.getLanr()));
        params.add(readDirSyncEntryCommand.getProvidedBy());

        params.add(readDirSyncEntryCommand.getDisplayName());
        params.add(readDirSyncEntryCommand.getGivenName());
        params.add(readDirSyncEntryCommand.getSn());
        params.add(readDirSyncEntryCommand.getCn());
        params.add(readDirSyncEntryCommand.getTitle());
        params.add(readDirSyncEntryCommand.getOrganization());
        params.add(readDirSyncEntryCommand.getOtherName());

        params.add(readDirSyncEntryCommand.getStreetAddress());
        params.add(readDirSyncEntryCommand.getPostalCode());
        params.add(readDirSyncEntryCommand.getLocalityName());
        params.add(readDirSyncEntryCommand.getCountryCode());
        params.add((readDirSyncEntryCommand.getStateOrProvinceName() != null && !readDirSyncEntryCommand.getStateOrProvinceName().equals(EnumStateOrProvinceName.UNKNOWN))? readDirSyncEntryCommand.getStateOrProvinceName().getHrText():null);

        params.add(StringUtils.listToString(readDirSyncEntryCommand.getSpecialization()));
        params.add(StringUtils.listToString(readDirSyncEntryCommand.getProfessionOid()));
        params.add((readDirSyncEntryCommand.getEntryType() != null && !readDirSyncEntryCommand.getEntryType().equals(EnumEntryType.UNKNOWN))? readDirSyncEntryCommand.getEntryType().getId():null);

        params.add(StringUtils.listToString(readDirSyncEntryCommand.getHolder()));
        params.add(readDirSyncEntryCommand.getPersonalEntry() != null? readDirSyncEntryCommand.getPersonalEntry().getDataValueStr(): null);
        params.add(readDirSyncEntryCommand.getDataFromAuthority() != null? readDirSyncEntryCommand.getDataFromAuthority().getDataValueStr(): null);
        params.add(readDirSyncEntryCommand.getActive() != null? readDirSyncEntryCommand.getActive().getDataValueStr(): null);
        params.add(readDirSyncEntryCommand.getMaxKomLeAdr());
        params.add(StringUtils.listToString(readDirSyncEntryCommand.getMeta()));
        params.add(readDirSyncEntryCommand.getChangeDateTimeFrom());
        params.add(readDirSyncEntryCommand.getChangeDateTimeTo());

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

            ReadDirSyncEntryCommand readDirSyncEntryCommand = (ReadDirSyncEntryCommand)command;
            
            String uid = readDirSyncEntryCommand.getUid();
            String telematikID = readDirSyncEntryCommand.getTelematikId();
            String telematikIDSubstr = readDirSyncEntryCommand.getTelematikIdSubstr();
            String domainID = checkOnEmptySearchValue(!StringUtils.listToString(readDirSyncEntryCommand.getDomainId()).equals("")?StringUtils.listToString(readDirSyncEntryCommand.getDomainId()):null);
            String lanr = checkOnEmptySearchValue(!StringUtils.listToString(readDirSyncEntryCommand.getLanr()).equals("")?StringUtils.listToString(readDirSyncEntryCommand.getLanr()):null);
            String providedBy = readDirSyncEntryCommand.getProvidedBy();

            String givenName = checkOnEmptySearchValue(readDirSyncEntryCommand.getGivenName());
            String sn = checkOnEmptySearchValue(readDirSyncEntryCommand.getSn());
            String cn = checkOnEmptySearchValue(readDirSyncEntryCommand.getCn());
            String displayName = checkOnEmptySearchValue(readDirSyncEntryCommand.getDisplayName());
            String title = checkOnEmptySearchValue(readDirSyncEntryCommand.getTitle());
            String organization = checkOnEmptySearchValue(readDirSyncEntryCommand.getOrganization());
            String otherName = checkOnEmptySearchValue(readDirSyncEntryCommand.getOtherName());
            String countryCode = checkOnEmptySearchValue(readDirSyncEntryCommand.getCountryCode());

            String streetAddress = checkOnEmptySearchValue(readDirSyncEntryCommand.getStreetAddress());
            String postalCode = checkOnEmptySearchValue(readDirSyncEntryCommand.getPostalCode());
            String localityName = checkOnEmptySearchValue(readDirSyncEntryCommand.getLocalityName());
            String stateOrProvinceName = (readDirSyncEntryCommand.getStateOrProvinceName() != null && !readDirSyncEntryCommand.getStateOrProvinceName().equals(EnumStateOrProvinceName.UNKNOWN))? readDirSyncEntryCommand.getStateOrProvinceName().getHrText():null;

            String specialization = checkOnEmptySearchValue(!StringUtils.listToString(readDirSyncEntryCommand.getSpecialization()).equals("")?StringUtils.listToString(readDirSyncEntryCommand.getSpecialization()):null);
            String professionOID = checkOnEmptySearchValue(!StringUtils.listToString(readDirSyncEntryCommand.getProfessionOid()).equals("")?StringUtils.listToString(readDirSyncEntryCommand.getProfessionOid()):null);
            String entryType = (readDirSyncEntryCommand.getEntryType() != null && !readDirSyncEntryCommand.getEntryType().equals(EnumEntryType.UNKNOWN))? readDirSyncEntryCommand.getEntryType().getId():null;

            String personalEntry = readDirSyncEntryCommand.getPersonalEntry() != null? readDirSyncEntryCommand.getPersonalEntry().getDataValueStr(): null;
            String dataFromAuthority = readDirSyncEntryCommand.getDataFromAuthority() != null? readDirSyncEntryCommand.getDataFromAuthority().getDataValueStr(): null;
            String holder = checkOnEmptySearchValue(!StringUtils.listToString(readDirSyncEntryCommand.getHolder()).equals("")?StringUtils.listToString(readDirSyncEntryCommand.getHolder()):null);
            String maxKomLeAdr = checkOnEmptySearchValue(readDirSyncEntryCommand.getMaxKomLeAdr());
            Boolean active = readDirSyncEntryCommand.getActive() != null?readDirSyncEntryCommand.getActive().getDataValue():null;
            Boolean baseEntryOnly = readDirSyncEntryCommand.getBaseEntryOnly();
            String meta = !StringUtils.listToString(readDirSyncEntryCommand.getMeta()).equals("")?StringUtils.listToString(readDirSyncEntryCommand.getMeta()):null;
            String changeDateTimeFrom = checkOnEmptySearchValue(readDirSyncEntryCommand.getChangeDateTimeFrom());
            String changeDateTimeTo = checkOnEmptySearchValue(readDirSyncEntryCommand.getChangeDateTimeTo());

            PagingInfo pagingInfo = readDirSyncEntryCommand.getPagingInfo();

            boolean searchResultsAvailable = false;
            int size = 0;
            while (true) {
                if (directoryEntrySynchronizationApiV1_12_8 != null) {
                    ResponseEntity response = null;
                    boolean pagingMode = true;
                    if (pagingInfo == null || pagingInfo.getPagingCookie() == null) {
                        response = directoryEntrySynchronizationApiV1_12_8.readDirectoryEntryForSyncWithHttpInfo(uid, givenName, sn, cn,
                            displayName, streetAddress, postalCode, countryCode, localityName, stateOrProvinceName, title,
                            organization, otherName, telematikID, telematikIDSubstr, lanr,  providedBy, specialization, domainID, holder, personalEntry,
                            dataFromAuthority, professionOID, entryType, maxKomLeAdr, changeDateTimeFrom, changeDateTimeTo, baseEntryOnly, active, meta);
                        pagingMode = false;
                    } else {
                        response = directoryEntrySynchronizationApiV1_12_8.readDirectoryEntryForSyncPagingWithHttpInfo(uid, givenName, sn, cn,
                            displayName, streetAddress, postalCode, countryCode, localityName, stateOrProvinceName, title,
                            organization, otherName, telematikID, telematikIDSubstr, null, null, specialization, domainID, holder, personalEntry,
                            dataFromAuthority, professionOID, entryType, maxKomLeAdr, changeDateTimeFrom, changeDateTimeTo, baseEntryOnly, active, meta, pagingInfo.getPagingSize(), pagingInfo.getPagingCookie());
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
                        } else {
                            directoryEntries = (List<DirectoryEntry>) response.getBody();
                        }
                        if (directoryEntries.isEmpty()) {
                            if (!searchResultsAvailable) {
                                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS));
                            }
                            break;
                        } else {
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
                    } else {
                        commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                        log.error("read directory entry execution failed. Response status was: "
                                + response.getStatusCode() + "\n" + command);
                        commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "read directory entry execution failed. Response status was: "
                                + response.getStatusCode() + "\n" + command));
                        break;
                    }
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
