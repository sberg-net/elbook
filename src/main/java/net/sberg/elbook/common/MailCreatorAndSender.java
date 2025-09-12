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
package net.sberg.elbook.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Service
public class MailCreatorAndSender {

    private static final Logger log = LoggerFactory.getLogger(MailCreatorAndSender.class);

    @Value("${mailCreatorAndSenderService.testMode}")
    private boolean testMode;
    @Value("${mailCreatorAndSenderService.testRecipients}")
    private String testRecipients;

    @Autowired
    private JavaMailSender javaMailSender;

    public boolean isTestMode() {
        return testMode;
    }

    public String getTestRecipients() {
        return testRecipients;
    }

    public void send() throws Exception {
        if (javaMailSender instanceof JavaMailSenderImpl && ((JavaMailSenderImpl)javaMailSender).getHost().equals("notSet")) {
            return;
        }
        File f = new File(ICommonConstants.BASE_DIR + "mails");
        if (!f.exists()) {
            return;
        }
        File[] mails = f.listFiles();
        MimeMessage msg;
        FileInputStream fileInputStream;
        Session sess = Session.getDefaultInstance(new Properties());
        for (int i = 0; i < mails.length; i++) {
            try {
                fileInputStream = new FileInputStream(mails[i].getAbsolutePath());
                msg = new MimeMessage(sess, fileInputStream);
                javaMailSender.send(msg);
                fileInputStream.close();
                mails[i].delete();
            }
            catch (Exception e) {
                log.error("error on sending mail: "+mails[i].getAbsolutePath(), e);
                continue;
            }
        }
    }

    public void write(SimpleMailMessage mailMessage, String prefix) throws Exception {
        MimeMailMessage message = new MimeMailMessage(new MimeMessage(Session.getDefaultInstance(new Properties())));
        mailMessage.copyTo(message);
        writeToFileDirectory(message.getMimeMessage(), prefix, ICommonConstants.BASE_DIR + "mails");
    }

    private static final void writeToFileDirectory(MimeMessage msg, String prefix, String storageFolder) throws Exception {
        File f = new File(storageFolder);
        if (!f.exists()) {
            f.mkdirs();
        }
        String whereToSave = f.getAbsolutePath() + File.separator
                + prefix + System.nanoTime() + ".eml";
        f = new File(whereToSave);
        if (!f.exists()) {
            OutputStream out = new FileOutputStream(new File(whereToSave));
            msg.writeTo(out);
            out.flush();
            out.close();
        }
    }

    public void send(String from, String[] to, String subject, String body, File attachment) throws Exception {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, attachment!=null?MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED:MimeMessageHelper.MULTIPART_MODE_NO, StandardCharsets.UTF_8.name());
        helper.setTo(to);
        helper.setFrom(from);
        helper.setSubject(subject);
        helper.setText(body);
        if (attachment != null) {
            helper.addAttachment(attachment.getName(), attachment);
        }
        javaMailSender.send(message);
    }
}
