/*
 *  Copyright (C) 2023 sberg it-systeme GmbH
 *
 *  Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the
 *  European Commission - subsequent versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
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
