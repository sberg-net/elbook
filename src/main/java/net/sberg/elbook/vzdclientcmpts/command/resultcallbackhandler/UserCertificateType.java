package net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserCertificateType {
    private DistinguishedNameType dn;
    private String entryType;
    private String telematikID;
    private String professionOID;
    private List<String> usage = new ArrayList<>();
    private String userCertificate;
    private String description;
    private Boolean active;
    private String serialNumber;
    private String issuer;
    private String publicKeyAlgorithm;
}
