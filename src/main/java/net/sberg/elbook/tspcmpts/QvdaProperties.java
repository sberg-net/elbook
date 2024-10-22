package net.sberg.elbook.tspcmpts;

import lombok.Data;

import java.util.Map;

@Data
public class QvdaProperties {
    private String name;
    private String frontendString;
    private Map<EnumAntragTyp, String> antragsPortal;
    private Boolean hba;
    private Boolean smcb;
    private Map<EnumAntragTyp, ConnectionProperties> connection;
}
