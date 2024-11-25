package net.sberg.elbook.glossarcmpts;

import lombok.Data;
import net.sberg.elbook.vzdclientcmpts.command.EnumEntryType;

@Data
public class ProfessionOIDInfo {
    private String code;
    private String display;
    private EnumEntryType entryType;
    private String entryTypeId;
    private String entryTypeText;
}
