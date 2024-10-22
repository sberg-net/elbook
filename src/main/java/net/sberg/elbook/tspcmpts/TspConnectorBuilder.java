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

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

public class TspConnectorBuilder {

    private Jaxb2Marshaller marshaller;

    public TspConnectorBuilder() {
        marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("de.gematik.ws.cm.pers.hba_smc_b.v1", "de.gematik.ws.sst.v1", "org.w3._2000._09.xmldsig_");
    }

    public TspConnector build(TspProperties tspProperties, QVDA qvda, EnumAntragTyp typ) throws Exception {
        return hbaConnector(tspProperties.getQvdaProperties(qvda), typ);
    }

    private TspConnector hbaConnector(QvdaProperties qvdaProperties, EnumAntragTyp typ) throws Exception {
        TspConnector connector = new TspConnector();
        connector.setMessageSender(messageSender(qvdaProperties.getConnection().get(typ)));
        connector.setDefaultUri(qvdaProperties.getConnection().get(typ).getDefaultUri());
        connector.setMarshaller(marshaller);
        connector.setUnmarshaller(marshaller);
        return connector;
    }

    private WebServiceMessageSender messageSender(ConnectionProperties connectionProperties) throws Exception {

        HttpComponentsMessageSender messageSender = new HttpComponentsMessageSender();

        if (connectionProperties.getTimeout() > 0) {
            RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectionProperties.getTimeout())
                .setConnectionRequestTimeout(connectionProperties.getTimeout())
                .setSocketTimeout(connectionProperties.getTimeout()).build();
            HttpClient httpCLient = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor())
                .setSSLContext(sslContext(connectionProperties))
                .build();
            messageSender.setHttpClient(httpCLient);
        }
        else {
            HttpClient httpCLient = HttpClientBuilder.create()
                .addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor())
                .setSSLContext(sslContext(connectionProperties))
                .build();
            messageSender.setHttpClient(httpCLient);
        }

        return messageSender;
    }

    private SSLContext sslContext(ConnectionProperties connectionProperties) throws Exception {
        char[] passCharArray = connectionProperties.getKeystorePass().toCharArray();
        KeyStore keyStore = KeyStore.getInstance(connectionProperties.getKeystoreType());
        keyStore.load(connectionProperties.isKeystoreFileInClasspath()?new ClassPathResource(connectionProperties.getKeystoreFile()).getInputStream():new FileInputStream(connectionProperties.getKeystoreFile()), passCharArray);
        return SSLContexts.custom().loadKeyMaterial(keyStore, passCharArray).loadTrustMaterial(new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        }).build();
    }

}
