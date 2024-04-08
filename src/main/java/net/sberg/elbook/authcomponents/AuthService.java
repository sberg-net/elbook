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
package net.sberg.elbook.authcomponents;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sberg.elbook.jdbc.DaoPlaceholderProperty;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.mandantcmpts.Mandant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @Value("${elbook.encryptionKeys}")
    private String[] ENC_KEYS;

    @Value("${elbook.defaultAdminUser}")
    private String ADMIN_USERNAME;

    @Value("${elbook.defaultAdminPwd}")
    private String ADMIN_PWD;

    private final JdbcGenericDao genericDao;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() throws Exception {
        Mandant mandant = (Mandant) genericDao.selectOne(Mandant.class.getName(), null,
                List.of(new DaoPlaceholderProperty("nutzername", ADMIN_USERNAME)));
        if (mandant == null) {
            mandant = new Mandant();
            mandant.setPasswort(passwordEncoder.encode(ADMIN_PWD));
            mandant.setName(ADMIN_USERNAME);
            mandant.setNutzername(ADMIN_USERNAME);
            mandant.setAngelegtAm(LocalDateTime.now());
            mandant.setGeaendertAm(LocalDateTime.now());
            mandant.setSuperAdmin(true);
            mandant.setGueltigBis(LocalDate.now().plusYears(100));
            genericDao.insert(mandant, Optional.empty());
        }
    }

    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        try {
            Mandant mandant = (Mandant) genericDao.selectOne(Mandant.class.getName(), null,
                    List.of(new DaoPlaceholderProperty("nutzername", userName)));
            if (mandant != null && mandant.getGueltigBis().isAfter(LocalDate.now())) {

                Mandant superMandant = null;
                if (mandant.getMandantId() > 0) {
                    superMandant = (Mandant) genericDao.selectOne(Mandant.class.getName(), null,
                            List.of(new DaoPlaceholderProperty("id", mandant.getMandantId())));
                    superMandant.decrypt(ENC_KEYS);
                }

                mandant.decrypt(ENC_KEYS);

                if (superMandant != null
                        && superMandant.getVzdAuthId() != null
                        && !superMandant.getVzdAuthId().trim().isEmpty()
                        && superMandant.getVzdAuthSecret() != null
                        && !superMandant.getVzdAuthSecret().trim().isEmpty()) {

                    mandant.setVzdAuthId(superMandant.getVzdAuthId());
                    mandant.setVzdAuthSecret(superMandant.getVzdAuthSecret());
                }

                if (superMandant != null) {
                    mandant.setGoldLizenz(superMandant.isGoldLizenz());
                    mandant.setSektor(superMandant.getSektor());
                }

                return new AuthUserDetails(mandant);
            } else {
                log.error("user not found: "+userName);
                throw new UsernameNotFoundException("user not found: " + userName);
            }
        }
        catch (UsernameNotFoundException e) {
            throw e;
        }
        catch (Exception e) {
            log.error("error on loading user",e);
            throw new UsernameNotFoundException("user not found: " + userName);
        }
    }
}
