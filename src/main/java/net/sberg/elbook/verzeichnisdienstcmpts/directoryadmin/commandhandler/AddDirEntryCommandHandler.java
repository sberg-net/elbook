package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.commandhandler;

import de.gematik.vzd.api.V1_12_7.DirectoryEntryAdministrationApi;
import de.gematik.vzd.model.V1_12_7.BaseDirectoryEntry;
import de.gematik.vzd.model.V1_12_7.CreateDirectoryEntry;
import de.gematik.vzd.model.V1_12_7.DistinguishedName;
import de.gematik.vzd.model.V1_12_7.UserCertificate;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.VzdEntryWrapper;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.ClientImpl;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.TiVZDProperties;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.*;
import net.sberg.elbook.vzdclientcmpts.command.*;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.ICommandResultCallbackHandler;
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

    private DirectoryEntryAdministrationApi directoryEntryAdministrationApiV1_12_7;
    private de.gematik.vzd.api.V1_12_6.DirectoryEntryAdministrationApi directoryEntryAdministrationApiV1_12_6;
    private Logger log = LoggerFactory.getLogger(AddDirEntryCommandHandler.class);

    public AddDirEntryCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
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

            if (directoryEntryAdministrationApiV1_12_7 != null) {
                CreateDirectoryEntry createDirectoryEntry = new CreateDirectoryEntry();
                createDirectoryEntry.setDirectoryEntryBase(convertV1_12_7(addDirEntryCommand));

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

                ResponseEntity<DistinguishedName> response = directoryEntryAdministrationApiV1_12_7.addDirectoryEntryWithHttpInfo(createDirectoryEntry);

                if (response.getStatusCode() == HttpStatus.CREATED) {
                    log.debug("Add directory entry execution successful operated\n" + response.getBody());
                    commandResultCallbackHandler.handleDistinguishedName(command, new VzdEntryWrapper(response.getBody()));
                } else {
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "Add directory entry execution failed. Response-Status was: " + response
                            .getStatusCode() + "\n" + command.getUid()));
                }
            }
            else if (directoryEntryAdministrationApiV1_12_6 != null) {
                de.gematik.vzd.model.V1_12_6.CreateDirectoryEntry createDirectoryEntry = new de.gematik.vzd.model.V1_12_6.CreateDirectoryEntry();
                createDirectoryEntry.setDirectoryEntryBase(convertV1_12_6(addDirEntryCommand));

                if (addDirEntryCommand.getCertContents() != null && !addDirEntryCommand.getCertContents().isEmpty()) {
                    createDirectoryEntry.setUserCertificates(new ArrayList<>());
                    for (Iterator<String> iterator = addDirEntryCommand.getCertContents().iterator(); iterator.hasNext(); ) {
                        String content = iterator.next().replaceAll("[\n\r]", "").trim();
                        de.gematik.vzd.model.V1_12_6.UserCertificate userCertificate = new de.gematik.vzd.model.V1_12_6.UserCertificate();
                        userCertificate.setTelematikID(command.getTelematikId());
                        userCertificate.setUserCertificate(content);
                        createDirectoryEntry.getUserCertificates().add(userCertificate);
                    }
                }

                ResponseEntity<de.gematik.vzd.model.V1_12_6.DistinguishedName> response = directoryEntryAdministrationApiV1_12_6.addDirectoryEntryWithHttpInfo(createDirectoryEntry);

                if (response.getStatusCode() == HttpStatus.CREATED) {
                    log.debug("Add directory entry execution successful operated\n" + response.getBody());
                    commandResultCallbackHandler.handleDistinguishedName(command, new VzdEntryWrapper(response.getBody()));
                } else {
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "Add directory entry execution failed. Response-Status was: " + response
                            .getStatusCode() + "\n" + command.getUid()));
                }
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

    private BaseDirectoryEntry convertV1_12_7(AddDirEntryCommand addDirEntryCommand) {
        BaseDirectoryEntry baseDirectoryEntry = new BaseDirectoryEntry();

        //id attributes
        baseDirectoryEntry.setTelematikID(addDirEntryCommand.getTelematikId());
        baseDirectoryEntry.setDomainID(addDirEntryCommand.getDomainId());
        baseDirectoryEntry.setLanr(addDirEntryCommand.getLanr());
        baseDirectoryEntry.setProvidedBy(addDirEntryCommand.getProvidedBy());

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

    private de.gematik.vzd.model.V1_12_6.BaseDirectoryEntry convertV1_12_6(AddDirEntryCommand addDirEntryCommand) {
        de.gematik.vzd.model.V1_12_6.BaseDirectoryEntry baseDirectoryEntry = new de.gematik.vzd.model.V1_12_6.BaseDirectoryEntry();

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
