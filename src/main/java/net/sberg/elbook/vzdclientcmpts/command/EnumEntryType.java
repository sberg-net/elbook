package net.sberg.elbook.vzdclientcmpts.command;

import org.slf4j.Logger;

public enum EnumEntryType {

    UNKNOWN("-1","Unbekannter Eintragstyp"),
    Berufsgruppe("1","Berufsgruppe"),
    Versicherte("2","Versicherte/-r"),
    Leistungserbringerinstitution("3","Leistungserbringerinstitution"),
    Organisation("4","Organisation"),
    Krankenkasse("5","Krankenkasse"),
    Krankenkasse_ePA("6","Krankenkasse ePA"),
    Kim("7","KIM-Hersteller und Anbieter"),
    DiGA("9","DIGA-Hersteller und Anbieter");

    private String id;
    private String hrText;

    private EnumEntryType(String id, String hrText) {
        this.id = id;
        this.hrText = hrText;
    }

    public String getHrText() {
        return hrText;
    }
    public String getId() {
        return id;
    }
    public static final EnumEntryType getFromId(String id, Logger logger) {
        if (id == null) {
            return EnumEntryType.UNKNOWN;
        }
        if (id.equals(EnumEntryType.Berufsgruppe.id)) {
            return EnumEntryType.Berufsgruppe;
        }
        if (id.equals(EnumEntryType.Versicherte.id)) {
            return EnumEntryType.Versicherte;
        }
        if (id.equals(EnumEntryType.Leistungserbringerinstitution.id)) {
            return EnumEntryType.Leistungserbringerinstitution;
        }
        if (id.equals(EnumEntryType.Organisation.id)) {
            return EnumEntryType.Organisation;
        }
        if (id.equals(EnumEntryType.Krankenkasse.id)) {
            return EnumEntryType.Krankenkasse;
        }
        if (id.equals(EnumEntryType.Krankenkasse_ePA.id)) {
            return EnumEntryType.Krankenkasse_ePA;
        }
        if (id.equals(EnumEntryType.Kim.id)) {
            return EnumEntryType.Kim;
        }
        if (id.equals(EnumEntryType.DiGA.id)) {
            return EnumEntryType.DiGA;
        }
        if (id.equals(EnumEntryType.UNKNOWN.id)) {
            return EnumEntryType.UNKNOWN;
        }
        logger.error("unknown entry type: "+id);
        return EnumEntryType.UNKNOWN;
    }
}
