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
package net.sberg.elbook.holderattrcmpts;

import lombok.Data;
import net.sberg.elbook.glossarcmpts.GlossarService;
import net.sberg.elbook.verzeichnisdienstcmpts.DirectoryEntrySaveContainer;
import net.sberg.elbook.verzeichnisdienstcmpts.VzdEntryWrapper;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.*;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
public class HolderAttrCommand {

    private String telematikID;
    private List<String> holder;

    public DirectoryEntrySaveContainer createModDirEntryCommand(GlossarService glossarService, TiVZDProperties tiVZDProperties, VzdEntryWrapper directoryEntry, Logger log, HolderAttrCommandContainer holderAttrCommandContainer, HolderAttrErgebnis holderAttrErgebnis) {
        DirectoryEntrySaveContainer directoryEntrySaveContainer = new DirectoryEntrySaveContainer();
        directoryEntrySaveContainer.setCreate(false);

        ModDirEntryCommand modDirEntryCommand = new ModDirEntryCommand();
        directoryEntrySaveContainer.setModDirEntryCommand(modDirEntryCommand);

        modDirEntryCommand.setUid(directoryEntry.extractDirectoryEntryUid());
        modDirEntryCommand.setTelematikId(telematikID);
        modDirEntryCommand.setDomainId(directoryEntry.extractDirectoryEntryDomainID());

        modDirEntryCommand.setSn(directoryEntry.extractDirectoryEntrySn());
        modDirEntryCommand.setGivenName(directoryEntry.extractDirectoryEntryGivenName());
        modDirEntryCommand.setOtherName(directoryEntry.extractDirectoryEntryOtherName());
        modDirEntryCommand.setDisplayName(directoryEntry.extractDirectoryEntryDisplayName());
        modDirEntryCommand.setCn(directoryEntry.extractDirectoryEntryCn());
        modDirEntryCommand.setTitle(directoryEntry.extractDirectoryEntryTitle());
        modDirEntryCommand.setOrganization(directoryEntry.extractDirectoryEntryOrganization());

        modDirEntryCommand.setStreetAddress(directoryEntry.extractDirectoryEntryStreetAddress());
        modDirEntryCommand.setPostalCode(directoryEntry.extractDirectoryEntryPostalCode());
        modDirEntryCommand.setLocalityName(directoryEntry.extractDirectoryEntryLocalityName());
        modDirEntryCommand.setStateOrProvinceName(EnumStateOrProvinceName.getFromHrText(directoryEntry.extractDirectoryEntryStateOrProvinceName()));
        modDirEntryCommand.setCountryCode(directoryEntry.extractDirectoryEntryCountryCode());

        modDirEntryCommand.setSpecialization(directoryEntry.extractDirectoryEntrySpecialization());

        //entryType handling
        List<String> entryType = directoryEntry.extractDirectoryEntryEntryType();
        if (entryType == null || entryType.isEmpty())  {
            log.error("entryType for id: "+directoryEntry.extractDirectoryEntryTelematikId()+" is not set");
            holderAttrErgebnis.setError(true);
            holderAttrErgebnis.getLog().add("entryType for id: "+directoryEntry.extractDirectoryEntryTelematikId()+" is not set");
        }
        else {
            modDirEntryCommand.setEntryType(EnumEntryType.getFromId(entryType.get(0), log));
        }

        modDirEntryCommand.setMeta(directoryEntry.extractDirectoryEntryMeta());
        modDirEntryCommand.setActive(directoryEntry.extractDirectoryEntryActive());
        modDirEntryCommand.setMaxKomLeAdr(directoryEntry.extractDirectoryEntryMaxKOMLEadr());

        //holder handling
        if (!holder.contains(tiVZDProperties.getAuthId())) {
            holderAttrErgebnis.setError(true);
            holderAttrErgebnis.getLog().add("holder set not valid. do not contains own holder-id: "+tiVZDProperties.getAuthId());
        }
        else if (holder.isEmpty()) {
            holderAttrErgebnis.setError(true);
            holderAttrErgebnis.getLog().add("holder set is empty");
        }
        else if (directoryEntry.extractDirectoryEntryHolder().isEmpty() && !holderAttrCommandContainer.isHandleEmptyHolder()) {
            holderAttrErgebnis.setError(true);
            holderAttrErgebnis.getLog().add("remote holder set is empty");
        }
        else {
            //check holder
            boolean valid = true;
            for (Iterator<String> iterator = holder.iterator(); iterator.hasNext(); ) {
                String holder = iterator.next();
                if (!glossarService.validHolder(holder)) {
                    holderAttrErgebnis.setError(true);
                    holderAttrErgebnis.getLog().add("holder not valid: "+holder);
                    valid = false;
                    break;
                }
            }
            modDirEntryCommand.setHolder(holder);
        }

        return directoryEntrySaveContainer;
    }
}
