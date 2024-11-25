package net.sberg.elbook.glossarcmpts;

import lombok.Data;
import net.sberg.elbook.mandantcmpts.EnumSektor;

@Data
public class TelematikIdPattern {
    private String pattern;
    private String fhirResourceType;
    private String code;
    private String displayShort;
    private String professionOIDs;
    private EnumSektor sektor;
    private String sektorImplLeitfadenUrl;
}
