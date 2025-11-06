package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReadDirCertCommand extends AbstractCommand {

    private String certUid;
    private EnumEntryType entryType;
    private List<String> professionOid = new ArrayList<>();
    private EnumTriValue active = EnumTriValue.UNDEFINED;
    private String serialNumber;
    private String issuer;
    private String publicKeyAlgorithm;

    @Override
    public EnumCommand command() {
        return EnumCommand.READ_DIR_CERT;
    }
}
