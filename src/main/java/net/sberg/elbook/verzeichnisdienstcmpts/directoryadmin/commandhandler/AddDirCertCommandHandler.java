package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.commandhandler;

import de.gematik.vzd.api.V1_12_8.CertificateAdministrationApi;
import de.gematik.vzd.model.V1_12_8.DistinguishedName;
import de.gematik.vzd.model.V1_12_8.UserCertificate;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.VzdEntryWrapper;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.ClientImpl;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.TiVZDProperties;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.AddDirCertCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.EnumCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.AbstractCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.ICommandResultCallbackHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;

import java.util.Iterator;

public class AddDirCertCommandHandler extends AbstractCommandHandler {

    private CertificateAdministrationApi certificateAdministrationApiV1_12_8;

    private Logger log = LoggerFactory.getLogger(AddDirCertCommandHandler.class);

    protected AddDirCertCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        if (client.getTiVZDProperties().getApiVersion().equals(TiVZDProperties.API_VERSION_V1_12_8)) {
            certificateAdministrationApiV1_12_8 = new CertificateAdministrationApi(client);
        }
        else {
            throw new IllegalStateException("unknown api version: "+client.getTiVZDProperties().getInfoObject().getVersion());
        }
    }

    @Override
    public boolean checkValidation() {
        if (!(command instanceof AddDirCertCommand)) {
            throw new IllegalStateException("command not type of AddDirCertCommand");
        }

        AddDirCertCommand addDirCertCommand = (AddDirCertCommand)command;

        if (StringUtils.isBlank(addDirCertCommand.getUid())) {
            log.error("No vzd uid defined");
            return false;
        }

        if (addDirCertCommand.getCertContents().isEmpty()) {
            log.error("No certificate contents delivered");
            return false;
        }

        return true;
    }

    @Override
    public void executeCommand() {
        try {
            client.validateToken();

            if (!CommandHandlerUtils.isEntryPresent(command.getUid(), null, client, log)) {
                log.error("Entry for this Certificate could not be found: " + command.getUid());
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "Entry for this Certificate could not be found: " + command.getUid()));
                return;
            }


            AddDirCertCommand addDirCertCommand = (AddDirCertCommand)command;

            for (Iterator<String> iterator = addDirCertCommand.getCertContents().iterator(); iterator.hasNext(); ) {
                String certContent = iterator.next();
                try {

                    if (this.certificateAdministrationApiV1_12_8 != null) {
                        UserCertificate userCertificate = new UserCertificate();
                        userCertificate.setUserCertificate(certContent);

                        DistinguishedName dn = new DistinguishedName();
                        dn.setUid(addDirCertCommand.getUid());
                        userCertificate.setDn(dn);

                        ResponseEntity<DistinguishedName> response = certificateAdministrationApiV1_12_8.addDirectoryEntryCertificateWithHttpInfo(addDirCertCommand.getUid(), userCertificate);

                        if (response.getStatusCode() == HttpStatus.CREATED) {
                            log.debug("Certificate successful added: \n" + userCertificate);
                            commandResultCallbackHandler.handleDistinguishedName(command, new VzdEntryWrapper(response.getBody()));
                        } else {
                            commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "Add directory cert entry execution failed. Response-Status was: " + response
                                    .getStatusCode() + "\n" + command.getUid()));
                        }
                    }
                }
                catch (RestClientResponseException e) {
                    AbstractCommandResultCallbackHandler.ServerResult serverResult = new AbstractCommandResultCallbackHandler.ServerResult(e.getStatusCode().value(), e.getResponseHeaders());
                    commandResultCallbackHandler.handle(command, serverResult);
                    log.error(
                            "Something went wrong will adding certificate. Responsecode: " + e.getMessage()
                                    + " certificate: " + certContent + "\n" + serverResult, e);
                    commandResultCallbackHandler.handle(command, e);
                }
            }
        }
        catch (Exception e) {
            log.error("error on executing command: " + EnumCommand.ADD_DIR_CERT.getSpecName() + "\n" + command.getUid(), e);
            commandResultCallbackHandler.handle(command, e);
        }
    }
}
