package net.sberg.elbook.vzdclientcmpts.commandhandler;

import de.gematik.vzd.api.V1_9_5.DirectoryEntryAdministrationApi;
import de.gematik.vzd.model.V1_9_5.BaseDirectoryEntry;
import de.gematik.vzd.model.V1_9_5.DistinguishedName;
import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
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

public class ModDirEntryCommandHandler extends AbstractCommandHandler {

    private Logger log = LoggerFactory.getLogger(ModDirEntryCommandHandler.class);
    private DirectoryEntryAdministrationApi directoryEntryAdministrationApiV1_9_5;
    private de.gematik.vzd.api.V1_12_6.DirectoryEntryAdministrationApi directoryEntryAdministrationApiV1_12_6;

    public ModDirEntryCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
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
        if (!(command instanceof ModDirEntryCommand)) {
            throw new IllegalStateException("command not type of ModDirEntryCommand");
        }

        if (StringUtils.isBlank(command.getUid())) {
            log.error(
                    "Missing argument -> uid for command " + command.command());
            return false;
        }

        if (StringUtils.isBlank(command.getTelematikId())) {
            log.error(
                    "Missing argument -> telematikid for command " + command.command() + " - "+command.getTelematikId());
            return false;
        }
        
        return true;
    }

    public void executeCommand() {
        try {
            client.validateToken();

            ModDirEntryCommand modDirEntryCommand = (ModDirEntryCommand)command;

            if (directoryEntryAdministrationApiV1_9_5 != null) {
                BaseDirectoryEntry baseDirectoryEntry = convertV1_9_5(modDirEntryCommand);

                if (!CommandHandlerUtils.isEntryPresent(null, command.getTelematikId(), client, log)) {
                    throw new IllegalStateException("entry with the telematikId is not present: " + command.getTelematikId());
                }

                ResponseEntity<DistinguishedName> response = directoryEntryAdministrationApiV1_9_5.modifyDirectoryEntryWithHttpInfo(command.getUid(), baseDirectoryEntry);
                if (response.getStatusCode() == HttpStatus.OK) {
                    log.debug("Modify directory entry execution successful operated\n" + response.getBody());
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(true, ""));
                } else {
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "modify directory entry execution failed. Response-Status was: "
                            + response.getStatusCode() + "\n"
                            + baseDirectoryEntry));
                }
            }
            if (directoryEntryAdministrationApiV1_12_6 != null) {
                de.gematik.vzd.model.V1_12_6.BaseDirectoryEntry baseDirectoryEntry = convertV1_12_6(modDirEntryCommand);

                if (!CommandHandlerUtils.isEntryPresent(null, command.getTelematikId(), client, log)) {
                    throw new IllegalStateException("entry with the telematikId is not present: " + command.getTelematikId());
                }

                ResponseEntity<de.gematik.vzd.model.V1_12_6.DistinguishedName> response = directoryEntryAdministrationApiV1_12_6.modifyDirectoryEntryWithHttpInfo(command.getUid(), baseDirectoryEntry);
                if (response.getStatusCode() == HttpStatus.OK) {
                    log.debug("Modify directory entry execution successful operated\n" + response.getBody());
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(true, ""));
                } else {
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "modify directory entry execution failed. Response-Status was: "
                            + response.getStatusCode() + "\n"
                            + baseDirectoryEntry));
                }
            }
        }
        catch (RestClientResponseException e) {
            AbstractCommandResultCallbackHandler.ServerResult serverResult = new AbstractCommandResultCallbackHandler.ServerResult(e.getStatusCode().value(), e.getResponseHeaders());
            commandResultCallbackHandler.handle(command, serverResult);
            log.error("error on executing command: " + EnumCommand.MOD_DIR_ENTRY.getSpecName() + "\n" + command.getUid() + "\n" + serverResult, e);
            commandResultCallbackHandler.handle(command, e);
        }
        catch (Exception e) {
            log.error("error on executing command: " + EnumCommand.MOD_DIR_ENTRY.getSpecName() + "\n" + command.getUid(), e);
            commandResultCallbackHandler.handle(command, e);
        }
    }

    private BaseDirectoryEntry convertV1_9_5(ModDirEntryCommand modDirEntryCommand) {
        BaseDirectoryEntry baseDirectoryEntry = new BaseDirectoryEntry();

        //id attributes
        baseDirectoryEntry.setTelematikID(modDirEntryCommand.getTelematikId());
        baseDirectoryEntry.setDomainID(modDirEntryCommand.getDomainId());

        //name attributes
        baseDirectoryEntry.setDisplayName(modDirEntryCommand.getDisplayName());
        baseDirectoryEntry.setTitle(modDirEntryCommand.getTitle());
        baseDirectoryEntry.setOrganization(modDirEntryCommand.getOrganization());
        baseDirectoryEntry.setOtherName(modDirEntryCommand.getOtherName());
        baseDirectoryEntry.setSn(modDirEntryCommand.getSn());
        baseDirectoryEntry.setCn(modDirEntryCommand.getCn());
        baseDirectoryEntry.setGivenName(modDirEntryCommand.getGivenName());

        //address attributes
        baseDirectoryEntry.setStreetAddress(modDirEntryCommand.getStreetAddress());
        baseDirectoryEntry.setPostalCode(modDirEntryCommand.getPostalCode());
        baseDirectoryEntry.setLocalityName(modDirEntryCommand.getLocalityName());
        baseDirectoryEntry.setStateOrProvinceName((modDirEntryCommand.getStateOrProvinceName() != null && !modDirEntryCommand.getStateOrProvinceName().equals(EnumStateOrProvinceName.UNKNOWN))?modDirEntryCommand.getStateOrProvinceName().getHrText():null);
        baseDirectoryEntry.setCountryCode(modDirEntryCommand.getCountryCode());

        // profession attributes
        baseDirectoryEntry.setSpecialization(modDirEntryCommand.getSpecialization());
        baseDirectoryEntry.setEntryType((modDirEntryCommand.getEntryType() != null && !modDirEntryCommand.getEntryType().equals(EnumEntryType.UNKNOWN))?new ArrayList<>(Arrays.asList(modDirEntryCommand.getEntryType().getId())):null);

        // system attributes
        baseDirectoryEntry.setHolder(modDirEntryCommand.getHolder());
        baseDirectoryEntry.setMaxKOMLEadr(modDirEntryCommand.getMaxKomLeAdr());
        baseDirectoryEntry.setActive(modDirEntryCommand.getActive() != null?modDirEntryCommand.getActive().getDataValue():null);
        baseDirectoryEntry.setMeta(modDirEntryCommand.getMeta());

        return baseDirectoryEntry;
    }

    private de.gematik.vzd.model.V1_12_6.BaseDirectoryEntry convertV1_12_6(ModDirEntryCommand modDirEntryCommand) {
        de.gematik.vzd.model.V1_12_6.BaseDirectoryEntry baseDirectoryEntry = new de.gematik.vzd.model.V1_12_6.BaseDirectoryEntry();

        //id attributes
        baseDirectoryEntry.setTelematikID(modDirEntryCommand.getTelematikId());
        baseDirectoryEntry.setDomainID(modDirEntryCommand.getDomainId());

        //name attributes
        baseDirectoryEntry.setDisplayName(modDirEntryCommand.getDisplayName());
        baseDirectoryEntry.setTitle(modDirEntryCommand.getTitle());
        baseDirectoryEntry.setOrganization(modDirEntryCommand.getOrganization());
        baseDirectoryEntry.setOtherName(modDirEntryCommand.getOtherName());
        baseDirectoryEntry.setSn(modDirEntryCommand.getSn());
        baseDirectoryEntry.setCn(modDirEntryCommand.getCn());
        baseDirectoryEntry.setGivenName(modDirEntryCommand.getGivenName());

        //address attributes
        baseDirectoryEntry.setStreetAddress(modDirEntryCommand.getStreetAddress());
        baseDirectoryEntry.setPostalCode(modDirEntryCommand.getPostalCode());
        baseDirectoryEntry.setLocalityName(modDirEntryCommand.getLocalityName());
        baseDirectoryEntry.setStateOrProvinceName((modDirEntryCommand.getStateOrProvinceName() != null && !modDirEntryCommand.getStateOrProvinceName().equals(EnumStateOrProvinceName.UNKNOWN))?modDirEntryCommand.getStateOrProvinceName().getHrText():null);
        baseDirectoryEntry.setCountryCode(modDirEntryCommand.getCountryCode());

        // profession attributes
        baseDirectoryEntry.setSpecialization(modDirEntryCommand.getSpecialization());
        baseDirectoryEntry.setEntryType((modDirEntryCommand.getEntryType() != null && !modDirEntryCommand.getEntryType().equals(EnumEntryType.UNKNOWN))?new ArrayList<>(Arrays.asList(modDirEntryCommand.getEntryType().getId())):null);

        // system attributes
        baseDirectoryEntry.setHolder(modDirEntryCommand.getHolder());
        baseDirectoryEntry.setMaxKOMLEadr(modDirEntryCommand.getMaxKomLeAdr());
        baseDirectoryEntry.setActive(modDirEntryCommand.getActive() != null?modDirEntryCommand.getActive().getDataValue():null);
        baseDirectoryEntry.setMeta(modDirEntryCommand.getMeta());

        return baseDirectoryEntry;
    }

}
