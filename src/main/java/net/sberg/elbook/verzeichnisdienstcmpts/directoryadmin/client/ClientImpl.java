package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.gematik.vzd.api.ApiClient;
import de.gematik.vzd.api.auth.HttpBasicAuth;
import de.gematik.vzd.api.auth.OAuth;
import de.gematik.vzd.holderauth.api.V1_0_1.DefaultApi;
import net.sberg.elbook.verzeichnisdienstcmpts.fhir.client.TiFhirProperties;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class ClientImpl extends ApiClient {

    private static final Logger log = LoggerFactory.getLogger(ClientImpl.class);

    private TiVZDProperties tiVZDProperties;
    private TiFhirProperties tiFhirProperties;
    private LocalDateTime tokenvalidationDate;
    private String id;
    private int usedCount;
    private LocalDateTime lastUsed;

    private ClientImpl() {}

    public ClientImpl(final TiVZDProperties tiVZDProperties, String id) throws Exception {
        super();
        this.tiVZDProperties = tiVZDProperties;
        this.id = id;
        setBasePath(tiVZDProperties.getResourceUri());
        createAndSetNewOAuth2Token();
    }

    public String getHolder() {
        return tiVZDProperties.getAuthId();
    }

    public String getId() {
        return id;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public TiVZDProperties getTiVZDProperties() {
        return tiVZDProperties;
    }

    public LocalDateTime getLastUsed() {
        return lastUsed;
    }

    public void use() {
        usedCount++;
        lastUsed = LocalDateTime.now();
    }

    public ClientDetails createDetails(Integer mutex, boolean inUse) {
        ClientDetails details = new ClientDetails();
        details.setId(id);
        details.setMutex(mutex);
        details.setLastUsed(lastUsed);
        details.setTokenvalidationDate(tokenvalidationDate);
        details.setUsedCount(usedCount);
        details.setInUse(inUse);
        details.setFhirConnected(tiFhirProperties.isFhirConnected());
        details.setFhirTokenvalidationDate(tiFhirProperties.getTokenvalidationDate());
        return details;
    }

    private void createAndSetNewOAuth2Token() throws Exception {
        log.debug(id+" -> trying to get new access token");

        HttpBasicAuth basicAuth = new HttpBasicAuth();
        basicAuth.setPassword(tiVZDProperties.getAuthSecret());
        basicAuth.setUsername(tiVZDProperties.getAuthId());

        OAuthClientRequest request = OAuthClientRequest
            .tokenLocation(tiVZDProperties.getTokenUri())
            .setClientId(basicAuth.getUsername())
            .setClientSecret(basicAuth.getPassword())
            .setGrantType(GrantType.CLIENT_CREDENTIALS)
            .buildBodyMessage();

        request.setHeader("Accept", "application/json");

        OAuthClient oAuthClient = null;
        if (tiVZDProperties.getSslCrtFile() != null) {
            oAuthClient = new OAuthClient(new URLConnectionClientImpl(true, getClass().getResourceAsStream(tiVZDProperties.getSslCrtFile())));
        }
        else {
            oAuthClient = new OAuthClient(new URLConnectionClient());
        }

        OAuthAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request, OAuthJSONAccessTokenResponse.class);

        JsonObject jObj = new JsonParser().parse(oAuthResponse.getBody()).getAsJsonObject();

        OAuth oAuth = (OAuth) getAuthentications().get("OAuth2");
        oAuth.setAccessToken(jObj.get("access_token").toString().replaceAll("\"", ""));
        setTokenValidation(jObj.get("expires_in").toString());
        log.debug(id+" -> requesting new oauth2 token successful");
        createFhirProperties(oAuth);
    }

    private void createFhirProperties(OAuth oAuth) throws Exception {
        tiFhirProperties = new TiFhirProperties(tiVZDProperties.determineEnvironment());

        try {
            de.gematik.vzd.holderauth.api.ApiClient defaultClient = new de.gematik.vzd.holderauth.api.ApiClient();
            defaultClient.setBasePath(tiFhirProperties.getTokenUri());
            defaultClient.setAccessToken(oAuth.getAccessToken());

            DefaultApi apiInstance = new DefaultApi(defaultClient);
            tiFhirProperties.setHolderAccessToken(apiInstance.getHolderAuthenticate());
            tiFhirProperties.setTokenValidation();
        }
        catch (Exception e) {
            tiFhirProperties.setFhirConnectException(e);
            log.debug("error on fhir connect", e);
        }
    }

    /**
     * Sets the time - 10% until the token expires. This ensures that the token is every time valid
     *
     * @param expires_in is an String with numbers. For example 3600 equals 1 hour.
     */
    private void setTokenValidation(String expires_in) {
        int seconds = Integer.parseInt(expires_in);
        int secureSeconds = (int) (seconds * 0.90);
        tokenvalidationDate = LocalDateTime.now().plusSeconds(secureSeconds);
        log.debug(id+" - tokenvalidationdate - "+tokenvalidationDate);
    }

    /**
     * Checks if the token is still valid. If not request a new one
     *
     * @return
     */
    public boolean validateToken() {
        if (LocalDateTime.now().isBefore(tokenvalidationDate)) {
            if (tiFhirProperties.isFhirConnected() && tiFhirProperties.validateToken()) {
                return true;
            }
            else if (!tiFhirProperties.isFhirConnected()) {
                return true;
            }
        }
        try {
            createAndSetNewOAuth2Token();
        } catch (Exception e) {
            throw new IllegalStateException("Requesting a new OAuth2 token failed.", e);
        }
        return LocalDateTime.now().isBefore(tokenvalidationDate);
    }
}
