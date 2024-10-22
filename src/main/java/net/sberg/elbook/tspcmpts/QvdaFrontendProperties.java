package net.sberg.elbook.tspcmpts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QvdaFrontendProperties {
    private String name;
    private String frontendString;
    private Map<EnumAntragTyp, String> antragsPortal;
    private Boolean hba;
    private Boolean smcb;
}
