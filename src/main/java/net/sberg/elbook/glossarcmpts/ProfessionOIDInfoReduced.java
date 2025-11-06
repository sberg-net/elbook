package net.sberg.elbook.glossarcmpts;

import lombok.Data;
import net.sberg.elbook.tspcmpts.EnumAntragTyp;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.EnumEntryType;

@Data
public class ProfessionOIDInfoReduced {
    private String code;
    private String display;
    private EnumEntryType entryType;
    private String entryTypeId;
    private String entryTypeText;
    private boolean organization;
    private boolean practitionier;
    private EnumAntragTyp tspAntragTyp;
}
