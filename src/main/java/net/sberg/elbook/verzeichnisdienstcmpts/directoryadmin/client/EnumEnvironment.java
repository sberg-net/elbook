package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client;

public enum EnumEnvironment {
    PU("Produktivumgebung"),
    RU("Referenzumgebung"),
    TU("Testumgebung"),
    UNKNOWN("Unbekannt");

    private String hrText;
    private EnumEnvironment(String hrText) {
        this.hrText = hrText;
    }
    public String getHrText() {
        return hrText;
    }
}
