package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command;

import lombok.Data;

@Data
public class DelDirEntryCommand extends AbstractCommand {

    @Override
    public EnumCommand command() {
        return EnumCommand.DEL_DIR_ENTRY;
    }
}
