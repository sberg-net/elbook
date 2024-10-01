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
package net.sberg.elbook.config;

import lombok.RequiredArgsConstructor;
import net.sberg.elbook.authcomponents.BasicAnd2faAuthenticationProvider;
import net.sberg.elbook.authcomponents.CustomWebAuthenticationDetailsSource;
import net.sberg.elbook.authcomponents.EnumAuthRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final BasicAnd2faAuthenticationProvider basicAnd2faAuthenticationProvider;
    private final CustomWebAuthenticationDetailsSource authenticationDetailsSource;

    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(sessionMgmt -> sessionMgmt
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/login")
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                antMatcher("/api/**"),
                                antMatcher("/mandant/is2FAenabled"),
                                antMatcher("/login")
                        )
                )
                .authorizeHttpRequests((resourceRequest) -> resourceRequest
                        .requestMatchers(
                                antMatcher("/webjars/**"),
                                antMatcher("/js/**"),
                                antMatcher("/css/**"),
                                antMatcher("/img/**"),
                                antMatcher("/fonts/**")
                        ).permitAll()
                )
                .authorizeHttpRequests((appRequest) -> appRequest
                        .requestMatchers(
                                antMatcher("/mandant/is2FAenabled"),
                                antMatcher("/hbakartendaten/**"),
                                antMatcher("/login"),
                                antMatcher("/info"),
                                antMatcher("/versionshistorie"),
                                antMatcher("/error/**")
                        ).permitAll()

                        .requestMatchers(
                                antMatcher("/"),
                                antMatcher("/passwort/**"),
                                antMatcher("/swagger-ui/**"),
                                antMatcher("/swagger/**"),
                                antMatcher("/api-docs/**")
                        ).authenticated()

                        .requestMatchers(
                                antMatcher("/thread/**"),
                                antMatcher("/clientconnection/**")
                        ).hasRole(EnumAuthRole.ROLE_ADMIN.getSuffix())

                        .requestMatchers(
                                antMatcher("/api/**")
                        ).hasAnyRole(EnumAuthRole.ROLE_ADMIN.getSuffix(), EnumAuthRole.ROLE_GOLD_LICENCE_HEAD_OF_DEPT.getSuffix())

                        .requestMatchers(
                                antMatcher("/mandant/einstellungen/**"),
                                antMatcher("/mandant/enable2FA/**"),
                                antMatcher("/mandant/enableFinish2FA/**"),
                                antMatcher("/mandant/disableFinish2FA/**"),
                                antMatcher("/verzeichnisdienst/**"),
                                antMatcher("/logeintrag/**"),
                                antMatcher("/report/**")
                        ).hasAnyRole(EnumAuthRole.ROLE_ADMIN.getSuffix(),
                                EnumAuthRole.ROLE_GOLD_LICENCE_HEAD_OF_DEPT.getSuffix(),
                                EnumAuthRole.ROLE_SILVER_LICENCE_HEAD_OF_DEPT.getSuffix(),
                                EnumAuthRole.ROLE_GOLD_LICENCE_USER.getSuffix(),
                                EnumAuthRole.ROLE_SILVER_LICENCE_USER.getSuffix())

                        .requestMatchers(
                                antMatcher("/mandant/**")
                        ).hasAnyRole(EnumAuthRole.ROLE_ADMIN.getSuffix(),
                                EnumAuthRole.ROLE_GOLD_LICENCE_HEAD_OF_DEPT.getSuffix(),
                                EnumAuthRole.ROLE_SILVER_LICENCE_HEAD_OF_DEPT.getSuffix())

                        .requestMatchers(
                                antMatcher("/stammdatencertimport/**"),
                                antMatcher("/api/**")
                        ).hasAnyRole(EnumAuthRole.ROLE_ADMIN.getSuffix(),
                                EnumAuthRole.ROLE_GOLD_LICENCE_HEAD_OF_DEPT.getSuffix())

                        .requestMatchers(
                                antMatcher("/hbakartendatentransfer/**")
                        ).hasAnyRole(EnumAuthRole.ROLE_PHARMACIST.getSuffix())
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin((form) -> form
                        .authenticationDetailsSource(authenticationDetailsSource)
                        .loginPage("/login")
                        .permitAll()
                )
                .logout((logout) -> logout
                        .permitAll()
                        .logoutSuccessUrl("/login"));
        return http.build();
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(basicAnd2faAuthenticationProvider);
    }

}
