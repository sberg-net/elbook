package net.sberg.elbook.vzdclientcmpts.command;

import lombok.Data;

@Data
public class DelDirEntryCommand extends AbstractCommand {

    @Override
    public EnumCommand command() {
        return EnumCommand.DEL_DIR_ENTRY;
    }
}
