package net.sberg.elbook.vzdclientcmpts.commandhandler;

import de.gematik.vzd.api.V1_9_5.CertificateAdministrationApi;
import de.gematik.vzd.model.V1_9_5.DistinguishedName;
import de.gematik.vzd.model.V1_9_5.UserCertificate;
import net.sberg.elbook.verzeichnisdienstcmpts.VzdEntryWrapper;
import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.AddDirCertCommand;
import net.sberg.elbook.vzdclientcmpts.command.EnumCommand;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.ICommandResultCallbackHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;

import java.util.Iterator;

public class AddDirCertCommandHandler extends AbstractCommandHandler {

    private CertificateAdministrationApi certificateAdministrationApiV1_9_5;
    private de.gematik.vzd.api.V1_12_6.CertificateAdministrationApi certificateAdministrationApiV1_12_6;

    private Logger log = LoggerFactory.getLogger(AddDirCertCommandHandler.class);

    protected AddDirCertCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        if (client.getTiVZDProperties().getApiVersion().equals(TiVZDProperties.API_VERSION_V1_9_5)) {
            certificateAdministrationApiV1_9_5 = new CertificateAdministrationApi(client);
        }
        else if (client.getTiVZDProperties().getApiVersion().equals(TiVZDProperties.API_VERSION_V1_12_6)) {
            certificateAdministrationApiV1_12_6 = new de.gematik.vzd.api.V1_12_6.CertificateAdministrationApi(client);
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

                    if (this.certificateAdministrationApiV1_9_5 != null) {
                        UserCertificate userCertificate = new UserCertificate();
                        userCertificate.setUserCertificate(certContent);

                        DistinguishedName dn = new DistinguishedName();
                        dn.setUid(addDirCertCommand.getUid());
                        userCertificate.setDn(dn);

                        ResponseEntity<DistinguishedName> response = certificateAdministrationApiV1_9_5.addDirectoryEntryCertificateWithHttpInfo(addDirCertCommand.getUid(), userCertificate);

                        if (response.getStatusCode() == HttpStatus.CREATED) {
                            log.debug("Certificate successful added: \n" + userCertificate);
                            commandResultCallbackHandler.handleDistinguishedName(command, new VzdEntryWrapper(response.getBody()));
                        } else {
                            commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "Add directory cert entry execution failed. Response-Status was: " + response
                                    .getStatusCode() + "\n" + command.getUid()));
                        }
                    }
                    else if (this.certificateAdministrationApiV1_12_6 != null) {
                        de.gematik.vzd.model.V1_12_6.UserCertificate userCertificate = new de.gematik.vzd.model.V1_12_6.UserCertificate();
                        userCertificate.setUserCertificate(certContent);

                        de.gematik.vzd.model.V1_12_6.DistinguishedName dn = new de.gematik.vzd.model.V1_12_6.DistinguishedName();
                        dn.setUid(addDirCertCommand.getUid());
                        userCertificate.setDn(dn);

                        ResponseEntity<de.gematik.vzd.model.V1_12_6.DistinguishedName> response = certificateAdministrationApiV1_12_6.addDirectoryEntryCertificateWithHttpInfo(addDirCertCommand.getUid(), userCertificate);

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
