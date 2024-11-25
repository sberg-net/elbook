package net.sberg.elbook.glossarcmpts;

import lombok.Data;

@Data
public class TelematikIdPattern {
    private String pattern;
    private String fhirResourceType;
    private String code;
    private String displayShort;
}
