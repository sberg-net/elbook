package net.sberg.elbook.vzdclientcmpts.command;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AddDirCertCommand extends AbstractCommand {

    private List<String> certContents = new ArrayList<>();

    @Override
    public EnumCommand command() {
        return EnumCommand.ADD_DIR_CERT;
    }
}
