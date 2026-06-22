package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin;

import lombok.Data;

import java.util.List;

@Data
public class VzdCertificateEntryResult {
    private String telematikId;
    private List<String> professionOid;
    private String serienNummer;
    private String algorithmus;
    private String gueltigVon;
    private String gueltigBis;
    private boolean gueltig;
    private boolean aktiv;
    private String anbieter;
    private String inhaber;
}
