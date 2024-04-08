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
package net.sberg.elbook.logeintragcmpts;

import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.mandantcmpts.EnumSektor;
import net.sberg.elbook.mandantcmpts.Mandant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Service
public class LogEintragService {

    private static final Logger log = LoggerFactory.getLogger(LogEintragService.class);

    @Autowired
    private JdbcGenericDao genericDao;

    private LogEintrag create(String telematikId, Mandant mandant, String businessId) throws Exception {
        LogEintrag logEintrag = new LogEintrag();
        logEintrag.setId(genericDao.getNextId(logEintrag.getClass().getName(), Optional.empty()));
        logEintrag.setDatentyp(mandant.getSektor().equals(EnumSektor.ARZTPRAXIS)? EnumDatatype.ARZTPRAXIS:EnumDatatype.APOTHEKE);
        logEintrag.setTelematikId(telematikId);
        logEintrag.setErstelltAm(LocalDateTime.now());
        logEintrag.setMandantId(mandant.getId());
        logEintrag.setBusinessId(businessId);
        return logEintrag;
    }

    private LogEintragArtikel createArtikel(String entryUid, String certUid, EnumLogEintragArtikelTyp artikelTyp) throws Exception {
        LogEintragArtikel artikel = new LogEintragArtikel();
        artikel.setId(genericDao.getNextId(artikel.getClass().getName(), Optional.empty()));
        artikel.setErstelltAm(LocalDateTime.now());
        artikel.setTyp(artikelTyp);
        artikel.setVzdUid(entryUid);
        artikel.setVzdZertUid(certUid);
        return artikel;
    }

    public void handle(Mandant mandant, String telematikId, String businessId, String entryUid, String certUid, EnumLogEintragArtikelTyp artikelTyp) throws Exception {
        LogEintrag logEintrag = (LogEintrag) genericDao.selectOne(LogEintrag.class.getName(), null, Arrays.asList(new DaoPlaceholderProperty("telematikId", telematikId)));
        if (logEintrag == null) {
            logEintrag = create(telematikId, mandant, businessId);
            genericDao.insert(logEintrag, Optional.empty());
        }
        LogEintragArtikel logEintragArtikel = createArtikel(entryUid, certUid, artikelTyp);
        logEintragArtikel.setLogEintragId(logEintrag.getId());
        genericDao.insert(logEintragArtikel, Optional.empty());
    }

}
