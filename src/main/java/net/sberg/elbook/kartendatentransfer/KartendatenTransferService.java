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
package net.sberg.elbook.kartendatentransfer;

import lombok.RequiredArgsConstructor;
import net.sberg.elbook.common.ICommonConstants;
import net.sberg.elbook.common.MailCreatorAndSender;
import net.sberg.elbook.jdbc.JdbcGenericDao;
import net.sberg.elbook.mandantcmpts.Mandant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KartendatenTransferService {

    @Value("${kartendatenTransferService.gueltigBisTage}")
    private int gueltigBisTage;
    @Value("${kartendatenTransferService.url}")
    private String url;

    private final MailCreatorAndSender mailCreatorAndSender;
    private final JdbcGenericDao genericDao;

    public String createAktivierungsCode() {
        return UUID.randomUUID().toString();
    }

    public int createHashcode(KartendatenTransfer kartendatenTransfer) {
        return kartendatenTransfer.getId() * 3 + kartendatenTransfer.getAktivierungsCode().hashCode() * 5;
    }

    public LocalDateTime createGueltigBis(KartendatenTransfer kartendatenTransfer) {
        return kartendatenTransfer.getErstelltAm().plusDays(gueltigBisTage);
    }

    public String getUrl(KartendatenTransfer kartendatenTransfer) {
        return url+kartendatenTransfer.getHashCode();
    }

    public String getDownloadUrl(KartendatenTransfer kartendatenTransfer) {
        return url+"herunterladen/"+kartendatenTransfer.getHashCode()+"/"+kartendatenTransfer.getAktivierungsCode();
    }

    public void delete(KartendatenTransfer kartendatenTransfer) throws Exception {
        File f = new File(ICommonConstants.BASE_DIR + "datentransfer" + File.separator + "hba_"+kartendatenTransfer.getMandantId()+"_"+kartendatenTransfer.getId()+".json");
        f.delete();
        genericDao.delete(kartendatenTransfer, Optional.empty());
    }

    public void createHBADatenTransferMail(HbaKartendatenTransferCommandContainer hbaKartendatenTransferCommandContainer, KartendatenTransfer kartendatenTransfer, Mandant mandant, boolean withActivationCode) throws Exception {
        SimpleMailMessage msg = new SimpleMailMessage();
        if (mailCreatorAndSender.isTestMode()) {
            msg.setTo(mailCreatorAndSender.getTestRecipients().split(","));
        }
        else {
            msg.setTo(kartendatenTransfer.getEmpfaenger());
            msg.setCc(kartendatenTransfer.getAbsender());
        }
        msg.setFrom("software@sberg.net");
        msg.setSubject("Elbook: HBA-Daten vom Kartenherausgeber "+hbaKartendatenTransferCommandContainer.getAbsenderKartenherausgeber().getHrText()+" vorhanden");

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("Guten Tag,\n");
        bodyBuilder.append("Es liegen neue HBA-Daten vor und sind unter dem Link "+getUrl(kartendatenTransfer)+" abrufbar.\n");
        if (withActivationCode) {
            bodyBuilder.append("Der Aktivierungscode lautet: " + kartendatenTransfer.getAktivierungsCode() + "\n");
        }
        else {
            bodyBuilder.append("Den Aktivierungscode bekommen Sie von Mitarbeiter:innen des absendenden Kartenherausgebers\n");
        }
        bodyBuilder.append("Bitte beachten Sie, dass die Daten bis zum "+kartendatenTransfer.getGueltigBis().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))+" abrufbar sind.\n");
        bodyBuilder.append("\nViele Grüße\nIhr\nelBook-Team\n");

        msg.setText(bodyBuilder.toString());
        mailCreatorAndSender.write(msg, "datentransfer_"+kartendatenTransfer.getId()+"_"+mandant.getId()+"_"+System.nanoTime());
    }

}
