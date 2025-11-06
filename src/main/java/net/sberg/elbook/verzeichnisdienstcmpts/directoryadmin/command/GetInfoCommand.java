package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command;

public class GetInfoCommand extends AbstractCommand {
    @Override
    public EnumCommand command() {
        return EnumCommand.GET_INFO;
    }
}
