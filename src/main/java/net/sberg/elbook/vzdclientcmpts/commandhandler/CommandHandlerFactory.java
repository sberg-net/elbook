package net.sberg.elbook.vzdclientcmpts.commandhandler;

import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.ICommandResultCallbackHandler;

public class CommandHandlerFactory {
    public static final AbstractCommandHandler create(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        switch (command.command()) {
            case ADD_DIR_ENTRY: return new AddDirEntryCommandHandler(command, client, commandResultCallbackHandler);
            case ADD_DIR_CERT: return new AddDirCertCommandHandler(command, client, commandResultCallbackHandler);
            case READ_DIR_CERT: return new ReadDirCertCommandHandler(command, client, commandResultCallbackHandler);
            case READ_DIR_ENTRY: return new ReadDirEntryCommandHandler(command, client, commandResultCallbackHandler);
            case READ_DIR_LOG_ENTRY: return new ReadDirLogEntryCommandHandler(command, client, commandResultCallbackHandler);
            case READ_DIR_SYNC_ENTRY: return new ReadDirSyncEntryCommandHandler(command, client, commandResultCallbackHandler);
            case GET_INFO: return new GetInfoCommandHandler(command, client, commandResultCallbackHandler);
            case SWITCH_STATE_DIR_ENTRY: return new SwitchStateDirEntryCommandHandler(command, client, commandResultCallbackHandler);
            case DEL_DIR_CERT: return new DelDirCertCommandHandler(command, client, commandResultCallbackHandler);
            case DEL_DIR_ENTRY: return new DelDirEntryCommandHandler(command, client, commandResultCallbackHandler);
            case MOD_DIR_ENTRY: return new ModDirEntryCommandHandler(command, client, commandResultCallbackHandler);
            case READ_DIR_FA_ATTRIBUTES_ENTRY: return new ReadDirEntryFaAttributesCommandHandler(command, client, commandResultCallbackHandler);
            case READ_DIR_FA_ATTRIBUTES_SYNC_ENTRY: return new ReadDirSyncEntryFaAttributesCommandHandler(command, client, commandResultCallbackHandler);
            default: throw new IllegalStateException("command not defined: "+command.command());
        }
    }
}
