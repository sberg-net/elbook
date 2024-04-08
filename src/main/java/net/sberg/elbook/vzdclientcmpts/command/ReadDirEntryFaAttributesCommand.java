package net.sberg.elbook.vzdclientcmpts.command;

import lombok.Data;

@Data
public class ReadDirEntryFaAttributesCommand extends AbstractCommand {

    private String mail;
    private String komLeData;
    private String kimData;

    @Override
    public EnumCommand command() {
        return EnumCommand.READ_DIR_FA_ATTRIBUTES_ENTRY;
    }
}
