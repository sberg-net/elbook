package net.sberg.elbook.verzeichnisdienstcmpts.fhir.client;

import de.gematik.vzd.holderauth.model.V1_0_1.HolderAccessToken;
import lombok.Data;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.EnumEnvironment;

import java.time.LocalDateTime;

@Data
public class TiFhirProperties {

    private static final String RU_TOKEN_URI = "https://vzd-fhir-directory-ref.vzd.ti-dienste.de";
    private static final String PU_TOKEN_URI = "https://fhir-directory.vzd.ti-dienste.de";
    private static final String TU_TOKEN_URI = "https://fhir-directory-tu.vzd.ti-dienste.de";

    private static final String RU_RESOURCE_URI = "https://fhir-directory-ref.vzd.ti-dienste.de/search";
    private static final String PU_RESOURCE_URI = "https://fhir-directory.vzd.ti-dienste.de/search";
    private static final String TU_RESOURCE_URI = "https://fhir-directory-tu.vzd.ti-dienste.de/search";

    private TiFhirProperties() {}

    public TiFhirProperties(EnumEnvironment enumEnvironment) throws Exception {
        switch (enumEnvironment) {
            case PU -> {
                resourceUri = PU_RESOURCE_URI;
                tokenUri = PU_TOKEN_URI;
            }
            case RU -> {
                resourceUri = RU_RESOURCE_URI;
                tokenUri = RU_TOKEN_URI;
            }
            case TU -> {
                resourceUri = TU_RESOURCE_URI;
                tokenUri = TU_TOKEN_URI;
            }
            case UNKNOWN -> throw new IllegalStateException("unknown environment");
        }
    }

    private String resourceUri;
    private String tokenUri;
    private HolderAccessToken holderAccessToken;
    private LocalDateTime tokenvalidationDate;
    private boolean fhirConnected;
    private Exception fhirConnectException;

    public void setTokenValidation() {
        int secureSeconds = (int) (holderAccessToken.getExpiresIn() * 0.90);
        tokenvalidationDate = LocalDateTime.now().plusSeconds(secureSeconds);
        fhirConnected = true;
    }

    /**
     * Checks if the token is still valid. If not request a new one
     *
     * @return
     */
    public boolean validateToken() {
        return LocalDateTime.now().isBefore(tokenvalidationDate);
    }
}
