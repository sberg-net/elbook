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
package net.sberg.elbook.verzeichnisdienstcmpts;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.authcomponents.AuthUserDetails;
import net.sberg.elbook.common.AbstractWebController;
import net.sberg.elbook.common.FileUtils;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.logeintragcmpts.EnumLogEintragArtikelTyp;
import net.sberg.elbook.logeintragcmpts.LogEintragService;
import net.sberg.elbook.logeintragcmpts.LogEntryContainer;
import net.sberg.elbook.mandantcmpts.Mandant;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.*;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.AbstractCommandResultCallbackHandler;
import net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler.DefaultCommandResultCallbackHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class VerzeichnisdienstController extends AbstractWebController {

    private final LogEintragService logEintragService;
    private final VerzeichnisdienstService verzeichnisdienstService;
    private final JdbcGenericDao genericDao;

    @Value("${elbook.encryptionKeys}")
    private String[] ENC_KEYS;

    @RequestMapping(value = "/verzeichnisdienst", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String verzeichnisDienstView() {
        return "verzeichnisdienst/verzeichnisdienst";
    }

    @RequestMapping(value = "/verzeichnisdienst/lade/{uid}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String lade(Authentication authentication, Model model, @PathVariable String uid) {

        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();

        try {
            if (uid.equals("-1")) {
                VzdEntryWrapper vzdEntryWrapper = new VzdEntryWrapper(authUserDetails.getMandant().getTiVZDProperties());
                vzdEntryWrapper.setDirectoryEntryUid("-1");

                model.addAttribute("fehlernachricht", "");
                model.addAttribute("vzdeintrag", new DirectoryEntryContainer(vzdEntryWrapper, null, authUserDetails.getMandant()));
            }
            else {

                List kuerzelL  = new ArrayList();
                if (authUserDetails.getMandant().getTelematikKuerzel() != null && !authUserDetails.getMandant().getTelematikKuerzel().trim().isEmpty()) {
                    kuerzelL = Arrays.asList(authUserDetails.getMandant().getTelematikKuerzel().split(","));
                }

                VzdEntryWrapper directoryEntry = verzeichnisdienstService.ladeByUid(authUserDetails.getMandant(), uid);
                if (directoryEntry == null) {
                    model.addAttribute("fehlernachricht", "Eintrag mit der ID="+uid+" kann nicht gefunden werden");
                    model.addAttribute("vzdeintrag", new DirectoryEntryContainer(true, null, kuerzelL, authUserDetails.getMandant()));
                }
                else {

                    DirectoryEntryContainer directoryEntryContainer = new DirectoryEntryContainer(directoryEntry, kuerzelL, authUserDetails.getMandant());
                    TiVZDProperties tiVZDProperties = authUserDetails.getMandant().getTiVZDProperties();
                    List<VzdEntryWrapper> logEntries = verzeichnisdienstService.logEntriesForUid(tiVZDProperties, uid);
                    for (VzdEntryWrapper logEntry : logEntries) {
                        LogEntryContainer logEntryContainer = new LogEntryContainer(logEntry);
                        directoryEntryContainer.getLogEntries().add(logEntryContainer.createSummary());
                    }

                    model.addAttribute("fehlernachricht", "");
                    model.addAttribute("vzdeintrag", directoryEntryContainer);
                }
            }
        }
        catch (Exception e) {
            log.error("error on loading the entry with the uid: "+uid+" - mandant: "+authUserDetails.getMandant().getId()+" - "+authUserDetails.getMandant().getName(), e);
            model.addAttribute("vzdeintrag", new DirectoryEntryContainer(true, null, null, authUserDetails.getMandant()));
            if (e.getCause() != null) {
                model.addAttribute("fehlernachricht", "Fehler beim Laden des Eintrages mit der ID=" + uid + " - " + e.getMessage() + " - " + e.getCause().getMessage());
            }
            else {
                model.addAttribute("fehlernachricht", "Fehler beim Laden des Eintrages mit der ID=" + uid + " - " + e.getMessage());
            }
        }

        return "verzeichnisdienst/verzeichnisdienstFormular";
    }

    @RequestMapping(value = "/verzeichnisdienst/zertifikate/speichern/{uid}/{telematikId}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String speichernZertifikat(Authentication authentication, @RequestBody String certContent, @PathVariable String uid, @PathVariable String telematikId) throws Exception {
        telematikId = new String(Base64.getDecoder().decode(telematikId.getBytes()), StandardCharsets.UTF_8);
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        TiVZDProperties tiVZDProperties = authUserDetails.getMandant().getTiVZDProperties();
        List dnNameL = verzeichnisdienstService.speichernZertifikat(tiVZDProperties, certContent, uid);
        logEintragService.handle(authUserDetails.getMandant(), telematikId, "", uid, ((VzdEntryWrapper)dnNameL.get(0)).extractDistinguishedNameCn(), EnumLogEintragArtikelTyp.MANUAL_CERT_INSERT);
        return "ok";
    }

    @RequestMapping(value = "/verzeichnisdienst/zertifikate/loeschen/{uid}/{certUid}/{telematikId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String loeschenZertifikat(Authentication authentication, @PathVariable String uid, @PathVariable String certUid, @PathVariable String telematikId) throws Exception {
        telematikId = new String(Base64.getDecoder().decode(telematikId.getBytes()), StandardCharsets.UTF_8);
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        TiVZDProperties tiVZDProperties = authUserDetails.getMandant().getTiVZDProperties();
        verzeichnisdienstService.loeschenZertifikat(tiVZDProperties, uid, certUid);
        logEintragService.handle(authUserDetails.getMandant(), telematikId, "", uid, certUid, EnumLogEintragArtikelTyp.MANUAL_CERT_DELETE);
        return "ok";
    }

    @RequestMapping(value = "/verzeichnisdienst/zertifikate/lade/{uid}/{telematikId}/{addNewZert}/{zertSize}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String ladeZertifikate(Authentication authentication, Model model, @PathVariable String uid, @PathVariable String telematikId, @PathVariable boolean addNewZert, @PathVariable int zertSize) {

        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();

        try {
            TiVZDProperties tiVZDProperties = authUserDetails.getMandant().getTiVZDProperties();
            List<VzdEntryWrapper> entries = verzeichnisdienstService.ladeZertifikate(tiVZDProperties, uid);

            List kuerzelL  = new ArrayList();
            if (authUserDetails.getMandant().getTelematikKuerzel() != null && !authUserDetails.getMandant().getTelematikKuerzel().trim().isEmpty()) {
                kuerzelL = Arrays.asList(authUserDetails.getMandant().getTelematikKuerzel().split(","));
            }

            VzdEntryWrapper directoryEntry = verzeichnisdienstService.ladeByUid(authUserDetails.getMandant(), uid);
            DirectoryEntryContainer directoryEntryContainer = new DirectoryEntryContainer(directoryEntry, kuerzelL, authUserDetails.getMandant());

            if (!directoryEntryContainer.isCheckSuccess()) {
                log.error("security check - error on lading the certificates for the uid: "+uid+", mandant: "+authUserDetails.getMandant().getId());
                model.addAttribute("fehlernachricht", "Sicherheitscheck. Sie dürfen die Zertifikate nicht laden. Fehler beim Laden der Zertifikate für die ID="+uid);

                model.addAttribute("vzdeintragZertikate", new ArrayList<>());
                model.addAttribute("uid", uid);
                model.addAttribute("telematikId", telematikId);
                model.addAttribute("editierbar", false);
            }
            else {
                model.addAttribute("editierbar", directoryEntryContainer.isEditable());

                List<VzdEntryWrapper> toRemoveEntries = new ArrayList<>();
                Map<String, VerzeichnisdienstZertifikat> zertInfo = new HashMap<>();
                for (VzdEntryWrapper next : entries) {
                    if (next.extractUserCertificateContent() == null || next.extractUserCertificateContent().trim().isEmpty()) {
                        toRemoveEntries.add(next);
                    } else {
                        VerzeichnisdienstZertifikat zertifikat = verzeichnisdienstService.erstelle(next);
                        if (zertifikat != null) {
                            zertInfo.put(next.extractUserCertificateDnCn(), zertifikat);
                        }
                    }
                }
                entries.removeAll(toRemoveEntries);

                if (addNewZert) {
                    int size = zertSize - entries.size() + 1;
                    for (int i = 1; i <= size; i++) {
                        VzdEntryWrapper newUserCertificate = new VzdEntryWrapper(tiVZDProperties);
                        newUserCertificate.setUserCertificateDnCn("");
                        entries.add(newUserCertificate);
                    }
                }

                model.addAttribute("zertInfo", zertInfo);
                model.addAttribute("vzdeintragZertikate", entries);
                model.addAttribute("uid", uid);
                model.addAttribute("telematikId", telematikId);
            }
        }
        catch (Exception e) {
            log.error("error on lading the certificates for the uid: "+uid+" - mandant: "+authUserDetails.getMandant().getId()+" - "+authUserDetails.getMandant().getName(), e);
            if (e.getCause() != null) {
                model.addAttribute("fehlernachricht", "Fehler beim Laden der Zertifikate für die ID=" + uid + " - " + e.getMessage() + " - " + e.getCause().getMessage());
            }
            else {
                model.addAttribute("fehlernachricht", "Fehler beim Laden der Zertifikate für die ID=" + uid + " - " + e.getMessage());
            }
            model.addAttribute("editierbar", false);
            model.addAttribute("vzdeintragZertikate", new ArrayList<>());
            model.addAttribute("uid", uid);
            model.addAttribute("telematikId", telematikId);
        }

        return "verzeichnisdienst/verzeichnisdienstZertifikateFormular";
    }

    @RequestMapping(value = "/verzeichnisdienst/loeschen/{uid}/{telematikId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String loeschen(Authentication authentication, @PathVariable String uid, @PathVariable String telematikId) throws Exception {
        telematikId = new String(Base64.getDecoder().decode(telematikId.getBytes()), StandardCharsets.UTF_8);
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        TiVZDProperties tiVZDProperties = authUserDetails.getMandant().createAndGetTiVZDProperties(verzeichnisdienstService);
        verzeichnisdienstService.loeschen(authUserDetails.getMandant(), tiVZDProperties, uid);
        logEintragService.handle(authUserDetails.getMandant(), telematikId, "", uid, null, EnumLogEintragArtikelTyp.MANUAL_DELETE);
        return "ok";
    }

    @SuppressWarnings("resource")
    @RequestMapping(value = "/verzeichnisdienst/uebersicht/herunterladen/{csvFileUuid}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void uebersichtCsvExport(HttpServletResponse response, Authentication authentication, @PathVariable String csvFileUuid) throws Exception {

        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        Mandant mandant = authUserDetails.getMandant();
        String fileName = ICommonConstants.BASE_DIR + "vzdCsvExport" + File.separator + mandant.getId() + File.separator + mandant.getId()+"_"+csvFileUuid+".csv";

        response.setHeader("Content-Disposition", "attachment; filename=" + new File(fileName).getName());
        response.setContentType("text/csv");

        InputStream inputStream = new FileInputStream(fileName);
        inputStream.transferTo(response.getOutputStream());
        response.flushBuffer();

        new File(fileName).delete();
    }

    @RequestMapping(value = "/verzeichnisdienst/uebersicht/{makeCSVExport}/{initalLoading}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersicht(Authentication authentication, Model model, @RequestBody VzdOverviewSearchContainer vzdOverviewSearchContainer, @PathVariable boolean makeCSVExport, @PathVariable boolean initalLoading) {

        if (vzdOverviewSearchContainer.getReadDirSyncEntryFaAttributesCommand() != null && vzdOverviewSearchContainer.getReadDirSyncEntryFaAttributesCommand().isEmpty()) {
            vzdOverviewSearchContainer.setReadDirSyncEntryFaAttributesCommand(null);
        }
        else if (vzdOverviewSearchContainer.getReadDirSyncEntryFaAttributesCommand() != null && !vzdOverviewSearchContainer.getReadDirSyncEntryFaAttributesCommand().isEmpty()) {
            vzdOverviewSearchContainer.setReadDirSyncEntryCommand(null);
        }

        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        Mandant mandant = authUserDetails.getMandant();
        model.addAttribute("bearbeitenEintraege", mandant.isBearbeitenEintraege());
        model.addAttribute("bearbeitenNurFiltereintraege", mandant.isBearbeitenNurFiltereintraege());
        model.addAttribute("filternEintraege", mandant.isFilternEintraege());
        model.addAttribute("telematikKuerzel", mandant.getTelematikKuerzel());
        model.addAttribute("csvExportErstellen", makeCSVExport);

        try {
            if (mandant.authInfoIsBlank()) {
                model.addAttribute("mandant", mandant);
                model.addAttribute("sektor", mandant.getSektor());
                return "verzeichnisdienst/verzeichnisdienstAuthFormular";
            }

            TiVZDProperties tiVZDProperties = null;
            try {
                tiVZDProperties = mandant.createAndGetTiVZDProperties(verzeichnisdienstService);
            }
            catch (Exception e) {
                model.addAttribute("infoObject", null);
                throw e;
            }
            model.addAttribute("infoObject", tiVZDProperties.getInfoObject());

            List<DirectoryEntryContainer> searchResults = new ArrayList<DirectoryEntryContainer>();
            if (initalLoading) {
                model.addAttribute("vzdeintraege", new ArrayList<DirectoryEntryContainer>());
                model.addAttribute("sektor", mandant.getSektor());
                return "verzeichnisdienst/verzeichnisdienstUebersicht";
            }

            boolean error = false;
            AbstractCommandResultCallbackHandler.ResultReason errorReason = null;

            List kuerzelL  = new ArrayList();
            if (mandant.getTelematikKuerzel() != null && !mandant.getTelematikKuerzel().trim().isEmpty()) {
                kuerzelL = Arrays.asList(mandant.getTelematikKuerzel().split(","));
            }

            if (vzdOverviewSearchContainer.getReadDirSyncEntryCommand() != null && vzdOverviewSearchContainer.getReadDirSyncEntryCommand().getTelematikIdSubstr() != null && !vzdOverviewSearchContainer.getReadDirSyncEntryCommand().getTelematikIdSubstr().trim().isEmpty()) {
                String[] searchKuerzelL = vzdOverviewSearchContainer.getReadDirSyncEntryCommand().getTelematikIdSubstr().split(",");
                for (String kuerzel : searchKuerzelL) {
                    if (kuerzel.trim().isEmpty()) {
                        continue;
                    }

                    vzdOverviewSearchContainer.getReadDirSyncEntryCommand().setTelematikIdSubstr(kuerzel.trim());

                    errorReason = uebersicht(tiVZDProperties, vzdOverviewSearchContainer, mandant, searchResults, kuerzelL);
                    if (errorReason != null && !errorReason.getResultReason().equals(AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS)) {
                        error = true;
                        break;
                    }
                }
            }
            else {
                errorReason = uebersicht(tiVZDProperties,vzdOverviewSearchContainer,mandant,searchResults,kuerzelL);
                if (errorReason != null && !errorReason.getResultReason().equals(AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS)) {
                    error = true;
                }
            }

            if (error || searchResults.isEmpty()) {
                model.addAttribute("vzdeintraege", searchResults);
                model.addAttribute("sektor", mandant.getSektor());
                model.addAttribute("suchergebnisFehler", errorReason != null?errorReason.getResultReason():AbstractCommandResultCallbackHandler.ResultReason.NO_SEARCH_RESULTS);
                return "verzeichnisdienst/verzeichnisdienstUebersicht";
            }

            model.addAttribute("vzdeintraege", searchResults);
            model.addAttribute("sektor", mandant.getSektor());

            if (makeCSVExport) {
                String uuid = verzeichnisdienstService.createCSVExport(mandant, searchResults);
                model.addAttribute("csvExportUuid", uuid);
            }
        }
        catch (Exception e) {
            log.error("error on loading the overview - mandant: "+mandant.getId()+" - "+mandant.getName(), e);
            if (e.getCause() != null) {
                model.addAttribute("fehlernachricht", "Fehler beim Laden der Übersicht. Bitte probieren Sie es erneut!: " + e.getMessage() + " - " + e.getCause().getMessage());
            }
            else {
                model.addAttribute("fehlernachricht", "Fehler beim Laden der Übersicht. Bitte probieren Sie es erneut!: " + e.getMessage());
            }
            model.addAttribute("sektor", mandant.getSektor());
            model.addAttribute("vzdeintraege", new ArrayList<>());
        }

        return "verzeichnisdienst/verzeichnisdienstUebersicht";
    }

    private AbstractCommandResultCallbackHandler.ResultReason uebersicht(
            TiVZDProperties tiVZDProperties,
            VzdOverviewSearchContainer vzdOverviewSearchContainer,
            Mandant mandant,
            List<DirectoryEntryContainer> searchResults,
            List kuerzelL
    ) throws Exception {

        PagingInfo pagingInfo = new PagingInfo();
        pagingInfo.setOneTimePaging(false);
        pagingInfo.setPagingSize(200);
        pagingInfo.setPagingCookie("");
        pagingInfo.setMaxSize(vzdOverviewSearchContainer.getReadDirSyncEntryCommand()!=null?5000:300);

        if (vzdOverviewSearchContainer.getReadDirSyncEntryCommand() != null) {
            vzdOverviewSearchContainer.getReadDirSyncEntryCommand().setPagingInfo(pagingInfo);
        }
        else {
            vzdOverviewSearchContainer.getReadDirSyncEntryFaAttributesCommand().setPagingInfo(pagingInfo);
        }
        String id = vzdOverviewSearchContainer.getReadDirSyncEntryCommand() != null?vzdOverviewSearchContainer.getReadDirSyncEntryCommand().getId():vzdOverviewSearchContainer.getReadDirSyncEntryFaAttributesCommand().getId();

        DefaultCommandResultCallbackHandler commandResultCallbackHandler = verzeichnisdienstService.uebersicht(tiVZDProperties, vzdOverviewSearchContainer);
        List<VzdEntryWrapper> entries = commandResultCallbackHandler.getDirectoryEntries(id);

        AbstractCommandResultCallbackHandler.ResultReason resultReason = null;
        if ((entries == null || entries.isEmpty()) && !commandResultCallbackHandler.getExceptions(id).isEmpty()) {
            throw commandResultCallbackHandler.getExceptions(id).get(0);
        }
        if (!commandResultCallbackHandler.getResultReasons(id).isEmpty()) {
            resultReason = commandResultCallbackHandler.getResultReasons(id).get(0);
        }

        if (resultReason != null && !resultReason.getResultReason().equals(AbstractCommandResultCallbackHandler.ResultReason.TOO_MANY_RESULTS)) {
            return resultReason;
        }

        if (entries != null) {
            for (VzdEntryWrapper entry : entries) {
                DirectoryEntryContainer directorEntryContainer = new DirectoryEntryContainer(entry, kuerzelL, mandant);
                if (!directorEntryContainer.isFillSuccess()) {
                    continue;
                }
                searchResults.add(directorEntryContainer);
            }

            if (searchResults.size() > pagingInfo.getMaxSize()) {
                resultReason = new AbstractCommandResultCallbackHandler.ResultReason(false, AbstractCommandResultCallbackHandler.ResultReason.TOO_MANY_RESULTS);
            }
        }

        return resultReason;
    }

    @RequestMapping(value = "/verzeichnisdienst/authspeichern/{speichereDauerhaft}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String authspeichern(Authentication authentication, @RequestBody Mandant mandant, @PathVariable boolean speichereDauerhaft) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        Mandant dbMandant = authUserDetails.getMandant();
        dbMandant.setVzdAuthId(mandant.getVzdAuthId());
        dbMandant.setVzdAuthSecret(mandant.getVzdAuthSecret());
        if (speichereDauerhaft) {
            dbMandant.encrypt(ENC_KEYS);
            genericDao.update(dbMandant, Optional.empty());
            dbMandant.decrypt(ENC_KEYS);
        }
        return "ok";
    }

    @RequestMapping(value = "/verzeichnisdienst/authloeschen", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String authloeschen(Authentication authentication) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        Mandant dbMandant = authUserDetails.getMandant();
        dbMandant.setVzdAuthId("");
        dbMandant.setVzdAuthSecret("");
        dbMandant.encrypt(ENC_KEYS);
        genericDao.update(dbMandant, Optional.empty());
        dbMandant.decrypt(ENC_KEYS);
        return "ok";
    }

    @RequestMapping(value = "/verzeichnisdienst/speichern", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String speichern(Authentication authentication, @RequestBody Map updateCommand) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        TiVZDProperties tiVZDProperties = authUserDetails.getMandant().getTiVZDProperties();

        List kuerzelL  = new ArrayList();
        if (authUserDetails.getMandant().getTelematikKuerzel() != null && !authUserDetails.getMandant().getTelematikKuerzel().trim().isEmpty()) {
            kuerzelL = Arrays.asList(authUserDetails.getMandant().getTelematikKuerzel().split(","));
        }

        String telematikId = (String)updateCommand.get("telematikId");
        DirectoryEntrySaveContainer directorEntrySaveContainer = new DirectoryEntrySaveContainer();
        if (telematikId != null && !telematikId.trim().isEmpty()) {
            VzdEntryWrapper dbDirectoryEntry = verzeichnisdienstService.ladeByTelematikId(authUserDetails.getMandant(), telematikId);
            if (dbDirectoryEntry == null) {
                AddDirEntryCommand addDirEntryCommand = new ObjectMapper().convertValue(updateCommand, AddDirEntryCommand.class);
                addDirEntryCommand.setUid(null);
                directorEntrySaveContainer.setCreate(true);
                directorEntrySaveContainer.setAddDirEntryCommand(addDirEntryCommand);
            }
            else {
                DirectoryEntryContainer directorEntryContainer = new DirectoryEntryContainer(dbDirectoryEntry, kuerzelL, authUserDetails.getMandant());
                if (!directorEntryContainer.isEditable()) {
                    log.error("Sicherheitscheck: Eintrag mit der TelematikID: "+telematikId+" kann nicht gespeichert werden. Der Mandant "+authUserDetails.getMandant().getId()+" hat das Kürzel: "+authUserDetails.getMandant().getTelematikKuerzel());
                    throw new IllegalStateException("Sicherheitscheck: Eintrag mit der TelematikID: "+telematikId+" kann nicht gespeichert werden. Die Kürzel: "+authUserDetails.getMandant().getTelematikKuerzel()+" sind erlaubt");
                }
                if (!dbDirectoryEntry.extractDirectoryEntryHolder().isEmpty() && !dbDirectoryEntry.extractDirectoryEntryHolder().contains(authUserDetails.getMandant().getVzdAuthId())) {
                    log.error("Sicherheitscheck: Eintrag mit der TelematikID: "+telematikId+" kann nicht gespeichert werden. Sie sind nicht der Besitzer des Eintrages");
                    throw new IllegalStateException("Sicherheitscheck: Eintrag mit der TelematikID: "+telematikId+" kann nicht gespeichert werden. Sie sind nicht der Besitzer des Eintrages");
                }

                ModDirEntryCommand modDirEntryCommand = new ObjectMapper().convertValue(updateCommand, ModDirEntryCommand.class);

                //set disabled form values
                modDirEntryCommand.setHolder(dbDirectoryEntry.extractDirectoryEntryHolder());
                modDirEntryCommand.setActive(dbDirectoryEntry.extractDirectoryEntryActive());
                modDirEntryCommand.setMaxKomLeAdr(dbDirectoryEntry.extractDirectoryEntryMaxKOMLEadr());
                modDirEntryCommand.setMeta(dbDirectoryEntry.extractDirectoryEntryMeta());

                directorEntrySaveContainer.setCreate(false);
                directorEntrySaveContainer.setModDirEntryCommand(modDirEntryCommand);
            }
        }
        else {
            log.error("Bitte eine TelematikId angeben");
            throw new IllegalStateException("Bitte eine TelematikId angeben");
        }

        try {
            List erg = verzeichnisdienstService.speichern(tiVZDProperties, directorEntrySaveContainer);

            AbstractCommand command = directorEntrySaveContainer.isCreate() ? directorEntrySaveContainer.getAddDirEntryCommand() : directorEntrySaveContainer.getModDirEntryCommand();
            logEintragService.handle(
                authUserDetails.getMandant(),
                command.getTelematikId(),
                "",
                command.command().equals(EnumCommand.ADD_DIR_ENTRY) ? ((VzdEntryWrapper) erg.get(0)).extractDistinguishedNameUid() : command.getUid(),
                null,
                command.command().equals(EnumCommand.ADD_DIR_ENTRY) ? EnumLogEintragArtikelTyp.MANUAL_INSERT : EnumLogEintragArtikelTyp.MANUAL_UPDATE
            );
        }
        catch (Exception e) {
            log.error("error on saving vzd entry", e);
            throw new IllegalStateException("Beim Speichern ist ein Fehler aufgetreten");
        }

        return "ok";
    }

    @RequestMapping(value = "/verzeichnisdienst/statusaendern", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String statusaendern(Authentication authentication, @RequestBody SwitchStateDirEntryCommand switchStateDirEntryCommand) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        TiVZDProperties tiVZDProperties = authUserDetails.getMandant().getTiVZDProperties();

        List kuerzelL  = new ArrayList();
        if (authUserDetails.getMandant().getTelematikKuerzel() != null && !authUserDetails.getMandant().getTelematikKuerzel().trim().isEmpty()) {
            kuerzelL = Arrays.asList(authUserDetails.getMandant().getTelematikKuerzel().split(","));
        }

        if (switchStateDirEntryCommand.getTelematikId() != null && !switchStateDirEntryCommand.getTelematikId().trim().isEmpty()) {
            VzdEntryWrapper dbDirectoryEntry = verzeichnisdienstService.ladeByTelematikId(authUserDetails.getMandant(), switchStateDirEntryCommand.getTelematikId());
            if (dbDirectoryEntry == null) {
                log.error("Eintrag mit der TelematikId "+switchStateDirEntryCommand.getTelematikId()+" ist nicht vorhanden");
                throw new IllegalStateException("Eintrag mit der TelematikId "+switchStateDirEntryCommand.getTelematikId()+" ist nicht vorhanden");
            }
            else {
                DirectoryEntryContainer directorEntryContainer = new DirectoryEntryContainer(dbDirectoryEntry, kuerzelL, authUserDetails.getMandant());
                if (!directorEntryContainer.isEditable()) {
                    log.error("Sicherheitscheck: Eintrag mit der TelematikID: "+switchStateDirEntryCommand.getTelematikId()+" kann nicht gespeichert werden. Der Mandant "+authUserDetails.getMandant().getId()+" hat das Kürzel: "+authUserDetails.getMandant().getTelematikKuerzel());
                    throw new IllegalStateException("Sicherheitscheck: Eintrag mit der TelematikID: "+switchStateDirEntryCommand.getTelematikId()+" kann nicht gespeichert werden. Die Kürzel: "+authUserDetails.getMandant().getTelematikKuerzel()+" sind erlaubt");
                }
                if (!dbDirectoryEntry.extractDirectoryEntryHolder().isEmpty() && !dbDirectoryEntry.extractDirectoryEntryHolder().contains(authUserDetails.getMandant().getVzdAuthId())) {
                    log.error("Sicherheitscheck: Eintrag mit der TelematikID: "+switchStateDirEntryCommand.getTelematikId()+" kann nicht gespeichert werden. Sie sind nicht der Besitzer des Eintrages");
                    throw new IllegalStateException("Sicherheitscheck: Eintrag mit der TelematikID: "+switchStateDirEntryCommand.getTelematikId()+" kann nicht gespeichert werden. Sie sind nicht der Besitzer des Eintrages");
                }
            }
        }
        else {
            log.error("Bitte eine TelematikId angeben");
            throw new IllegalStateException("Bitte eine TelematikId angeben");
        }

        try {
            verzeichnisdienstService.statusAendern(tiVZDProperties, switchStateDirEntryCommand);
            logEintragService.handle(
                authUserDetails.getMandant(),
                switchStateDirEntryCommand.getTelematikId(),
                "",
                switchStateDirEntryCommand.getUid(),
                null,
                EnumLogEintragArtikelTyp.MANUAL_STATE_CHANGED
            );
        }
        catch (Exception e) {
            log.error("error on switching vzd entry state", e);
            throw new IllegalStateException("Beim Ändern des Status ist ein Fehler aufgetreten");
        }

        return "ok";
    }

    @SuppressWarnings("resource")
    @RequestMapping(value = "/verzeichnisdienst/herunterladen/zertifikat/{uid}/{certUid}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void herunterladenZertifikat(HttpServletResponse response, Authentication authentication, @PathVariable String uid, @PathVariable String certUid) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        TiVZDProperties tiVZDProperties = authUserDetails.getMandant().getTiVZDProperties();

        VzdEntryWrapper userCertificate = verzeichnisdienstService.ladeZertifikat(tiVZDProperties, uid, certUid);
        String certStr = "-----BEGIN CERTIFICATE-----" + "\n"
                + userCertificate.extractUserCertificateContent() + "\n"
                + "-----END CERTIFICATE-----";

        File file = new File(System.getProperty("java.io.tmpdir")+File.separator+"cert_"+certUid+System.nanoTime()+".crt");

        FileUtils.writeToFile(certStr, file.getAbsolutePath());

        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        response.setContentType("application/x-x509-user-cert");

        InputStream inputStream = new FileInputStream(file);
        inputStream.transferTo(response.getOutputStream());
        response.flushBuffer();
    }

    @SuppressWarnings("resource")
    @RequestMapping(value = "/verzeichnisdienst/herunterladen/{uid}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void herunterladen(HttpServletResponse response, Authentication authentication, @PathVariable String uid) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();

        VzdEntryWrapper directoryEntry = verzeichnisdienstService.ladeByUid(authUserDetails.getMandant(), uid);
        File file = new File(System.getProperty("java.io.tmpdir")+File.separator+"vzd_"+uid+System.nanoTime()+".json");
        directoryEntry.writeDirectoryEntry(file);

        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        response.setContentType("application/json");

        InputStream inputStream = new FileInputStream(file);
        inputStream.transferTo(response.getOutputStream());
        response.flushBuffer();
    }

}
