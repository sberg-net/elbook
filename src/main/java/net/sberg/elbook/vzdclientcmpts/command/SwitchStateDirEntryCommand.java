package net.sberg.elbook.vzdclientcmpts.command;

import lombok.Data;

@Data
public class SwitchStateDirEntryCommand extends AbstractCommand {

    private Boolean active;

    @Override
    public EnumCommand command() {
        return EnumCommand.SWITCH_STATE_DIR_ENTRY;
    }
}
