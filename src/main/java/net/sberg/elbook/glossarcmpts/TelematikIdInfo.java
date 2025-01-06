package net.sberg.elbook.glossarcmpts;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TelematikIdInfo {
    private TelematikIdPattern telematikIdPattern;
    private String telematikId;
    private List<ProfessionOIDInfo> professionOIDInfos = new ArrayList<>();
}
