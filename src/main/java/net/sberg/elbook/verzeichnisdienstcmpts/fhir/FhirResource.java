package net.sberg.elbook.verzeichnisdienstcmpts.fhir;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FhirResource {
    private String name;
    private String resourceName;
    private String uid;
    private int order;
    private List<FhirResourceAttribute> attributes = new ArrayList<>();
}
