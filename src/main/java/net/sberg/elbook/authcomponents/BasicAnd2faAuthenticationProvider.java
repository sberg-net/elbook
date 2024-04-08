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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasicAnd2faAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {

        boolean skip2FA = false;

        AuthUserDetails authUserDetails = (AuthUserDetails) authService.loadUserByUsername(auth.getName());

        if (!passwordEncoder.matches(auth.getCredentials().toString(), authUserDetails.getMandant().getPasswort())) {
            throw new BadCredentialsException("password invalid: " + auth.getName());
        }

        // disable 2FA für REST Request
        if (!(auth.getDetails() instanceof CustomWebAuthenticationDetails)) {
            skip2FA = true;
        }
        if (authUserDetails.getMandant().isUsing2FA() && !skip2FA) {
            String verificationCode = ((CustomWebAuthenticationDetails) auth.getDetails()).getVerificationCode();
            Totp totp = new Totp(authUserDetails.getMandant().getSecret2FA());
            if (!isValidLong(verificationCode) || !totp.verify(verificationCode)) {
                throw new BadCredentialsException("Invalid 2FA validation code: " + auth.getName());
            }
        }

        UsernamePasswordAuthenticationToken outgoingAuth =
                new UsernamePasswordAuthenticationToken(authUserDetails, auth.getCredentials(), authUserDetails.getAuthorities());
        outgoingAuth.setDetails(auth.getDetails());
        return outgoingAuth;
    }

    private boolean isValidLong(String code) {
        try {
            Long.parseLong(code);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}