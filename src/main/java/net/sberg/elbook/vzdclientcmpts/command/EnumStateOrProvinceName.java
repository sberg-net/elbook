package net.sberg.elbook.vzdclientcmpts.command;

public enum EnumStateOrProvinceName {
    MV("Mecklenburg-Vorpommern"),
    SH("Schleswig-Holstein"),
    NDS("Niedersachsen"),
    HH("Hamburg"),
    HB("Bremen"),
    NRW("Nordrhein-Westfalen"),
    NRW_WL_LIPPE("Westfalen-Lippe"),
    NRW_NR("Nordrhein"),
    B("Berlin"),
    TH("Thüringen"),
    BAY("Bayern"),
    RLP("Rheinland-Pfalz"),
    BW("Baden-Württemberg"),
    SACHSEN("Sachsen"),
    SACHSENANHALT("Sachsen-Anhalt"),
    BB("Brandenburg"),
    HS("Hessen"),
    SAR("Saarland"),
    UNKNOWN("Unbekannt");

    private String hrText;
    private EnumStateOrProvinceName(String hrText) {
        this.hrText = hrText;
    }

    public String getHrText() {
        return hrText;
    }
    public static final EnumStateOrProvinceName getFromHrText(String hrText) {
        if (hrText == null) {
            return EnumStateOrProvinceName.UNKNOWN;
        }
        if (hrText.equals(EnumStateOrProvinceName.B.hrText)) {
            return EnumStateOrProvinceName.B;
        }
        if (hrText.equals(EnumStateOrProvinceName.BAY.hrText)) {
            return EnumStateOrProvinceName.BAY;
        }
        if (hrText.equals(EnumStateOrProvinceName.BB.hrText)) {
            return EnumStateOrProvinceName.BB;
        }
        if (hrText.equals(EnumStateOrProvinceName.BW.hrText)) {
            return EnumStateOrProvinceName.BW;
        }
        if (hrText.equals(EnumStateOrProvinceName.HB.hrText)) {
            return EnumStateOrProvinceName.HB;
        }
        if (hrText.equals(EnumStateOrProvinceName.HH.hrText)) {
            return EnumStateOrProvinceName.HH;
        }
        if (hrText.equals(EnumStateOrProvinceName.HS.hrText)) {
            return EnumStateOrProvinceName.HS;
        }
        if (hrText.equals(EnumStateOrProvinceName.MV.hrText)) {
            return EnumStateOrProvinceName.MV;
        }
        if (hrText.equals(EnumStateOrProvinceName.NDS.hrText)) {
            return EnumStateOrProvinceName.NDS;
        }
        if (hrText.equals(EnumStateOrProvinceName.NRW.hrText)) {
            return EnumStateOrProvinceName.NRW;
        }
        if (hrText.equals(EnumStateOrProvinceName.NRW_WL_LIPPE.hrText)) {
            return EnumStateOrProvinceName.NRW_WL_LIPPE;
        }
        if (hrText.equals(EnumStateOrProvinceName.NRW_NR.hrText)) {
            return EnumStateOrProvinceName.NRW_NR;
        }
        if (hrText.equals(EnumStateOrProvinceName.RLP.hrText)) {
            return EnumStateOrProvinceName.RLP;
        }
        if (hrText.equals(EnumStateOrProvinceName.SAR.hrText)) {
            return EnumStateOrProvinceName.SAR;
        }
        if (hrText.equals(EnumStateOrProvinceName.SACHSEN.hrText)) {
            return EnumStateOrProvinceName.SACHSEN;
        }
        if (hrText.equals(EnumStateOrProvinceName.SACHSENANHALT.hrText)) {
            return EnumStateOrProvinceName.SACHSENANHALT;
        }
        if (hrText.equals(EnumStateOrProvinceName.SH.hrText)) {
            return EnumStateOrProvinceName.SH;
        }
        if (hrText.equals(EnumStateOrProvinceName.TH.hrText)) {
            return EnumStateOrProvinceName.TH;
        }
        else {
            return EnumStateOrProvinceName.UNKNOWN;
        }
    }
}
