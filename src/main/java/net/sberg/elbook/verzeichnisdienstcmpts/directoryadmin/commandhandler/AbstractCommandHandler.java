package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.commandhandler;

import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.ClientImpl;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.AbstractCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.ICommandResultCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommandHandler {

    private Logger log = LoggerFactory.getLogger(AbstractCommandHandler.class);

    protected AbstractCommand command;
    protected ClientImpl client;
    protected ICommandResultCallbackHandler commandResultCallbackHandler;

    private AbstractCommandHandler() {}
    protected AbstractCommandHandler(AbstractCommand command, ClientImpl client, ICommandResultCallbackHandler commandResultCallbackHandler) {
        this.command = command;
        this.client = client;
        this.commandResultCallbackHandler = commandResultCallbackHandler;
    }

    public abstract boolean checkValidation() throws Exception;
    public abstract void executeCommand();
}
