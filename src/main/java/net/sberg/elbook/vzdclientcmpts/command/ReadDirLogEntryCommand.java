package net.sberg.elbook.vzdclientcmpts.command;

import lombok.Data;

@Data
public class ReadDirLogEntryCommand extends AbstractCommand {

    private String logTimeFrom;
    private String logTimeTo;
    private String operation;
    private Boolean noDataChanged;

    @Override
    public EnumCommand command() {
        return EnumCommand.READ_DIR_LOG_ENTRY;
    }
}
