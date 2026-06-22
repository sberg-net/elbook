package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VzdEntryResult {
    private String anzeigeName;
    private String strasse;
    private String plz;
    private String ort;
    private String bundesland;
    private String telematikId;
    private List<String> kimAdressen;
    private List<String> spezialisierung;
    private List<String> holder;
    private String aenderungsDatum;
    private boolean aktiv;
    private List<VzdCertificateEntryResult> zertifikate = new ArrayList<>();
}
