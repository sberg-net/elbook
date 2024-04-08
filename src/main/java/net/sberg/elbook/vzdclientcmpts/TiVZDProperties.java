package net.sberg.elbook.vzdclientcmpts;

import de.gematik.vzd.model.V1_9_5.InfoObject;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TiVZDProperties {

    public static final String API_VERSION_V1_9_5 = "V1_9_5";
    public static final String API_VERSION_V1_12_6 = "V1_12_6";

    private static final String RU_RESOURCE_URI = "https://vzdpflege-ref.vzd.ti-dienste.de";
    private static final String PU_RESOURCE_URI = "https://vzdpflege.vzd.ti-dienste.de";
    private static final String TU_RESOURCE_URI = "https://vzdpflege-test.vzd.ti-dienste.de";

    private static Map<Integer, Integer> mutexMap = new HashMap<>();

    private String resourceUri;
    private String tokenUri;
    private String authId;
    private String authSecret;
    private String sslCrtFile;
    private boolean debugNetworkTraffic = false;
    private InfoObject infoObject;
    private String apiVersion;

    public static final synchronized Integer checkMutex(TiVZDProperties tiVZDProperties) {
        Integer hash =
            tiVZDProperties.authId.hashCode() * 3
            +
            tiVZDProperties.authSecret.hashCode() * 5
            +
            tiVZDProperties.tokenUri.hashCode() * 7
            +
            tiVZDProperties.resourceUri.hashCode() * 11
            +
            (tiVZDProperties.sslCrtFile == null?"".hashCode()*13:tiVZDProperties.sslCrtFile.hashCode()*13);
        if (!mutexMap.containsKey(hash)) {
            mutexMap.put(hash, hash);
        }
        return mutexMap.get(hash);
    }

    public EnumEnvironment determineEnvironment() {
        if (resourceUri.startsWith(PU_RESOURCE_URI)) {
            return EnumEnvironment.PU;
        }
        else if (resourceUri.startsWith(RU_RESOURCE_URI)) {
            return EnumEnvironment.RU;
        }
        else if (resourceUri.startsWith(TU_RESOURCE_URI)) {
            return EnumEnvironment.TU;
        }
        return EnumEnvironment.UNKNOWN;
    }
}
