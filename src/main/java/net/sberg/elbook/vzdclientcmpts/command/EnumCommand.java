package net.sberg.elbook.vzdclientcmpts.command;

public enum EnumCommand {

    ADD_DIR_ENTRY("add_Directory_Entry", "VZD-Eintrag - hinzugefügt"),
    READ_DIR_ENTRY("read_Directory_Entry", "VZD-Eintrag - gelesen"),
    READ_DIR_SYNC_ENTRY("read_Directory_Sync_Entries", "VZD-Eintrag - gelesen im Sync-Mode"),
    READ_DIR_FA_ATTRIBUTES_ENTRY("read_Directory_FA-Attributes", "VZD-Eintrag - gelesen und gefunden in den Fachdaten mail, komledata und kimdata"),
    READ_DIR_FA_ATTRIBUTES_SYNC_ENTRY("read_Directory_Sync_Entries", "VZD-Eintrag - gelesen und gefunden im Sync-Mode in den Fachdaten mail, komledata und kimdata"),
    SWITCH_STATE_DIR_ENTRY("stateSwitch_Directory_Entry", "VZD-Eintrag - State geändert"),
    READ_DIR_LOG_ENTRY("read_Directory_Log_Entries", "VZD-Eintrag - Logs gelesen"),
    MOD_DIR_ENTRY("modify_Directory_Entry", "VZD-Eintrag - geändert"),
    DEL_DIR_ENTRY("delete_Directory_Entry", "VZD-Eintrag - gelöscht"),
    ADD_DIR_CERT("add_Directory_Entry_Certificate", "VZD-Eintrag - Zertifikat hinzugefügt"),
    READ_DIR_CERT("readDirectoryEntryCertificate", "VZD-Eintrag - Zertifikat gelesen"),
    DEL_DIR_CERT("delete_Directory_Entry_Certificate", "VZD-Eintrag - Zertifikat gelöscht"),
    ADD_DIR_ENTRY_FA_ATTRIBUTES("add_Directory_FA-Attributes", "VZD-Eintrag - Fachdienst-Attr. hinzugefügt"),
    MOD_DIR_ENTRY_FA_ATTRIBUTES("modify_Directory_FA-Attributes", "VZD-Eintrag - Fachdienst-Attr. geändert"),
    DEL_DIR_ENTRY_FA_ATTRIBUTES("delete_Directory_FA-Attributes", "VZD-Eintrag - Fachdienst-Attr. gelöscht"),
    GET_INFO("get_Info", "VZD-Informationen gelesen");

    private final String specName;
    private final String hrText;

    EnumCommand(String specName, String hrText) {
        this.hrText = hrText;
        this.specName = specName;
    }

    public String getSpecName() {
        return this.specName;
    }
    public String getHrText() { return hrText; }

    public static EnumCommand getEntry(String name) {
        for (EnumCommand cn : EnumCommand.values()) {
            if (name.equals(cn.getSpecName())) {
                return cn;
            }
        }
        return null;
    }
}
