package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin;

import lombok.Data;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.ReadDirSyncEntryCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.ReadDirSyncEntryFaAttributesCommand;

@Data
public class VzdOverviewSearchContainer {
    private ReadDirSyncEntryCommand readDirSyncEntryCommand;
    private ReadDirSyncEntryFaAttributesCommand readDirSyncEntryFaAttributesCommand;
}
