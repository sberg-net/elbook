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
package net.sberg.elbook.tspcmpts;

import de.gematik.ws.sst.v1.AddHbaVorbefuellungenResponseType;
import de.gematik.ws.sst.v1.AddSmcbVorbefuellungenResponseType;
import de.gematik.ws.sst.v1.ReturnCode;

public class TspUtils {
    public static final String getVorgangsNr(AddHbaVorbefuellungenResponseType addHbaVorbefuellungenResponseType) throws IllegalStateException {
        if (!addHbaVorbefuellungenResponseType.getReturnCode().getCode().equals(ReturnCode.OK)) {
            throw new IllegalStateException("error on sending hba request. return code = "+addHbaVorbefuellungenResponseType.getReturnCode().getCode()+" description = "+addHbaVorbefuellungenResponseType.getReturnCode().getDescription());
        }
        return addHbaVorbefuellungenResponseType.getHbaVorbefuellungen().getHbaVorbefuellung().get(0).getVorgangsNr();
    }
    public static final String getVorgangsNr(AddSmcbVorbefuellungenResponseType addSmcbVorbefuellungenResponseType) throws IllegalStateException {
        if (!addSmcbVorbefuellungenResponseType.getReturnCode().getCode().equals(ReturnCode.OK)) {
            throw new IllegalStateException("error on sending smcb request. return code = "+addSmcbVorbefuellungenResponseType.getReturnCode().getCode()+" description = "+addSmcbVorbefuellungenResponseType.getReturnCode().getDescription());
        }
        return addSmcbVorbefuellungenResponseType.getSmcbVorbefuellungen().getSmcbVorbefuellung().get(0).getVorgangsNr();
    }
}
