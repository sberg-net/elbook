package net.sberg.elbook.tspcmpts;

import lombok.Data;

@Data
public class ConnectionProperties {
    private int timeout;
    private String defaultUri;
    private String keystorePass;
    private String keystoreType;
    private String keystoreFile;
    private boolean keystoreFileInClasspath = true;
}
