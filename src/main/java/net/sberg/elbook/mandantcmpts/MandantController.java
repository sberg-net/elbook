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
package net.sberg.elbook.mandantcmpts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.authcomponents.AuthUserDetails;
import net.sberg.elbook.batchjobcmpts.BatchJob;
import net.sberg.elbook.common.AbstractWebController;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.common.StringUtils;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.DaoProjectionBean;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.logeintragcmpts.LogEintrag;
import net.sberg.elbook.logeintragcmpts.LogEintragArtikel;
import net.sberg.elbook.reportcmpts.Report;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MandantController extends AbstractWebController {

    private final JdbcGenericDao genericDao;
    private final PasswordEncoder passwordEncoder;

    @Value("${elbook.encryptionKeys}")
    private String[] ENC_KEYS;

    @Value("${elbook.defaultAdminUser}")
    private String ADMIN_USERNAME;

    @RequestMapping(value = "/mandant", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String mandantView() {
        return "mandant/mandant";
    }

    @RequestMapping(value = "/mandant/pwdmigration", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String pwdmigration(Authentication authentication) throws Exception {
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        if (!authUserDetails.isAdmin()) {
            return "not allowed";
        }
        List<Mandant> mandants = genericDao.selectMany(
                "SELECT * FROM Mandant m where m.nutzername <> '"+ADMIN_USERNAME+"'",
                Mandant.class.getName(),
                null,
                null
        );
        for (Iterator<Mandant> iterator = mandants.iterator(); iterator.hasNext(); ) {
            Mandant mandant = iterator.next();
            if (StringUtils.isBase64(mandant.getPasswort())) {
                String plainPwd = StringUtils.decrypt(ENC_KEYS, mandant.getPasswort());
                mandant.setPasswort(passwordEncoder.encode(plainPwd));
                genericDao.update(mandant, Optional.empty());
            }
        }
        return "ok";
    }

    @RequestMapping(value = "/mandant/lade/{mandantId}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String lade(Authentication authentication, Model model, @PathVariable int mandantId) throws Exception {
        Mandant mandant = null;
        AuthUserDetails authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        if (mandantId == -1) {
            mandant = new Mandant();
            if (!authUserDetails.isAdmin()) {
                mandant.setMandantId(authUserDetails.getMandant().getId());
                mandant.setTelematikKuerzel(authUserDetails.getMandant().getTelematikKuerzel());
                mandant.setFilternEintraege(authUserDetails.getMandant().isFilternEintraege());
                mandant.setBearbeitenEintraege(authUserDetails.getMandant().isBearbeitenEintraege());
                mandant.setBearbeitenNurFiltereintraege(authUserDetails.getMandant().isBearbeitenNurFiltereintraege());
                mandant.setGueltigBis(authUserDetails.getMandant().getGueltigBis());
            }
        }
        else {
            mandant = (Mandant) genericDao.selectOne(Mandant.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", mandantId)));
            mandant.decrypt(ENC_KEYS);
        }

        model.addAttribute("mandant", mandant);
        return "mandant/mandantFormular";
    }

    @RequestMapping(value = "/mandant/loeschen/{mandantId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String loeschen(@PathVariable String mandantId) throws Exception {
        List subMandantIds = genericDao.selectMany(
            Mandant.class.getName(),
            new DaoProjectionBean(Arrays.asList("id"), null, true),
            Arrays.asList(
                new DaoPlaceholderProperty("mandantId", Integer.parseInt(mandantId))
            )
        );
        for (Iterator iterator = subMandantIds.iterator(); iterator.hasNext(); ) {
            Integer subMandantId = (Integer)iterator.next();
            loeschen(subMandantId);
        }
        loeschen(Integer.parseInt(mandantId));
        return "ok";
    }

    private void loeschen(int mandantId) throws Exception {
        //logeintragartikel
        genericDao.delete("DELETE art FROM "+LogEintragArtikel.class.getSimpleName()+" art\n" +
                "        inner JOIN "+LogEintrag.class.getSimpleName()+" log ON art.logEintragId = log.id\n" +
                "        where log.mandantId = ?", Arrays.asList(new DaoPlaceholderProperty("mandantId", mandantId))
        );

        //logeintrag
        genericDao.delete("DELETE log FROM "+LogEintrag.class.getSimpleName()+" log where log.mandantId = ?", Arrays.asList(new DaoPlaceholderProperty("mandantId", mandantId)));

        //job
        genericDao.delete("DELETE log FROM "+BatchJob.class.getSimpleName()+" log where log.mandantId = ?", Arrays.asList(new DaoPlaceholderProperty("mandantId", mandantId)));

        //report
        genericDao.delete("DELETE log FROM "+ Report.class.getSimpleName()+" log where log.mandantId = ?", Arrays.asList(new DaoPlaceholderProperty("mandantId", mandantId)));

        //delete data/reports/mandantId
        FileSystemUtils.deleteRecursively(new File(ICommonConstants.BASE_DIR + "reports" + File.separator + mandantId));

        //delete all result files -> ICommonConstants.BASE_DIR + File.separator + "jobs" + File.separator + "ergebnisse_"+mandant.getId()+"_"+batchJob.getId()+".json"
        File dir = new File(ICommonConstants.BASE_DIR + "jobs");
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().startsWith("ergebnisse_"+mandantId+"_")) {
                    files[i].delete();
                }
            }
        }

        Mandant mandant = (Mandant) genericDao.selectOne(Mandant.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", mandantId)));
        genericDao.delete(mandant, Optional.empty());
    }

    @RequestMapping(value = "/mandant/uebersicht", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public String uebersicht(Authentication authentication, Model model, String searchValue) throws Exception {

        String mandantIdClause = " nutzername <> '"+ADMIN_USERNAME+"' and mandantId = ";
        AuthUserDetails authUserDetails = (AuthUserDetails)authentication.getPrincipal();
        if (authUserDetails.isAdmin()) {
            mandantIdClause = mandantIdClause + "0";
        }
        else {
            mandantIdClause = mandantIdClause + authUserDetails.getMandant().getId();
        }

        List<MandantReduziert> mandanten = null;
        if (searchValue.equals("")) {
            mandanten = genericDao.selectMany("select id,name,nutzername,sektor,bundesland,mail from Mandant where "+mandantIdClause, MandantReduziert.class.getName(), null, null);
        }
        else {
            mandanten = genericDao.selectMany(
                    "select id,name,nutzername,sektor,bundesland,mail from Mandant where name like ? or nutzername like ? or sektor like ? and "+mandantIdClause,
                    MandantReduziert.class.getName(),
                    null,
                    Arrays.asList(
                            new DaoPlaceholderProperty("name", "%"+searchValue+"%"),
                            new DaoPlaceholderProperty("nutzername", "%"+searchValue+"%"),
                            new DaoPlaceholderProperty("sektor", "%"+searchValue+"%")
                    )
            );
        }
        model.addAttribute("mandanten", mandanten);
        model.addAttribute("superUser", !authUserDetails.isAdmin());
        return "mandant/mandantUebersicht";
    }

    @RequestMapping(value = "/mandant/speichern", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String speichern(@RequestBody Mandant mandant) throws Exception {
        Mandant dbMandant = null;
        boolean insert = false;
        if (mandant.getId() == 0) {
            insert = true;
            dbMandant = new Mandant();
            if (mandant.getId() == 0 && (mandant.getPasswort() == null || mandant.getPasswort().trim().isEmpty())) {
                throw new IllegalStateException("Der Nutzer ist NEU. Bitte geben Sie ein Passwort an!");
            }
            dbMandant.setPasswort(passwordEncoder.encode(mandant.getPasswort()));
            dbMandant.setAngelegtAm(LocalDateTime.now());
        } else {
            dbMandant = (Mandant) genericDao.selectOne(Mandant.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", mandant.getId())));
            dbMandant.decrypt(ENC_KEYS);
        }

        dbMandant.setGeaendertAm(LocalDateTime.now());
        dbMandant.setMail(mandant.getMail());
        dbMandant.setNutzername(mandant.getNutzername());
        dbMandant.setGueltigBis(mandant.getGueltigBis());
        dbMandant.setBearbeitenEintraege(mandant.isBearbeitenEintraege());
        dbMandant.setBearbeitenNurFiltereintraege(mandant.isBearbeitenNurFiltereintraege());
        dbMandant.setFilternEintraege(mandant.isFilternEintraege());
        dbMandant.setTelematikKuerzel(mandant.getTelematikKuerzel());

        if (mandant.getMandantId() == 0) {
            dbMandant.setMandantId(mandant.getMandantId());
            dbMandant.setName(mandant.getName());
            dbMandant.setBundesland(mandant.getBundesland());
            dbMandant.setThreadAnzahl(mandant.getThreadAnzahl());
            dbMandant.setSektor(mandant.getSektor());
            dbMandant.setVzdResourceUri(mandant.getVzdResourceUri());
            dbMandant.setVzdTokenUri(mandant.getVzdTokenUri());
            dbMandant.setGoldLizenz(mandant.isGoldLizenz());
        }
        else {
            Mandant superMandant = (Mandant) genericDao.selectOne(Mandant.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("id", mandant.getMandantId())));
            superMandant.decrypt(ENC_KEYS);

            dbMandant.setName(superMandant.getName());
            dbMandant.setMandantId(superMandant.getId());
            dbMandant.setVzdTokenUri(superMandant.getVzdTokenUri());
            dbMandant.setVzdResourceUri(superMandant.getVzdResourceUri());
            dbMandant.setBundesland(superMandant.getBundesland());
            dbMandant.setSektor(superMandant.getSektor());
            dbMandant.setThreadAnzahl(superMandant.getThreadAnzahl());
            dbMandant.setGoldLizenz(superMandant.isGoldLizenz());
        }

        dbMandant.encrypt(ENC_KEYS);

        List ids = genericDao.selectMany(
                "select id from Mandant where lower(nutzername) = ?",
                null,
                new DaoProjectionBean(null, null, true),
                Arrays.asList(
                    new DaoPlaceholderProperty("nutzername", dbMandant.getNutzername().toLowerCase())
                )
        );

        for (Iterator iterator = ids.iterator(); iterator.hasNext(); ) {
            Integer dbId =  (Integer)iterator.next();
            if (dbId != dbMandant.getId()) {
                throw new IllegalStateException("Nutzer ist schon vorhanden");
            }
        }

        if (insert) {
            genericDao.insert(dbMandant, Optional.empty());
        }
        else {
            genericDao.update(dbMandant, Optional.empty());
        }

        return "ok";
    }

    @RequestMapping(value = "/mandant/einstellungen", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String laden() throws Exception {
        return "mandant/mandantEinstellungen";
    }

    @RequestMapping(value = "/mandant/is2FAenabled", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public boolean is2FAEnabled(@RequestBody Map map) throws Exception {
        String nutzername = (String)map.get("username");
        Mandant mandant = null;
        try {
            mandant = (Mandant) genericDao.selectOne(Mandant.class.getName(), null,
                    List.of(new DaoPlaceholderProperty("nutzername", nutzername)));
        } catch (Exception e) {
            log.error("Error getting user: " + nutzername);
        }
        if (mandant == null) {
            log.warn("user not found:" + nutzername);
            return false;
        }
        return mandant.isUsing2FA();
    }

    @RequestMapping(value = "/mandant/enable2FA", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public TwoFAEnableResponse enable2FA() throws Exception {
        TwoFAEnableResponse twoFAActivateResponse = new TwoFAEnableResponse();
        Authentication curAuth = SecurityContextHolder.getContext().getAuthentication();
        AuthUserDetails authUserDetails = (AuthUserDetails) curAuth.getPrincipal();
        Mandant mandant = authUserDetails.getMandant();
        String secret2FA = Base32.random();
        twoFAActivateResponse.setQrCodeLink(generateQRUrl(mandant.getNutzername(), secret2FA));
        twoFAActivateResponse.setSecret2FA(secret2FA);
        return twoFAActivateResponse;
    }

    @RequestMapping(value = "/mandant/enableFinish2FA", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public boolean enableFinish2FA(@RequestBody TwoFAEnableFinishRequest twoFAEnableFinishRequest) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthUserDetails authUserDetails = (AuthUserDetails) auth.getPrincipal();
        Mandant mandant = authUserDetails.getMandant();

        if (!passwordEncoder.matches(twoFAEnableFinishRequest.getCurrentElbookPwd(), mandant.getPasswort())) {
            log.error("enableFinish2FA - password invalid: " + auth.getName());
            throw new IllegalStateException("enableFinish2FA - password invalid: " + auth.getName());
        }

        String verificationCode = twoFAEnableFinishRequest.getTwoFaCode();
        try {
            Long.parseLong(verificationCode);
        }
        catch (Exception e) {
            log.error("enableFinish2FA - Invalid 2FA validation code: " + auth.getName());
            throw new IllegalStateException("enableFinish2FA - Invalid 2FA validation code: " + auth.getName());
        }
        Totp totp = new Totp(twoFAEnableFinishRequest.getSecret2FA());
        if (!totp.verify(verificationCode)) {
            log.error("enableFinish2FA - Invalid 2FA validation code: " + auth.getName());
            throw new IllegalStateException("enableFinish2FA - Invalid 2FA validation code: " + auth.getName());
        }

        mandant.setUsing2FA(true);
        mandant.setSecret2FA(twoFAEnableFinishRequest.getSecret2FA());

        mandant.encrypt(ENC_KEYS);
        genericDao.update(mandant, Optional.empty());
        mandant.decrypt(ENC_KEYS);

        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(authUserDetails, auth.getCredentials(), authUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        return true;
    }

    @RequestMapping(value = "/mandant/disableFinish2FA", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public boolean disableFinish2FA(@RequestBody TwoFADisableFinishRequest twoFADisableFinishRequest) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthUserDetails authUserDetails = (AuthUserDetails) auth.getPrincipal();
        Mandant mandant = authUserDetails.getMandant();

        if (!passwordEncoder.matches(twoFADisableFinishRequest.getCurrentElbookPwd(), mandant.getPasswort())) {
            log.error("disableFinish2FA - password invalid: " + auth.getName());
            throw new IllegalStateException("disableFinish2FA - password invalid: " + auth.getName());
        }

        mandant.setUsing2FA(false);
        mandant.setSecret2FA(null);

        mandant.encrypt(ENC_KEYS);
        genericDao.update(mandant, Optional.empty());
        mandant.decrypt(ENC_KEYS);

        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(authUserDetails, auth.getCredentials(), authUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        return true;
    }

    private String generateQRUrl(String nutzerName, String secret2FA) throws UnsupportedEncodingException {
        final String QR_PREFIX = "https://quickchart.io/chart?cht=qr&chs=200x200&chl=";
        final String APP_NAME = "elBook";
        return QR_PREFIX + URLEncoder.encode(String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", APP_NAME, nutzerName, secret2FA, APP_NAME),"UTF-8");
    }
}
