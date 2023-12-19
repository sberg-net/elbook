package net.sberg.elbook.vzdclientcmpts.commandhandler;

import de.gematik.vzd.api.DirectoryEntryAdministrationApi;
import de.gematik.vzd.model.BaseDirectoryEntry;
import de.gematik.vzd.model.CreateDirectoryEntry;
import de.gematik.vzd.model.DistinguishedName;
import de.gematik.vzd.model.UserCertificate;
import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.command.*;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.ICommandResultCallbackHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class AddDirEntryCommandHandler extends AbstractCommandHandler {

    private final DirectoryEntryAdministrationApi directoryEntryAdministrationApi;
    private Logger log = LoggerFactory.getLogger(AddDirEntryCommandHandler.class);

    public AddDirEntryCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        directoryEntryAdministrationApi = new DirectoryEntryAdministrationApi(client);
    }

    @Override
    public boolean checkValidation() {
        if (!(command instanceof AddDirEntryCommand)) {
            throw new IllegalStateException("command not type of AddDirCertCommand");
        }

        AddDirEntryCommand addDirEntryCommand = (AddDirEntryCommand)command;
        if (StringUtils.isBlank(addDirEntryCommand.getTelematikId())) {
            log.error("telematikid not set");
            return false;
        }

        return true;
    }

    public void executeCommand() {
        try {
            client.validateToken();

            if (CommandHandlerUtils.isEntryPresent(null, command.getTelematikId(), client, log)) {
                throw new IllegalStateException("entry with the telematikId is present: "+command.getTelematikId());
            }

            AddDirEntryCommand addDirEntryCommand = (AddDirEntryCommand)command;

            CreateDirectoryEntry createDirectoryEntry = new CreateDirectoryEntry();
            createDirectoryEntry.setDirectoryEntryBase(convert(addDirEntryCommand));

            if (addDirEntryCommand.getCertContents() != null && !addDirEntryCommand.getCertContents().isEmpty()) {
                createDirectoryEntry.setUserCertificates(new ArrayList<>());
                for (Iterator<String> iterator = addDirEntryCommand.getCertContents().iterator(); iterator.hasNext(); ) {
                    String content = iterator.next().replaceAll("[\n\r]", "").trim();
                    UserCertificate userCertificate = new UserCertificate();
                    userCertificate.setTelematikID(command.getTelematikId());
                    userCertificate.setUserCertificate(content);
                    createDirectoryEntry.getUserCertificates().add(userCertificate);
                }
            }

            ResponseEntity<DistinguishedName> response = directoryEntryAdministrationApi.addDirectoryEntryWithHttpInfo(createDirectoryEntry);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.debug("Add directory entry execution successful operated\n" + response.getBody());
                commandResultCallbackHandler.handleDistinguishedName(command, response.getBody());
            }
            else {
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "Add directory entry execution failed. Response-Status was: " + response
                        .getStatusCode() + "\n" + command.getUid()));
            }
        }
        catch (RestClientResponseException e) {
            AbstractCommandResultCallbackHandler.ServerResult serverResult = new AbstractCommandResultCallbackHandler.ServerResult(e.getStatusCode().value(), e.getResponseHeaders());
            commandResultCallbackHandler.handle(command, serverResult);
            log.error("error on executing command: " + EnumCommand.ADD_DIR_ENTRY.getSpecName() + "\n" + command.getUid() + "\n" + serverResult, e);
            commandResultCallbackHandler.handle(command, e);
        }
        catch (Exception e) {
            log.error("error on executing command: " + EnumCommand.ADD_DIR_ENTRY.getSpecName() + "\n" + command.getUid(), e);
            commandResultCallbackHandler.handle(command, e);
        }
    }

    private BaseDirectoryEntry convert(AddDirEntryCommand addDirEntryCommand) {
        BaseDirectoryEntry baseDirectoryEntry = new BaseDirectoryEntry();

        //id attributes
        baseDirectoryEntry.setTelematikID(addDirEntryCommand.getTelematikId());
        baseDirectoryEntry.setDomainID(addDirEntryCommand.getDomainId());

        //name attributes
        baseDirectoryEntry.setDisplayName(addDirEntryCommand.getDisplayName());
        baseDirectoryEntry.setTitle(addDirEntryCommand.getTitle());
        baseDirectoryEntry.setOrganization(addDirEntryCommand.getOrganization());
        baseDirectoryEntry.setOtherName(addDirEntryCommand.getOtherName());
        baseDirectoryEntry.setSn(addDirEntryCommand.getSn());
        baseDirectoryEntry.setCn(addDirEntryCommand.getCn());
        baseDirectoryEntry.setGivenName(addDirEntryCommand.getGivenName());

        //address attributes
        baseDirectoryEntry.setStreetAddress(addDirEntryCommand.getStreetAddress());
        baseDirectoryEntry.setPostalCode(addDirEntryCommand.getPostalCode());
        baseDirectoryEntry.setLocalityName(addDirEntryCommand.getLocalityName());
        baseDirectoryEntry.setStateOrProvinceName((addDirEntryCommand.getStateOrProvinceName() != null && !addDirEntryCommand.getStateOrProvinceName().equals(EnumStateOrProvinceName.UNKNOWN))?addDirEntryCommand.getStateOrProvinceName().getHrText():null);
        baseDirectoryEntry.setCountryCode(addDirEntryCommand.getCountryCode());

        // profession attributes
        baseDirectoryEntry.setSpecialization(addDirEntryCommand.getSpecialization());
        baseDirectoryEntry.setEntryType((addDirEntryCommand.getEntryType() != null && !addDirEntryCommand.getEntryType().equals(EnumEntryType.UNKNOWN))?new ArrayList<>(Arrays.asList(addDirEntryCommand.getEntryType().getId())):null);

        // system attributes
        baseDirectoryEntry.setHolder(addDirEntryCommand.getHolder());
        baseDirectoryEntry.setMaxKOMLEadr(addDirEntryCommand.getMaxKomLeAdr());
        baseDirectoryEntry.setActive(addDirEntryCommand.getActive() != null?addDirEntryCommand.getActive().getDataValue():null);
        baseDirectoryEntry.setMeta(addDirEntryCommand.getMeta());

        return baseDirectoryEntry;
    }

}
