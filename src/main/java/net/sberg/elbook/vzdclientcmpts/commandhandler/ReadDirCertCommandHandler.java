package net.sberg.elbook.vzdclientcmpts.commandhandler;

import de.gematik.vzd.api.CertificateAdministrationApi;
import de.gematik.vzd.model.UserCertificate;
import net.sberg.elbook.common.StringUtils;
import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.EnumCommand;
import net.sberg.elbook.vzdclientcmpts.command.ReadDirCertCommand;
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

public class ReadDirCertCommandHandler extends AbstractCommandHandler {

    private CertificateAdministrationApi certificateAdministrationApi;
    private Logger log = LoggerFactory.getLogger(ReadDirCertCommandHandler.class);

    public ReadDirCertCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        super(command, client, commandResultCallbackHandler);
        certificateAdministrationApi = new CertificateAdministrationApi(client);
    }

    @Override
    public boolean checkValidation() {
        if (!(command instanceof ReadDirCertCommand)) {
            throw new IllegalStateException("command not type of ReadDirCertCommand");
        }

        ReadDirCertCommand readDirCertCommand = (ReadDirCertCommand)command;

        if (readDirCertCommand.getActive() != null) {
            return true;
        }

        if (readDirCertCommand.getEntryType() != null) {
            return true;
        }

        boolean check = false;

        List<String> params = new ArrayList<>();
        params.add(readDirCertCommand.getUid());
        params.add(readDirCertCommand.getCertUid());
        params.add(readDirCertCommand.getTelematikId());
        params.add(readDirCertCommand.getIssuer());
        params.add(readDirCertCommand.getSerialNumber());
        params.add(readDirCertCommand.getPublicKeyAlgorithm());
        params.add(StringUtils.listToString(readDirCertCommand.getProfessionOid()));

        for (String parameter : params) {
            if (!org.apache.commons.lang3.StringUtils.isBlank(parameter)) {
                check = true;
            }
        }

        if (!check) {
            log.error("Missing search arguments");
        }

        return check;
    }

    public void executeCommand() {
        try {
            client.validateToken();

            ReadDirCertCommand readDirCertCommand = (ReadDirCertCommand)command;

            String uid = readDirCertCommand.getUid();
            String issuer = readDirCertCommand.getIssuer();
            String serialNumber = readDirCertCommand.getSerialNumber();
            String publicKeyAlgorithm = readDirCertCommand.getPublicKeyAlgorithm();
            String certUid = readDirCertCommand.getCertUid();
            String telematikID = readDirCertCommand.getTelematikId();
            String professionOID = !StringUtils.listToString(readDirCertCommand.getProfessionOid()).equals("")?StringUtils.listToString(readDirCertCommand.getProfessionOid()):null;
            String entryType = readDirCertCommand.getEntryType() != null?readDirCertCommand.getEntryType().getId():null;
            Boolean active = readDirCertCommand.getActive() != null?readDirCertCommand.getActive().getDataValue():null;

            ResponseEntity<List<UserCertificate>> response = certificateAdministrationApi.readDirectoryCertificatesWithHttpInfo(
                uid, certUid, entryType, telematikID, professionOID, active, serialNumber, issuer, publicKeyAlgorithm
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                if (response.getBody().isEmpty()) {
                    commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS));
                }
                else {
                    for (Iterator<UserCertificate> iterator = response.getBody().iterator(); iterator.hasNext(); ) {
                        UserCertificate userCertificate = iterator.next();
                        commandResultCallbackHandler.handleUserCertificate(command, userCertificate);
                    }
                }
            }
            else {
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ServerResult(response.getStatusCode().value(), response.getHeaders()));
                log.error("read cert entry execution failed. Response status was: " + response.getStatusCode());
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, "read cert entry execution failed. Response status was: " + response.getStatusCode()));
            }
        }
        catch (RestClientResponseException e) {
            AbstractCommandResultCallbackHandler.ServerResult serverResult = new AbstractCommandResultCallbackHandler.ServerResult(e.getStatusCode().value(), e.getResponseHeaders());
            commandResultCallbackHandler.handle(command, serverResult);

            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                commandResultCallbackHandler.handle(command, new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS));
            }
            else {
                log.error("error on executing command: " + EnumCommand.READ_DIR_CERT.getSpecName() + "\n" + command.getUid() + "\n" + serverResult, e);
                commandResultCallbackHandler.handle(command, e);
            }
        }
        catch (Exception ex) {
            log.error("error on executing command: " + EnumCommand.READ_DIR_CERT.getSpecName() + "\n" + command.getUid(), ex);
            commandResultCallbackHandler.handle(command, ex);
        }
    }

}
