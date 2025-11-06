package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command;

public enum EnumTriValue {
    YES(true, "true", "Ja"),
    NO(false, "false", "Nein"),
    UNDEFINED(null, null,"Nicht gesetzt");

    private Boolean dataValue;
    private String textValue;
    private String dataValueStr;

    private EnumTriValue(Boolean dataValue, String dataValueStr, String textValue) {
        this.textValue = textValue;
        this.dataValue = dataValue;
        this.dataValueStr = dataValueStr;
    }

    public Boolean getDataValue() {
        return dataValue;
    }
    public String getTextValue() { return textValue; }
    public String getDataValueStr() { return dataValueStr; }

    public static final EnumTriValue getFromBool(Boolean val) {
        if (val == null) {
            return EnumTriValue.UNDEFINED;
        }
        if (val) {
            return EnumTriValue.YES;
        }
        return EnumTriValue.NO;
    }
}
