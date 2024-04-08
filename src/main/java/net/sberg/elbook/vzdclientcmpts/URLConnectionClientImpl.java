package net.sberg.elbook.vzdclientcmpts;

import okhttp3.internal.tls.OkHostnameVerifier;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.apache.oltu.oauth2.client.response.OAuthClientResponseFactory;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URLConnectionClientImpl extends URLConnectionClient {

    private boolean verifySsl;
    private InputStream sslCaCert;

    public URLConnectionClientImpl(boolean verifySsl, InputStream sslCaCert) {
        this.verifySsl = verifySsl;
        this.sslCaCert = sslCaCert;
    }

    public <T extends OAuthClientResponse> T execute(OAuthClientRequest request, Map<String, String> headers,
                                                     String requestMethod, Class<T> responseClass)
            throws OAuthSystemException, OAuthProblemException {

        InputStream responseBody = null;
        URLConnection c;
        Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
        int responseCode;
        try {
            URL url = new URL(request.getLocationUri());

            c = url.openConnection();
            responseCode = -1;
            if (c instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) c;

                if (httpURLConnection instanceof HttpsURLConnection) {
                    applySslSettings((HttpsURLConnection)httpURLConnection);
                }

                if (headers != null && !headers.isEmpty()) {
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        httpURLConnection.addRequestProperty(header.getKey(), header.getValue());
                    }
                }

                if (request.getHeaders() != null) {
                    for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                        httpURLConnection.addRequestProperty(header.getKey(), header.getValue());
                    }
                }

                if (OAuthUtils.isEmpty(requestMethod)) {
                    httpURLConnection.setRequestMethod(OAuth.HttpMethod.GET);
                } else {
                    httpURLConnection.setRequestMethod(requestMethod);
                    setRequestBody(request, requestMethod, httpURLConnection);
                }

                httpURLConnection.connect();

                InputStream inputStream;
                responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST || responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    inputStream = httpURLConnection.getErrorStream();
                } else {
                    inputStream = httpURLConnection.getInputStream();
                }

                responseHeaders = httpURLConnection.getHeaderFields();
                responseBody = inputStream;
            }
        } catch (IOException e) {
            throw new OAuthSystemException(e);
        }

        return OAuthClientResponseFactory
                .createCustomResponse(responseBody, c.getContentType(), responseCode, responseHeaders, responseClass);
    }

    private void applySslSettings(HttpsURLConnection httpsURLConnection) {
        try {
            TrustManager[] trustManagers;
            HostnameVerifier hostnameVerifier;
            if (!verifySsl) {
                trustManagers = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };
                hostnameVerifier = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };
            }
            else {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                if (sslCaCert == null) {
                    trustManagerFactory.init((KeyStore) null);
                }
                else {
                    char[] password = null;
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(sslCaCert);
                    if (certificates.isEmpty()) {
                        throw new IllegalArgumentException("expected non-empty set of trusted certificates");
                    }
                    KeyStore caKeyStore = newEmptyKeyStore(password);
                    int index = 0;
                    for (Certificate certificate : certificates) {
                        String certificateAlias = "ca" + Integer.toString(index++);
                        caKeyStore.setCertificateEntry(certificateAlias, certificate);
                    }
                    trustManagerFactory.init(caKeyStore);
                }
                trustManagers = trustManagerFactory.getTrustManagers();
                hostnameVerifier = OkHostnameVerifier.INSTANCE;
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());

            httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(hostnameVerifier);

        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void setRequestBody(OAuthClientRequest request, String requestMethod, HttpURLConnection httpURLConnection)
            throws IOException {
        String requestBody = request.getBody();
        if (OAuthUtils.isEmpty(requestBody)) {
            return;
        }

        if (OAuth.HttpMethod.POST.equals(requestMethod) || OAuth.HttpMethod.PUT.equals(requestMethod)) {
            httpURLConnection.setDoOutput(true);
            OutputStream ost = httpURLConnection.getOutputStream();
            PrintWriter pw = new PrintWriter(ost);
            pw.print(requestBody);
            pw.flush();
            pw.close();
        }
    }
}
