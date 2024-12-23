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
package net.sberg.elbook.reportcmpts;

import lombok.Data;
import net.sberg.elbook.mandantcmpts.EnumSektor;
import net.sberg.elbook.tspcmpts.EnumAntragTyp;

import java.util.HashMap;
import java.util.Map;

@Data
public class ReportDescriptor {
    private EnumSektor sektor;
    private EnumAntragTyp type;
    private String id;
    private String prefix;
    private String prefixSubject;
    private Map<String, ReportDescriptorKey> kuerzel = new HashMap<>();
}
