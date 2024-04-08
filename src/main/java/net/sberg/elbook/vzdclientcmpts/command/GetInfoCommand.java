package net.sberg.elbook.vzdclientcmpts.command;

public class GetInfoCommand extends AbstractCommand {
    @Override
    public EnumCommand command() {
        return EnumCommand.GET_INFO;
    }
}
