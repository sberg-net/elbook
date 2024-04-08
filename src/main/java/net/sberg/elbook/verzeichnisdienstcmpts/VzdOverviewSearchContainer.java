package net.sberg.elbook.verzeichnisdienstcmpts;

import lombok.Data;
import net.sberg.elbook.vzdclientcmpts.command.ReadDirSyncEntryCommand;
import net.sberg.elbook.vzdclientcmpts.command.ReadDirSyncEntryFaAttributesCommand;

@Data
public class VzdOverviewSearchContainer {
    private ReadDirSyncEntryCommand readDirSyncEntryCommand;
    private ReadDirSyncEntryFaAttributesCommand readDirSyncEntryFaAttributesCommand;
}
