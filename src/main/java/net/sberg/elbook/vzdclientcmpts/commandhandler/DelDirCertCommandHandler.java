package net.sberg.elbook.vzdclientcmpts.commandhandler;

import de.gematik.vzd.api.CertificateAdministrationApi;
import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.DelDirCertCommand;
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

public class DelDirCertCommandHandler extends AbstractCommandHandler {

    private CertificateAdministrationApi certificateAdministrationApi;
    private Logger log = LoggerFactory.getLogger(DelDirCertCommandHandler.class);

    public DelDirCertCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        certificateAdministrationApi = new CertificateAdministrationApi(client);
    }

    @Override
    public boolean checkValidation() {
        if (!(command instanceof DelDirCertCommand)) {
            throw new IllegalStateException("command not type of DelDirCertCommand");
        }

        DelDirCertCommand delDirCertCommand = (DelDirCertCommand)command;

        if (StringUtils.isBlank(delDirCertCommand.getUid())) {
            log.error("No vzd uid defined");
            return false;
        }

        if (delDirCertCommand.getCertUids().isEmpty()) {
            log.error("No certificate uids delivered");
            return false;
        }

        return true;
    }

    public void executeCommand() {
        try {
            client.validateToken();

            DelDirCertCommand delDirCertCommand = (DelDirCertCommand)command;
            for (Iterator<String> iterator = delDirCertCommand.getCertUids().iterator(); iterator.hasNext(); ) {
                String certUid = iterator.next();
                try {
                    ResponseEntity<Void> response = certificateAdministrationApi.deleteDirectoryEntryCertificateWithHttpInfo(delDirCertCommand.getUid(), certUid);
                    if (response.getStatusCode() == HttpStatus.OK) {
                        log.debug(
                                "Certificate successful deleted: \n" + certUid);
                        commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(true, "succesful"));
                    }
                    else {
                        commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "Delete directory cert entry execution failed. Response-Status was: " + response
                                .getStatusCode() + "\n" + command.getUid()));
                    }
                }
                catch (RestClientResponseException e) {
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(e.getStatusCode().value(), e.getResponseHeaders()));
                    log.error(
                            "Something went wrong will deleting certificate. Responsecode: " + e.getMessage()
                                    + " certificate: " + certUid, e);
                    commandResultCallbackHandler.handle(command, e);
                }
            }
        }
        catch (Exception e) {
            log.error("error on executing command: " + EnumCommand.DEL_DIR_CERT.getSpecName() + "\n" + command.getUid(), e);
            commandResultCallbackHandler.handle(command, e);
        }
    }
}
