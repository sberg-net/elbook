package net.sberg.elbook.tspcmpts;

import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TspProperties {
    private String lakTelematikId;
    private String preProcessingService;
    private List<QvdaProperties> qvdas;

    public QvdaProperties getQvdaProperties(QVDA qvda) {
        return getQvdaProperties(qvda.name());
    }

    public QvdaProperties getQvdaProperties(String qvdaName) {
        List<QvdaProperties> res = qvdas.stream().filter(e -> e.getName().equals(qvdaName)).collect(Collectors.toList());
        if (res.isEmpty()) {
            throw new IllegalStateException("No QVDA " + qvdaName + " found in properties");
        }
        return res.get(0);
    }
}
