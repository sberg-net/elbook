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

import de.gematik.ws.cm.pers.hba_smc_b.v1.*;
import de.gematik.ws.sst.v1.ObjectFactory;
import de.gematik.ws.sst.v1.*;
import jakarta.xml.bind.JAXBElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class TspConnector extends WebServiceGatewaySupport {

    private Logger log = LoggerFactory.getLogger(TspConnector.class);
    private ObjectFactory oFactory = new ObjectFactory();

    public GetHbaAntraegeExportResponseType getHbaAntrag(String vorgangsNr) {
        AntraegeExportRequestType antraegeExportRequest = new AntraegeExportRequestType();
        antraegeExportRequest.setVorgangsNr(vorgangsNr);
        return (GetHbaAntraegeExportResponseType)getSoapResponse(oFactory.createGetHbaAntraegeExportRequest(antraegeExportRequest));
    }

    public GetSmcbAntraegeExportResponseType getSmcbAntrag(String vorgangsNr) {
        AntraegeExportRequestType antraegeExportRequest = new AntraegeExportRequestType();
        antraegeExportRequest.setVorgangsNr(vorgangsNr);
        return (GetSmcbAntraegeExportResponseType)getSoapResponse(oFactory.createGetSmcbAntraegeExportRequest(antraegeExportRequest));
    }

    public GetHbaAntraegeExportResponseType getHbaAntraegeZertifikateFreigeschaltet(boolean overview, int limit, int offset) {
        AntraegeExportRequestType antraegeExportRequest = new AntraegeExportRequestType();
        antraegeExportRequest.setUeberblicksanfrage(overview);
        antraegeExportRequest.setKarteStatus(KartenStatusKey.ZERTIFIKATE_FREIGESCHALTET);
        antraegeExportRequest.setAnfrageLimit(limit);
        antraegeExportRequest.setAnfrageOffset(offset);
        return (GetHbaAntraegeExportResponseType)getSoapResponse(oFactory.createGetHbaAntraegeExportRequest(antraegeExportRequest));
    }

    public GetSmcbAntraegeExportResponseType getSmcbAntraegeZertifikateFreigeschaltet(boolean overview, int limit, int offset) {
        AntraegeExportRequestType antraegeExportRequest = new AntraegeExportRequestType();
        antraegeExportRequest.setUeberblicksanfrage(overview);
        antraegeExportRequest.setAnfrageLimit(limit);
        antraegeExportRequest.setKarteStatus(KartenStatusKey.ZERTIFIKATE_FREIGESCHALTET);
        antraegeExportRequest.setAnfrageOffset(offset);
        return (GetSmcbAntraegeExportResponseType)getSoapResponse(oFactory.createGetSmcbAntraegeExportRequest(antraegeExportRequest));
    }

    private GeneralResponseType getSoapResponse(Object requestPayload) {
        JAXBElement response = (JAXBElement) getWebServiceTemplate().marshalSendAndReceive(requestPayload);
        GeneralResponseType generalResponseType = (GeneralResponseType)response.getValue();
        log.debug("Code: " + generalResponseType.getReturnCode() + " Number: " + generalResponseType.getReturnCode().getNumber() + " Description: " + generalResponseType.getReturnCode().getDescription());
        return generalResponseType;
    }
}
