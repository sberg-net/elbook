package net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler;

import net.sberg.elbook.vzdclientcmpts.ClientImpl;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.EnumCommand;
import net.sberg.elbook.vzdclientcmpts.commandhandler.AbstractCommandHandler;
import net.sberg.elbook.vzdclientcmpts.commandhandler.CommandHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultCommandResultCallbackHandler extends AbstractCommandResultCallbackHandler {

    private Logger log = LoggerFactory.getLogger(DefaultCommandResultCallbackHandler.class);

    private ClientImpl client;

    private DefaultCommandResultCallbackHandler() {}
    public DefaultCommandResultCallbackHandler(ClientImpl client) {
        this.client = client;
    }

    public DefaultCommandResultCallbackHandler execute(List<AbstractCommand> commands) throws Exception {
        for (Iterator<AbstractCommand> iterator = commands.iterator(); iterator.hasNext(); ) {
            AbstractCommand command = iterator.next();
            clear();
            try {
                if (command.command().equals(EnumCommand.ADD_DIR_ENTRY)) {
                    if (command.getHolder() == null || command.getHolder().isEmpty()) {
                        command.setHolder(new ArrayList<>());
                        command.getHolder().add(client.getHolder());
                    }
                }
                else if (!command.command().equals(EnumCommand.READ_DIR_SYNC_ENTRY)
                        &&
                        !command.command().equals(EnumCommand.READ_DIR_ENTRY)
                        &&
                        !command.command().equals(EnumCommand.READ_DIR_CERT)
                        &&
                        !command.command().equals(EnumCommand.READ_DIR_LOG_ENTRY)
                        &&
                        !command.command().equals(EnumCommand.READ_DIR_FA_ATTRIBUTES_ENTRY)
                        &&
                        !command.command().equals(EnumCommand.READ_DIR_FA_ATTRIBUTES_SYNC_ENTRY)
                        &&
                        !command.command().equals(EnumCommand.GET_INFO)
                ) {
                    if (command.getHolder() != null && !command.getHolder().isEmpty() && !command.getHolder().contains(client.getHolder())) {
                        log.error("error on execute op: "+command.command()+" - holder not permittet: "+client.getHolder());
                        throw new IllegalStateException("error on execute op: "+command.command()+" - holder not permittet: "+client.getHolder());
                    }
                }
                
                //execute
                AbstractCommandHandler commandHandler = CommandHandlerFactory.create(command, client, this);
                if (commandHandler.checkValidation()) {
                    commandHandler.executeCommand();
                }
                else {
                    handle(command, new ResultReason(false, ResultReason.PRE_EXECUTE_VALIDATION_FAILED));
                }
            }
            catch (Exception e) {
                handle(command, e);
            }
        }
        return this;
    }
}
