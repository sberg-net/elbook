package net.sberg.elbook.vzdclientcmpts;

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
