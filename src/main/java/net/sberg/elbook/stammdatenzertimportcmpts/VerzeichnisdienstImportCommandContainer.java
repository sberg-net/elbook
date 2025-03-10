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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.ws.cm.pers.hba_smc_b.v1.ExtCertType;
import de.gematik.ws.cm.pers.hba_smc_b.v1.HbaAntragExport;
import de.gematik.ws.cm.pers.hba_smc_b.v1.ProdResultType;
import de.gematik.ws.cm.pers.hba_smc_b.v1.SmcbAntragExport;
import de.gematik.ws.sst.v1.GetHbaAntraegeExportResponseType;
import de.gematik.ws.sst.v1.GetSmcbAntraegeExportResponseType;
import lombok.Data;
import net.sberg.elbook.glossarcmpts.TelematikIdInfo;
import net.sberg.elbook.mandantcmpts.EnumSektor;
import org.apache.commons.codec.binary.Base64;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class VerzeichnisdienstImportCommandContainer {

    private String vzdAuthId;
    private String vzdAuthSecret;
    private List<VerzeichnisdienstImportCommand> commands = new ArrayList<>();
    private boolean syncWithTsps = false;
    private boolean silentMode = false;

    public void merge() {
        Map<String, Optional<VerzeichnisdienstImportCommand>> cmdsPerTelematikId = commands.stream().collect(Collectors.groupingBy(VerzeichnisdienstImportCommand::getTelematikID, Collectors.reducing(VerzeichnisdienstImportCommand::merge)));
        commands.clear();
        for (Iterator<String> iterator = cmdsPerTelematikId.keySet().iterator(); iterator.hasNext(); ) {
            String telematikId =  iterator.next();
            commands.add(cmdsPerTelematikId.get(telematikId).get());
        }
    }

    public void syncHba(TelematikIdInfo telematikIdInfo, List<HbaAntragExport> hbaAntragExports) throws Exception {
        for (Iterator<HbaAntragExport> iterator = hbaAntragExports.iterator(); iterator.hasNext(); ) {
            HbaAntragExport hbaAntragExport = iterator.next();
            String telematikId = hbaAntragExport.getFreigabedaten().getTelematikID();
            sync(telematikId, telematikIdInfo, hbaAntragExport.getProdResult());
        }
    }

    public void syncSmcb(TelematikIdInfo telematikIdInfo, List<SmcbAntragExport> smcbAntragExports) throws Exception {
        for (Iterator<SmcbAntragExport> iterator = smcbAntragExports.iterator(); iterator.hasNext(); ) {
            SmcbAntragExport smcbAntragExport = iterator.next();
            String telematikId = smcbAntragExport.getInstitution().getTelematikID().getValue();
            sync(telematikId, telematikIdInfo, smcbAntragExport.getProdResult());
        }
    }

    private void sync(String telematikId, TelematikIdInfo telematikIdInfo, List<ProdResultType> prodResultTypes) throws Exception {
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
        verzeichnisdienstImportCommand.setTelematikID(telematikId);
        verzeichnisdienstImportCommand.setToIgnore(false);

        for (Iterator<ProdResultType> iteratored = prodResultTypes.iterator(); iteratored.hasNext(); ) {
            ProdResultType prodResultType = iteratored.next();
            for (Iterator<ExtCertType> zertIterator = prodResultType.getZertifikate().iterator(); zertIterator.hasNext(); ) {
                ExtCertType extCertType = zertIterator.next();
                if (extCertType.getCertificateSem().contains(".ENC.")) {
                    EncZertifikat encZertifikat = new EncZertifikat();
                    encZertifikat.setContent(Base64.encodeBase64String(extCertType.getCertificateValue()));
                    verzeichnisdienstImportCommand.getEncZertifikat().add(encZertifikat);
                }
            }
        }
    }
}
