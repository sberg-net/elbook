package net.sberg.elbook.verzeichnisdienstcmpts;

public enum EnumFachdienst {
    FAD0002("CGM"),
    FAD0006("Akquinet"),
    FAD0008("T-Systems"),
    FAD0012("IBM"),
    FAD0015("Arvato"),
    FAD0017("BITMARCK"),
    FAD0021("RISE");

    private String hrText;
    private EnumFachdienst(String hrText) {
        this.hrText = hrText;
    }
    public String getHrText() {
        return hrText;
    }

}
