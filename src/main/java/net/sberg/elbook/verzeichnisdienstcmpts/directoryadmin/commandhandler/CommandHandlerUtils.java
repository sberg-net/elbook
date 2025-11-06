package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.commandhandler;

import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.VzdEntryWrapper;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.ClientImpl;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.ReadDirEntryCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.DefaultCommandResultCallbackHandler;
import org.slf4j.Logger;

import java.util.List;

public class CommandHandlerUtils {

    protected static final boolean isEntryPresent(
        String uid,
        String telematikId,
        ClientImpl client,
        Logger log) {
        try {
            if (uid != null) {

                ReadDirEntryCommand readDirEntryCommand = new ReadDirEntryCommand();
                readDirEntryCommand.setUid(uid);
                readDirEntryCommand.setBaseEntryOnly(true);

                DefaultCommandResultCallbackHandler commandResultCallbackHandler = new DefaultCommandResultCallbackHandler(client);
                CommandHandlerFactory.create(readDirEntryCommand, client, commandResultCallbackHandler).executeCommand();

                List entries = commandResultCallbackHandler.getDirectoryEntries(readDirEntryCommand.getId());

                if (entries != null && !entries.isEmpty()) {
                    return true;
                }
                return false;
            }
            else {
                uid = searchUidByTelematikId(telematikId, client, log);
                return uid != null && !uid.trim().isEmpty();
            }
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("error on isEntryPresent", e);
            }
            return false;
        }
    }

    private static final String searchUidByTelematikId(String telematikId, ClientImpl client, Logger log) {
        try {
            if (telematikId == null || telematikId.trim().isEmpty()) {
                return null;
            }

            ReadDirEntryCommand readDirEntryCommand = new ReadDirEntryCommand();
            readDirEntryCommand.setTelematikId(telematikId);
            readDirEntryCommand.setBaseEntryOnly(true);

            DefaultCommandResultCallbackHandler commandResultCallbackHandler = new DefaultCommandResultCallbackHandler(client);
            CommandHandlerFactory.create(readDirEntryCommand, client, commandResultCallbackHandler).executeCommand();

            List entries = commandResultCallbackHandler.getDirectoryEntries(readDirEntryCommand.getId());

            if (entries != null && !entries.isEmpty()) {
                return ((VzdEntryWrapper)entries.get(0)).extractDirectoryEntryUid();
            }
            return null;
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("error on searchUidByTelematikId", e);
            }
            return null;
        }
    }
}
