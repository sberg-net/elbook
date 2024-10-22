package net.sberg.elbook.tspcmpts;

import de.datec.hba.bean.Antrag;
import de.gematik.ws.cm.pers.hba_smc_b.v1.*;
import de.gematik.ws.sst.v1.ObjectFactory;
import de.gematik.ws.sst.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import javax.xml.bind.JAXBElement;
import java.util.List;

public class TspConnector extends WebServiceGatewaySupport {

    private HbaModelMapper hbaModelMapper;

    private Logger log = LoggerFactory.getLogger(TspConnector.class);

    private ObjectFactory oFactory = new ObjectFactory();

    public TspConnector(HbaModelMapper hbaModelMapper) {
        this.hbaModelMapper = hbaModelMapper;
    }

    public String sendAntrag(Antrag antrag) throws Exception {
        if (antrag.getTyp().equals(EnumAntragTyp.HBA)) {
            return HbaUtils.getVorgangsNr(addHbaVorbefuellungen(hbaModelMapper.hbaVorbefuellungenFromAntrag(antrag)));
        }
        else if (antrag.getTyp().equals(EnumAntragTyp.SMCB)) {
            return HbaUtils.getVorgangsNr(addSmcbVorbefuellungen(hbaModelMapper.smcbVorbefuellungenFromAntrag(antrag)));
        }
        else {
            throw new IllegalStateException("Unbekannter Antragstyp " + antrag.getTyp() + "(antrag-id: " + antrag.getId());
        }
    }

    public GetSmcbAntraegeExportResponseType getSmcbAntraegeExportByStatusKey(AntragStatusKey statusKey, boolean overview)  {
        AntraegeExportRequestType antraegeExportRequest = new AntraegeExportRequestType();
        antraegeExportRequest.setAntragsStatus(statusKey);
        antraegeExportRequest.setUeberblicksanfrage(overview);
        return (GetSmcbAntraegeExportResponseType)getSoapResponse(oFactory.createGetSmcbAntraegeExportRequest(antraegeExportRequest));
    }

    public GetSmcbAntraegeExportResponseType getSmcbAntraegeExportByKartenStatusKey(KartenStatusKey statusKey, boolean overview)  {
        AntraegeExportRequestType antraegeExportRequest = new AntraegeExportRequestType();
        antraegeExportRequest.setKarteStatus(statusKey);
        antraegeExportRequest.setUeberblicksanfrage(overview);
        return (GetSmcbAntraegeExportResponseType)getSoapResponse(oFactory.createGetSmcbAntraegeExportRequest(antraegeExportRequest));
    }

    public GetSmcbAntraegeExportResponseType getSmcbAntraegeExportByVorgangsnummer(String vorgangsnummer) {
        AntraegeExportRequestType antraegeExportRequest = new AntraegeExportRequestType();
        antraegeExportRequest.setVorgangsNr(vorgangsnummer);
        antraegeExportRequest.setUeberblicksanfrage(false);
        return (GetSmcbAntraegeExportResponseType)getSoapResponse(oFactory.createGetSmcbAntraegeExportRequest(antraegeExportRequest));
    }

    public GetHbaAntraegeExportResponseType getHbaAntraegeExportByKartenStatusKey(KartenStatusKey statusKey, boolean overview) {
        AntraegeExportRequestType antraegeExportRequest = new AntraegeExportRequestType();
        antraegeExportRequest.setKarteStatus(statusKey);
        antraegeExportRequest.setUeberblicksanfrage(overview);
        return (GetHbaAntraegeExportResponseType)getSoapResponse(oFactory.createGetHbaAntraegeExportRequest(antraegeExportRequest));
    }

    public GetHbaAntraegeExportResponseType getHbaAntraegeExportByStatusKey(AntragStatusKey statusKey, boolean overview) {
        AntraegeExportRequestType antraegeExportRequest = new AntraegeExportRequestType();
        antraegeExportRequest.setAntragsStatus(statusKey);
        antraegeExportRequest.setUeberblicksanfrage(overview);
        return (GetHbaAntraegeExportResponseType)getSoapResponse(oFactory.createGetHbaAntraegeExportRequest(antraegeExportRequest));
    }

    public GetHbaAntraegeExportResponseType getHbaAntraegeExportByVorgangsnummer(String vorgangsnummer) {
        AntraegeExportRequestType antraegeExportRequest = new AntraegeExportRequestType();
        antraegeExportRequest.setVorgangsNr(vorgangsnummer);
        antraegeExportRequest.setUeberblicksanfrage(false);
        return (GetHbaAntraegeExportResponseType)getSoapResponse(oFactory.createGetHbaAntraegeExportRequest(antraegeExportRequest));
    }

    public AddHbaVorbefuellungenResponseType addHbaVorbefuellungen(List<HbaVorbefuellung> hbaVorbefuellungCollection) {
        HbaVorbefuellungen hbaVorbefuellungen = new HbaVorbefuellungen(hbaVorbefuellungCollection);
        return (AddHbaVorbefuellungenResponseType)getSoapResponse(hbaVorbefuellungen);
    }

    public AddSmcbVorbefuellungenResponseType addSmcbVorbefuellungen(List<SmcbVorbefuellung> smcbVorbefuellungCollection)  {
        SmcbVorbefuellungen smcbVorbefuellungen = new SmcbVorbefuellungen(smcbVorbefuellungCollection);
        return (AddSmcbVorbefuellungenResponseType)getSoapResponse(smcbVorbefuellungen);
    }

    private GeneralResponseType getSoapResponse(Object requestPayload) {
        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(requestPayload);
        GeneralResponseType generalResponseType = (GeneralResponseType)response.getValue();
        log.debug("Code: " + generalResponseType.getReturnCode() + " Number: " + generalResponseType.getReturnCode().getNumber() + " Description: " + generalResponseType.getReturnCode().getDescription());
        return generalResponseType;
    }
}
