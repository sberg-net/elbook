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
package net.sberg.elbook.stammdatenzertimportcmpts;

import lombok.Data;
import net.sberg.elbook.mandantcmpts.EnumSektor;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class VerzeichnisdienstImportCommandContainer {
    private EnumSektor sektor = EnumSektor.APOTHEKE;
    private String vzdAuthId;
    private String vzdAuthSecret;
    private List<VerzeichnisdienstImportCommand> commands = new ArrayList<>();

    public void merge() {
        Map<String, Optional<VerzeichnisdienstImportCommand>> cmdsPerTelematikId = commands.stream().collect(Collectors.groupingBy(VerzeichnisdienstImportCommand::getTelematikID, Collectors.reducing(VerzeichnisdienstImportCommand::merge)));
        commands.clear();
        for (Iterator<String> iterator = cmdsPerTelematikId.keySet().iterator(); iterator.hasNext(); ) {
            String telematikId =  iterator.next();
            commands.add(cmdsPerTelematikId.get(telematikId).get());
        }
    }
}
