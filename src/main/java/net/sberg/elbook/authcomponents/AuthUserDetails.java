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

import lombok.Getter;
import net.sberg.elbook.mandantcmpts.EnumSektor;
import net.sberg.elbook.mandantcmpts.Mandant;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class AuthUserDetails implements UserDetails {

    private final Mandant mandant;
    private boolean admin = false;

    public AuthUserDetails(Mandant mandant) {
        this.mandant = mandant;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (mandant.isSuperAdmin()) {
            authorities.add(new SimpleGrantedAuthority(EnumAuthRole.ROLE_ADMIN.toString()));
            admin = true;
        }
        else {
            //user or head of dpt
            if (mandant.getMandantId() == 0 && mandant.isGoldLizenz()) {
                authorities.add(new SimpleGrantedAuthority(EnumAuthRole.ROLE_GOLD_LICENCE_HEAD_OF_DEPT.toString()));
            } else if (mandant.getMandantId() == 0) {
                authorities.add(new SimpleGrantedAuthority(EnumAuthRole.ROLE_SILVER_LICENCE_HEAD_OF_DEPT.toString()));
            } else if (mandant.getMandantId() > 0 && mandant.isGoldLizenz()) {
                authorities.add(new SimpleGrantedAuthority(EnumAuthRole.ROLE_GOLD_LICENCE_USER.toString()));
            } else if (mandant.getMandantId() > 0) {
                authorities.add(new SimpleGrantedAuthority(EnumAuthRole.ROLE_SILVER_LICENCE_USER.toString()));
            }

            //doctor or pharmacist
            if (mandant.getSektor().equals(EnumSektor.APOTHEKE)) {
                authorities.add(new SimpleGrantedAuthority(EnumAuthRole.ROLE_PHARMACIST.toString()));
            } else if (mandant.getSektor().equals(EnumSektor.ARZTPRAXIS)) {
                authorities.add(new SimpleGrantedAuthority(EnumAuthRole.ROLE_DOCTOR.toString()));
            }
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return mandant.getPasswort();
    }

    @Override
    public String getUsername() {
        return mandant.getNutzername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
