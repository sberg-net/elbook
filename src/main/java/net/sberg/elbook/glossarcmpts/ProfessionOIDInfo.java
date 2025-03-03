package net.sberg.elbook.glossarcmpts;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProfessionOIDInfo extends ProfessionOIDInfoReduced {
    private List<TelematikIdInfo> telematikIdInfos = new ArrayList<>();
}
