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

import de.gematik.ws.cm.pers.hba_smc_b.v1.SmcbAntragExport;
import lombok.Data;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.glossarcmpts.TelematikIdInfo;
import net.sberg.elbook.mandantcmpts.Mandant;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class VerzeichnisdienstImportCommandContainer {

    private String vzdAuthId;
    private String vzdAuthSecret;
    private List<VerzeichnisdienstImportCommand> commands = new ArrayList<>();
    private boolean syncWithTsps = false;
    private boolean silentMode = false;
    private String telematikIdPattern;

    public void merge() {
        Map<String, Optional<VerzeichnisdienstImportCommand>> cmdsPerTelematikId = commands.stream().collect(Collectors.groupingBy(VerzeichnisdienstImportCommand::getTelematikID, Collectors.reducing(VerzeichnisdienstImportCommand::merge)));
        commands.clear();
        for (Iterator<String> iterator = cmdsPerTelematikId.keySet().iterator(); iterator.hasNext(); ) {
            String telematikId =  iterator.next();
            commands.add(cmdsPerTelematikId.get(telematikId).get());
        }
    }

    public void syncHba(TelematikIdInfo telematikIdInfo, Mandant mandant) throws Exception {
        String certDirName = ICommonConstants.BASE_DIR + "tspSyncCerts" + File.separator + mandant.getId() + File.separator + telematikIdInfo.getTelematikIdPattern().getSektor();
        File certDir = new File(certDirName);
        if (!certDir.exists()) {
            return;
        }
        File[] files = certDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (!files[i].getName().endsWith(".crt")) {
                continue;
            }
            String telematikId = files[i].getName().split("_")[0];
            sync(telematikId, telematikIdInfo, null, files[i].getAbsolutePath());
        }
    }

    public void syncSmcb(TelematikIdInfo telematikIdInfo, List<SmcbAntragExport> smcbAntragExports) throws Exception {
        for (Iterator<SmcbAntragExport> iterator = smcbAntragExports.iterator(); iterator.hasNext(); ) {
            SmcbAntragExport smcbAntragExport = iterator.next();
            String telematikId = smcbAntragExport.getInstitution().getTelematikID().getValue();
            sync(telematikId, telematikIdInfo, smcbAntragExport, null);
        }
    }

    private void sync(String telematikId, TelematikIdInfo telematikIdInfo, SmcbAntragExport smcbAntragExport, String hbaAntragCertFile) throws Exception {
        String businessId = telematikIdInfo.getTelematikIdPattern().getSektor().getBusinessId(telematikId);

        List<VerzeichnisdienstImportCommand> c = commands.stream().filter(verzeichnisdienstImportCommand1 -> {
            String id = verzeichnisdienstImportCommand1.getVerwaltungsId();
            if (verzeichnisdienstImportCommand1.getVerwaltungsId().chars().anyMatch(Character::isDigit)) {
                id = String.valueOf(Integer.parseInt(verzeichnisdienstImportCommand1.getVerwaltungsId()));
            }
            return id.equals(businessId);
        }).collect(Collectors.toList());
        if (c.isEmpty()) {
            return;
        }

        if (c.size() > 1) {
            throw new IllegalStateException("more than one commands for the businessId = "+businessId);
        }

        VerzeichnisdienstImportCommand verzeichnisdienstImportCommand = c.get(0);

        //fill helper variables
        if (!verzeichnisdienstImportCommand.getHelperTelematikIDs().contains(telematikId)) {
            verzeichnisdienstImportCommand.getHelperTelematikIDs().add(telematikId);
        }
        if (smcbAntragExport != null) {
            verzeichnisdienstImportCommand.getHelperSmcbAntragExports().add(smcbAntragExport);
        }
        else if (hbaAntragCertFile != null) {
            verzeichnisdienstImportCommand.getHbaAntragCertFiles().add(hbaAntragCertFile);
        }
    }
}
