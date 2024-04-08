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

@Getter
public enum EnumAuthRole {
    ROLE_ADMIN("ADMIN"),
    ROLE_GOLD_LICENCE_HEAD_OF_DEPT("GOLD_LICENCE_HEAD_OF_DEPT"),
    ROLE_SILVER_LICENCE_HEAD_OF_DEPT("SILVER_LICENCE_HEAD_OF_DEPT"),
    ROLE_GOLD_LICENCE_USER("GOLD_LICENCE_USER"),
    ROLE_SILVER_LICENCE_USER("SILVER_LICENCE_USER"),
    ROLE_DOCTOR("DOCTOR"),
    ROLE_PHARMACIST("PHARMACIST");

    private final String suffix;
    private EnumAuthRole(String suffix) {
        this.suffix = suffix;
    }
}
