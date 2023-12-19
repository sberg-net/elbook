package net.sberg.elbook.vzdclientcmpts.commandhandler;

import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.ICommandResultCallbackHandler;
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
