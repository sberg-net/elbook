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

import net.sberg.elbook.vzdclientcmpts.command.EnumEntryType;

public enum EnumSektor {

    APOTHEKE(EnumEntryType.Leistungserbringerinstitution),
    APOTHEKER(EnumEntryType.Berufsgruppe),
    ARZT(EnumEntryType.Berufsgruppe),
    ARZTPRAXIS(EnumEntryType.Leistungserbringerinstitution),
    ZAHNARZT(EnumEntryType.Berufsgruppe),
    ZAHNARZTPRAXIS(EnumEntryType.Leistungserbringerinstitution),
    PSYCHOTHERAPEUTH(EnumEntryType.Berufsgruppe),
    KRANKENHAUS(EnumEntryType.Leistungserbringerinstitution),
    GKV(EnumEntryType.Krankenkasse),
    GKV_EPA(EnumEntryType.Krankenkasse_ePA),
    HBAGEMATIK(EnumEntryType.Berufsgruppe),
    SMCBGEMATIK(EnumEntryType.Organisation),
    HBAEGBR(EnumEntryType.Berufsgruppe),
    SMCBEGBR(EnumEntryType.Leistungserbringerinstitution);

    private EnumEntryType entryType;

    private EnumSektor(EnumEntryType entryType) {
        this.entryType = entryType;
    }

    public EnumEntryType getEntryType() {
        return entryType;
    }
}
