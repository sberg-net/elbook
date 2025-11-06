package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command;

import lombok.Data;

import java.util.List;

@Data
public class DelDirCertCommand extends AbstractCommand {

    private List<String> certUids;

    @Override
    public EnumCommand command() {
        return EnumCommand.DEL_DIR_CERT;
    }
}
