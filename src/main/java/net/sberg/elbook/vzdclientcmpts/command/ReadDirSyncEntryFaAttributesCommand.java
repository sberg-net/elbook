package net.sberg.elbook.vzdclientcmpts.command;

import lombok.Data;

@Data
public class ReadDirSyncEntryFaAttributesCommand extends AbstractCommand {

    private String mail;
    private String komLeData;
    private String kimData;

    private PagingInfo pagingInfo;

    @Override
    public EnumCommand command() {
        return EnumCommand.READ_DIR_FA_ATTRIBUTES_SYNC_ENTRY;
    }

    public boolean isEmpty() {
        return (mail == null || mail.trim().isEmpty())
            && (komLeData == null || komLeData.trim().isEmpty())
            && (kimData == null || kimData.trim().isEmpty());
    }
}
