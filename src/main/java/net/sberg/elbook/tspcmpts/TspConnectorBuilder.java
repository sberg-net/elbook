package net.sberg.elbook.tspcmpts;

import de.datec.hba.properties.HbaProperties;
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
    private HbaProperties hbaProperties;
    private HbaModelMapper hbaModelMapper;

    public TspConnectorBuilder() {
        marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("de.gematik.ws.cm.pers.hba_smc_b.v1", "de.gematik.ws.sst.v1", "org.w3._2000._09.xmldsig_");
        hbaModelMapper = new HbaModelMapper();
    }

    public TspConnectorBuilder(HbaProperties hbaProperties) {
        this();
        this.hbaProperties = hbaProperties;
    }

    private WebServiceMessageSender createWebServiceMessageSender(int connectionTimeout, int readTimeout) {
        HttpComponentsMessageSender httpComponentsMessageSender = new HttpComponentsMessageSender();
        httpComponentsMessageSender.setConnectionTimeout(connectionTimeout);
        httpComponentsMessageSender.setReadTimeout(readTimeout);
        return httpComponentsMessageSender;
    }

    public List<QvdaFrontendProperties> getFrontendQvdaProperties() {
        return getFrontendQvdaProperties(hbaProperties);
    }

    public List<QvdaFrontendProperties> getFrontendQvdaProperties(HbaProperties hbaProperties) {
        return hbaProperties.getQvdas().stream().map(item -> new QvdaFrontendProperties(
                item.getName(),
                item.getFrontendString(),
                item.getAntragsPortal(),
                item.getHba(),
                item.getSmcb())).collect(Collectors.toList());
    }

    public TspConnector build(QVDA qvda, EnumAntragTyp typ) throws Exception {
        return build(hbaProperties, qvda, typ);
    }

    public TspConnector build(HbaProperties hbaProperties, QVDA qvda, EnumAntragTyp typ) throws Exception {
        QvdaProperties qvdaProperties = hbaProperties.getQvdaProperties(qvda);
        return hbaConnector(hbaProperties, qvdaProperties, typ);
    }

    private TspConnector hbaConnector(HbaProperties hbaProperties, QvdaProperties qvdaProperties, EnumAntragTyp typ) throws Exception {
        hbaModelMapper.setHbaProperties(hbaProperties);

        TspConnector connector = new TspConnector(hbaModelMapper);
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
