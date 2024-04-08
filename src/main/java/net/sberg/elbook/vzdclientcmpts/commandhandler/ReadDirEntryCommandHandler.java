package net.sberg.elbook.vzdclientcmpts.commandhandler;

import de.gematik.vzd.api.V1_9_5.DirectoryEntryAdministrationApi;
import de.gematik.vzd.model.V1_9_5.DirectoryEntry;
import net.sberg.elbook.common.StringUtils;
import net.sberg.elbook.verzeichnisdienstcmpts.VzdEntryWrapper;
import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.*;
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

public class ReadDirEntryCommandHandler extends AbstractCommandHandler {

    private static final String CHECK_EMPTY_STRING = "**";

    private Logger log = LoggerFactory.getLogger(ReadDirEntryCommandHandler.class);
    private DirectoryEntryAdministrationApi directoryEntryAdministrationApiV1_9_5;
    private de.gematik.vzd.api.V1_12_6.DirectoryEntryAdministrationApi directoryEntryAdministrationApiV1_12_6;

    public ReadDirEntryCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        if (client.getTiVZDProperties().getApiVersion().equals(TiVZDProperties.API_VERSION_V1_9_5)) {
            directoryEntryAdministrationApiV1_9_5 = new DirectoryEntryAdministrationApi(client);
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
        if (!(command instanceof ReadDirEntryCommand)) {
            throw new IllegalStateException("command not type of ReadDirEntryCommand");
        }

        ReadDirEntryCommand readDirEntryCommand = (ReadDirEntryCommand)command;

        boolean check = false;

        List<String> params = new ArrayList<>();

        params.add(readDirEntryCommand.getUid());
        params.add(readDirEntryCommand.getTelematikId());
        params.add(readDirEntryCommand.getTelematikIdSubstr());
        params.add(StringUtils.listToString(readDirEntryCommand.getDomainId()));

        params.add(readDirEntryCommand.getDisplayName());
        params.add(readDirEntryCommand.getGivenName());
        params.add(readDirEntryCommand.getSn());
        params.add(readDirEntryCommand.getCn());
        params.add(readDirEntryCommand.getTitle());
        params.add(readDirEntryCommand.getOrganization());
        params.add(readDirEntryCommand.getOtherName());

        params.add(readDirEntryCommand.getStreetAddress());
        params.add(readDirEntryCommand.getPostalCode());
        params.add(readDirEntryCommand.getLocalityName());
        params.add(readDirEntryCommand.getCountryCode());
        params.add((readDirEntryCommand.getStateOrProvinceName() != null && !readDirEntryCommand.getStateOrProvinceName().equals(EnumStateOrProvinceName.UNKNOWN))? readDirEntryCommand.getStateOrProvinceName().getHrText():null);

        params.add(StringUtils.listToString(readDirEntryCommand.getSpecialization()));
        params.add(StringUtils.listToString(readDirEntryCommand.getProfessionOid()));
        params.add((readDirEntryCommand.getEntryType() != null && !readDirEntryCommand.getEntryType().equals(EnumEntryType.UNKNOWN))? readDirEntryCommand.getEntryType().getId():null);

        params.add(StringUtils.listToString(readDirEntryCommand.getHolder()));
        params.add(readDirEntryCommand.getPersonalEntry() != null? readDirEntryCommand.getPersonalEntry().getDataValueStr(): null);
        params.add(readDirEntryCommand.getDataFromAuthority() != null? readDirEntryCommand.getDataFromAuthority().getDataValueStr(): null);
        params.add(readDirEntryCommand.getActive() != null? readDirEntryCommand.getActive().getDataValueStr(): null);
        params.add(readDirEntryCommand.getMaxKomLeAdr());
        params.add(StringUtils.listToString(readDirEntryCommand.getMeta()));
        params.add(readDirEntryCommand.getChangeDateTimeFrom());
        params.add(readDirEntryCommand.getChangeDateTimeTo());

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

            ReadDirEntryCommand readDirEntryCommand = (ReadDirEntryCommand)command;

            String uid = readDirEntryCommand.getUid();
            String telematikID = readDirEntryCommand.getTelematikId();
            String telematikIDSubstr = readDirEntryCommand.getTelematikIdSubstr();
            String domainID = checkOnEmptySearchValue(!StringUtils.listToString(readDirEntryCommand.getDomainId()).equals("")?StringUtils.listToString(readDirEntryCommand.getDomainId()):null);

            String givenName = checkOnEmptySearchValue(readDirEntryCommand.getGivenName());
            String sn = checkOnEmptySearchValue(readDirEntryCommand.getSn());
            String cn = checkOnEmptySearchValue(readDirEntryCommand.getCn());
            String displayName = checkOnEmptySearchValue(readDirEntryCommand.getDisplayName());
            String title = checkOnEmptySearchValue(readDirEntryCommand.getTitle());
            String organization = checkOnEmptySearchValue(readDirEntryCommand.getOrganization());
            String otherName = checkOnEmptySearchValue(readDirEntryCommand.getOtherName());
            String countryCode = checkOnEmptySearchValue(readDirEntryCommand.getCountryCode());

            String streetAddress = checkOnEmptySearchValue(readDirEntryCommand.getStreetAddress());
            String postalCode = checkOnEmptySearchValue(readDirEntryCommand.getPostalCode());
            String localityName = checkOnEmptySearchValue(readDirEntryCommand.getLocalityName());
            String stateOrProvinceName = (readDirEntryCommand.getStateOrProvinceName() != null && !readDirEntryCommand.getStateOrProvinceName().equals(EnumStateOrProvinceName.UNKNOWN))? readDirEntryCommand.getStateOrProvinceName().getHrText():null;

            String specialization = checkOnEmptySearchValue(!StringUtils.listToString(readDirEntryCommand.getSpecialization()).equals("")?StringUtils.listToString(readDirEntryCommand.getSpecialization()):null);
            String professionOID = checkOnEmptySearchValue(!StringUtils.listToString(readDirEntryCommand.getProfessionOid()).equals("")?StringUtils.listToString(readDirEntryCommand.getProfessionOid()):null);
            String entryType = (readDirEntryCommand.getEntryType() != null && !readDirEntryCommand.getEntryType().equals(EnumEntryType.UNKNOWN))? readDirEntryCommand.getEntryType().getId():null;

            String personalEntry = readDirEntryCommand.getPersonalEntry() != null? readDirEntryCommand.getPersonalEntry().getDataValueStr(): null;
            String dataFromAuthority = readDirEntryCommand.getDataFromAuthority() != null? readDirEntryCommand.getDataFromAuthority().getDataValueStr(): null;
            String holder = checkOnEmptySearchValue(!StringUtils.listToString(readDirEntryCommand.getHolder()).equals("")?StringUtils.listToString(readDirEntryCommand.getHolder()):null);
            String maxKomLeAdr = checkOnEmptySearchValue(readDirEntryCommand.getMaxKomLeAdr());
            Boolean active = readDirEntryCommand.getActive() != null?readDirEntryCommand.getActive().getDataValue():null;
            Boolean baseEntryOnly = readDirEntryCommand.getBaseEntryOnly();
            String meta = !StringUtils.listToString(readDirEntryCommand.getMeta()).equals("")?StringUtils.listToString(readDirEntryCommand.getMeta()):null;
            String changeDateTimeFrom = checkOnEmptySearchValue(readDirEntryCommand.getChangeDateTimeFrom());
            String changeDateTimeTo = checkOnEmptySearchValue(readDirEntryCommand.getChangeDateTimeTo());

            if (directoryEntryAdministrationApiV1_9_5 != null) {
                ResponseEntity<List<DirectoryEntry>> response =
                    directoryEntryAdministrationApiV1_9_5.readDirectoryEntryWithHttpInfo(uid, givenName, sn, cn,
                        displayName, streetAddress, postalCode, countryCode, localityName, stateOrProvinceName, title,
                        organization, otherName, telematikID, telematikIDSubstr, specialization, domainID, holder, personalEntry,
                        dataFromAuthority, professionOID, entryType, maxKomLeAdr, changeDateTimeFrom, changeDateTimeTo, baseEntryOnly, active, meta);
                if (response.getStatusCode() == HttpStatus.OK) {
                    if (response.getBody().isEmpty()) {
                        commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS));
                    } else {
                        for (Iterator<DirectoryEntry> iterator = response.getBody().iterator(); iterator.hasNext(); ) {
                            DirectoryEntry directoryEntry = iterator.next();
                            commandResultCallbackHandler.handleDirectoryEntry(command, new VzdEntryWrapper(directoryEntry));
                        }
                    }
                } else {
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                    log.error("read directory entry execution failed. Response status was: "
                            + response.getStatusCode() + "\n" + command);
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "read directory entry execution failed. Response status was: "
                            + response.getStatusCode() + command));
                }
            }
            else if (directoryEntryAdministrationApiV1_12_6 != null) {
                ResponseEntity<List<de.gematik.vzd.model.V1_12_6.DirectoryEntry>> response =
                    directoryEntryAdministrationApiV1_12_6.readDirectoryEntryWithHttpInfo(uid, givenName, sn, cn,
                        displayName, streetAddress, postalCode, countryCode, localityName, stateOrProvinceName, title,
                        organization, otherName, telematikID, telematikIDSubstr, readDirEntryCommand.getProvidedBy(), specialization, domainID, holder, personalEntry,
                        dataFromAuthority, professionOID, entryType, maxKomLeAdr, changeDateTimeFrom, changeDateTimeTo, baseEntryOnly, active, meta);
                if (response.getStatusCode() == HttpStatus.OK) {
                    if (response.getBody().isEmpty()) {
                        commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS));
                    } else {
                        for (Iterator<de.gematik.vzd.model.V1_12_6.DirectoryEntry> iterator = response.getBody().iterator(); iterator.hasNext(); ) {
                            de.gematik.vzd.model.V1_12_6.DirectoryEntry directoryEntry = iterator.next();
                            commandResultCallbackHandler.handleDirectoryEntry(command, new VzdEntryWrapper(directoryEntry));
                        }
                    }
                } else {
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                    log.error("read directory entry execution failed. Response status was: "
                            + response.getStatusCode() + "\n" + command);
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "read directory entry execution failed. Response status was: "
                            + response.getStatusCode() + command));
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
