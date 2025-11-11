package net.sberg.elbook.verzeichnisdienstcmpts.fhir;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FhirResourceAttribute {
    private String name;
    private List<String> values = new ArrayList<>();
    private List<String> htmlValues = new ArrayList<>();
}
