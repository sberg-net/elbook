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
