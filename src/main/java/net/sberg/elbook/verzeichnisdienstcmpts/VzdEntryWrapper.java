package net.sberg.elbook.verzeichnisdienstcmpts;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.vzd.model.V1_12_6.FAD1;
import de.gematik.vzd.model.V1_12_6.FAD1KimDataInner;
import de.gematik.vzd.model.V1_12_6.FAD1KomLeDataInner;
import de.gematik.vzd.model.V1_12_6.Fachdaten;
import de.gematik.vzd.model.V1_9_5.*;
import lombok.Data;
import net.sberg.elbook.common.StringUtils;
import net.sberg.elbook.vzdclientcmpts.TiVZDProperties;
import net.sberg.elbook.vzdclientcmpts.command.EnumTriValue;

import java.io.File;
import java.util.Iterator;
import java.util.List;

@Data
public class VzdEntryWrapper {

    private DirectoryEntry directoryV1_9_5Entry;
    private de.gematik.vzd.model.V1_12_6.DirectoryEntry directoryV1_12_6Entry;

    private LogEntry logV1_9_5Entry;
    private de.gematik.vzd.model.V1_12_6.LogEntry logV1_12_6Entry;

    private UserCertificate userV1_9_5Certificate;
    private de.gematik.vzd.model.V1_12_6.UserCertificate userV1_12_6Certificate;

    private DistinguishedName distinguishedNameV1_9_5;
    private de.gematik.vzd.model.V1_12_6.DistinguishedName distinguishedNameV1_12_6;

    private TiVZDProperties tiVZDProperties;

    private VzdEntryWrapper() {}
    public VzdEntryWrapper(TiVZDProperties tiVZDProperties) {
        this.tiVZDProperties = tiVZDProperties;
        if (tiVZDProperties.getApiVersion().equals(TiVZDProperties.API_VERSION_V1_9_5)) {

            directoryV1_9_5Entry = new DirectoryEntry();
            BaseDirectoryEntry baseDirectoryEntry = new BaseDirectoryEntry();
            DistinguishedName dn = new DistinguishedName();
            baseDirectoryEntry.setDn(dn);
            directoryV1_9_5Entry.setDirectoryEntryBase(baseDirectoryEntry);

            logV1_9_5Entry = new LogEntry();

            userV1_9_5Certificate = new UserCertificate();
            userV1_9_5Certificate.setDn(new DistinguishedName());

            distinguishedNameV1_9_5 = new DistinguishedName();
        }
        else if (tiVZDProperties.getApiVersion().equals(TiVZDProperties.API_VERSION_V1_12_6)) {

            directoryV1_12_6Entry = new de.gematik.vzd.model.V1_12_6.DirectoryEntry();
            de.gematik.vzd.model.V1_12_6.BaseDirectoryEntry baseDirectoryEntry = new de.gematik.vzd.model.V1_12_6.BaseDirectoryEntry();
            de.gematik.vzd.model.V1_12_6.DistinguishedName dn = new de.gematik.vzd.model.V1_12_6.DistinguishedName();
            baseDirectoryEntry.setDn(dn);
            directoryV1_12_6Entry.setDirectoryEntryBase(baseDirectoryEntry);

            logV1_12_6Entry = new de.gematik.vzd.model.V1_12_6.LogEntry();

            userV1_12_6Certificate = new de.gematik.vzd.model.V1_12_6.UserCertificate();
            userV1_12_6Certificate.setDn(new de.gematik.vzd.model.V1_12_6.DistinguishedName());

            distinguishedNameV1_12_6 = new de.gematik.vzd.model.V1_12_6.DistinguishedName();
        }
    }
    public VzdEntryWrapper(DirectoryEntry directoryV1_9_5Entry) {
        this.directoryV1_9_5Entry = directoryV1_9_5Entry;
    }
    public VzdEntryWrapper(de.gematik.vzd.model.V1_12_6.DirectoryEntry directoryV1_12_6Entry) {
        this.directoryV1_12_6Entry = directoryV1_12_6Entry;
    }

    public VzdEntryWrapper(LogEntry logV1_9_5Entry) {
        this.logV1_9_5Entry = logV1_9_5Entry;
    }
    public VzdEntryWrapper(de.gematik.vzd.model.V1_12_6.LogEntry logV1_12_6Entry) {
        this.logV1_12_6Entry = logV1_12_6Entry;
    }

    public VzdEntryWrapper(UserCertificate userV1_9_5Certificate) {
        this.userV1_9_5Certificate = userV1_9_5Certificate;
    }
    public VzdEntryWrapper(de.gematik.vzd.model.V1_12_6.UserCertificate userV1_12_6Certificate) {
        this.userV1_12_6Certificate = userV1_12_6Certificate;
    }

    public VzdEntryWrapper(DistinguishedName distinguishedNameV1_9_5) {
        this.distinguishedNameV1_9_5 = distinguishedNameV1_9_5;
    }
    public VzdEntryWrapper(de.gematik.vzd.model.V1_12_6.DistinguishedName distinguishedNameV1_12_6) {
        this.distinguishedNameV1_12_6 = distinguishedNameV1_12_6;
    }

    public void writeDirectoryEntry(File f) throws Exception {
        if (this.directoryV1_9_5Entry != null) {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(f, this.directoryV1_9_5Entry);
        }
        else if (this.directoryV1_12_6Entry != null) {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(f, this.directoryV1_12_6Entry);
        }
        else {
            throw new IllegalStateException("no directory entry found");
        }
    }

    public void setDirectoryEntryUid(String uid) {
        if (this.directoryV1_9_5Entry != null) {
            this.getDirectoryV1_9_5Entry().getDirectoryEntryBase().getDn().setUid(uid);
        }
        else if (this.directoryV1_12_6Entry != null) {
            this.getDirectoryV1_12_6Entry().getDirectoryEntryBase().getDn().setUid(uid);
        }
        else {
            throw new IllegalStateException("no directory entry found");
        }
    }

    public void setUserCertificateDnCn(String cn) {
        if (this.userV1_9_5Certificate != null) {
            this.userV1_9_5Certificate.getDn().setCn(cn);
        }
        else if (this.userV1_12_6Certificate != null) {
            this.userV1_12_6Certificate.getDn().setCn(cn);
        }
        else {
            throw new IllegalStateException("no usercertificate entry found");
        }
    }

    public String extractLogEntryLogTime() {
        if (this.logV1_9_5Entry != null) {
            return logV1_9_5Entry.getLogTime();
        }
        else if (this.logV1_12_6Entry != null) {
            return logV1_12_6Entry.getLogTime();
        }
        throw new IllegalStateException("no log entry found");
    }

    public String extractLogEntryOperation() {
        if (this.logV1_9_5Entry != null) {
            return logV1_9_5Entry.getOperation().getValue();
        }
        else if (this.logV1_12_6Entry != null) {
            return logV1_12_6Entry.getOperation().getValue();
        }
        throw new IllegalStateException("no log entry found");
    }

    public String extractLogEntryClientID() {
        if (this.logV1_9_5Entry != null) {
            return logV1_9_5Entry.getClientID();
        }
        else if (this.logV1_12_6Entry != null) {
            return logV1_12_6Entry.getClientID();
        }
        throw new IllegalStateException("no log entry found");
    }

    public Boolean extractLogEntryNoDataChanged() {
        if (this.logV1_9_5Entry != null) {
            return logV1_9_5Entry.getNoDataChanged();
        }
        else if (this.logV1_12_6Entry != null) {
            return logV1_12_6Entry.getNoDataChanged();
        }
        throw new IllegalStateException("no log entry found");
    }

    public String extractDistinguishedNameCn() {
        if (this.distinguishedNameV1_9_5 != null) {
            return this.distinguishedNameV1_9_5.getCn();
        }
        else if (this.distinguishedNameV1_12_6 != null) {
            return this.distinguishedNameV1_12_6.getCn();
        }
        throw new IllegalStateException("no distinguishedName entry found");
    }

    public String extractDistinguishedNameUid() {
        if (this.distinguishedNameV1_9_5 != null) {
            return this.distinguishedNameV1_9_5.getUid();
        }
        else if (this.distinguishedNameV1_12_6 != null) {
            return this.distinguishedNameV1_12_6.getUid();
        }
        throw new IllegalStateException("no distinguishedName entry found");
    }

    public String extractUserCertificateDnCn() {
        if (this.userV1_9_5Certificate != null) {
            return this.userV1_9_5Certificate.getDn().getCn();
        }
        else if (this.userV1_12_6Certificate != null) {
            return this.userV1_12_6Certificate.getDn().getCn();
        }
        throw new IllegalStateException("no usercertificate entry found");
    }

    public Boolean extractUserCertificateActive() {
        if (this.userV1_9_5Certificate != null) {
            return this.userV1_9_5Certificate.getActive();
        }
        else if (this.userV1_12_6Certificate != null) {
            return this.userV1_12_6Certificate.getActive();
        }
        throw new IllegalStateException("no usercertificate entry found");
    }

    public String extractUserCertificatePublicKeyAlgorithm() {
        if (this.userV1_9_5Certificate != null) {
            return this.userV1_9_5Certificate.getPublicKeyAlgorithm();
        }
        else if (this.userV1_12_6Certificate != null) {
            return this.userV1_12_6Certificate.getPublicKeyAlgorithm();
        }
        throw new IllegalStateException("no usercertificate entry found");
    }

    public String extractUserCertificateContent() {
        if (this.userV1_9_5Certificate != null) {
            return this.userV1_9_5Certificate.getUserCertificate();
        }
        else if (this.userV1_12_6Certificate != null) {
            return this.userV1_12_6Certificate.getUserCertificate();
        }
        throw new IllegalStateException("no usercertificate entry found");
    }

    public String extractUserCertificateNotAfter() {
        if (this.userV1_9_5Certificate != null) {
            return this.userV1_9_5Certificate.getNotAfter();
        }
        else if (this.userV1_12_6Certificate != null) {
            return this.userV1_12_6Certificate.getNotAfter();
        }
        throw new IllegalStateException("no usercertificate entry found");
    }

    public String extractUserCertificateNotBefore() {
        if (this.userV1_9_5Certificate != null) {
            return this.userV1_9_5Certificate.getNotBefore();
        }
        else if (this.userV1_12_6Certificate != null) {
            return this.userV1_12_6Certificate.getNotBefore();
        }
        throw new IllegalStateException("no usercertificate entry found");
    }

    public String extractUserCertificateTelematikId() {
        if (this.userV1_9_5Certificate != null) {
            return this.userV1_9_5Certificate.getTelematikID();
        }
        else if (this.userV1_12_6Certificate != null) {
            return this.userV1_12_6Certificate.getTelematikID();
        }
        throw new IllegalStateException("no usercertificate found");
    }

    public EnumTriValue extractDirectoryEntryPersonalEntry() {
        if (this.directoryV1_9_5Entry != null) {
            return EnumTriValue.getFromBool(this.directoryV1_9_5Entry.getDirectoryEntryBase().getPersonalEntry());
        }
        else if (this.directoryV1_12_6Entry != null) {
            return EnumTriValue.getFromBool(this.directoryV1_12_6Entry.getDirectoryEntryBase().getPersonalEntry());
        }
        throw new IllegalStateException("no directory entry found");
    }

    public EnumTriValue extractDirectoryEntryDataFromAuthority() {
        if (this.directoryV1_9_5Entry != null) {
            return EnumTriValue.getFromBool(this.directoryV1_9_5Entry.getDirectoryEntryBase().getDataFromAuthority());
        }
        else if (this.directoryV1_12_6Entry != null) {
            return EnumTriValue.getFromBool(this.directoryV1_12_6Entry.getDirectoryEntryBase().getDataFromAuthority());
        }
        throw new IllegalStateException("no directory entry found");
    }

    public EnumTriValue extractDirectoryEntryActive() {
        if (this.directoryV1_9_5Entry != null) {
            return EnumTriValue.getFromBool(this.directoryV1_9_5Entry.getDirectoryEntryBase().getActive());
        }
        else if (this.directoryV1_12_6Entry != null) {
            return EnumTriValue.getFromBool(this.directoryV1_12_6Entry.getDirectoryEntryBase().getActive());
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryTelematikId() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getTelematikID();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getTelematikID();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryChangeDateTime() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getChangeDateTime();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getChangeDateTime();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public List<String> extractDirectoryEntryHolder() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getHolder();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getHolder();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public List<String> extractDirectoryEntryMeta() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getMeta();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getMeta();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryMaxKOMLEadr() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getMaxKOMLEadr();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getMaxKOMLEadr();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryStateOrProvinceName() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getStateOrProvinceName();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getStateOrProvinceName();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntrySn() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getSn();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getSn();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryGivenName() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getGivenName();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getGivenName();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryOtherName() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getOtherName();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getOtherName();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryDisplayName() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getDisplayName();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getDisplayName();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryCn() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getCn();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getCn();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryTitle() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getTitle();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getTitle();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryOrganization() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getOrganization();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getOrganization();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryStreetAddress() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getStreetAddress();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getStreetAddress();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryPostalCode() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getPostalCode();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getPostalCode();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryLocalityName() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getLocalityName();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getLocalityName();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryCountryCode() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getCountryCode();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getCountryCode();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public List<String> extractDirectoryEntrySpecialization() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getSpecialization();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getSpecialization();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public List<String> extractDirectoryEntryDomainID() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getDomainID();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getDomainID();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public List<String> extractDirectoryEntryEntryType() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getEntryType();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getEntryType();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryUid() {
        if (this.directoryV1_9_5Entry != null) {
            return this.directoryV1_9_5Entry.getDirectoryEntryBase().getDn().getUid();
        }
        else if (this.directoryV1_12_6Entry != null) {
            return this.directoryV1_12_6Entry.getDirectoryEntryBase().getDn().getUid();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public boolean checkDirectoryEntryHolder(String holder) {
        if (this.directoryV1_9_5Entry != null) {
            return !this.directoryV1_9_5Entry.getDirectoryEntryBase().getHolder().isEmpty() && this.directoryV1_9_5Entry.getDirectoryEntryBase().getHolder().contains(holder);
        }
        else if (this.directoryV1_12_6Entry != null) {
            return !this.directoryV1_12_6Entry.getDirectoryEntryBase().getHolder().isEmpty() && this.directoryV1_12_6Entry.getDirectoryEntryBase().getHolder().contains(holder);
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryDomainIDAsStr() {
        if (this.directoryV1_9_5Entry != null && this.directoryV1_9_5Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_9_5Entry.getDirectoryEntryBase().getDomainID());
        }
        else if (this.directoryV1_12_6Entry != null && this.directoryV1_12_6Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_12_6Entry.getDirectoryEntryBase().getDomainID());
        }
        return "";
    }
    public String extractDirectoryEntrySpecializationAsStr() {
        if (this.directoryV1_9_5Entry != null && this.directoryV1_9_5Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_9_5Entry.getDirectoryEntryBase().getSpecialization());
        }
        else if (this.directoryV1_12_6Entry != null && this.directoryV1_12_6Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_12_6Entry.getDirectoryEntryBase().getSpecialization());
        }
        return "";
    }
    public String extractDirectoryEntryProfessionOIDAsStr() {
        if (this.directoryV1_9_5Entry != null && this.directoryV1_9_5Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_9_5Entry.getDirectoryEntryBase().getProfessionOID());
        }
        else if (this.directoryV1_12_6Entry != null && this.directoryV1_12_6Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_12_6Entry.getDirectoryEntryBase().getProfessionOID());
        }
        return "";
    }
    public String extractDirectoryEntryHolderAsStr() {
        if (this.directoryV1_9_5Entry != null && this.directoryV1_9_5Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_9_5Entry.getDirectoryEntryBase().getHolder());
        }
        else if (this.directoryV1_12_6Entry != null && this.directoryV1_12_6Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_12_6Entry.getDirectoryEntryBase().getHolder());
        }
        return "";
    }
    public String extractDirectoryEntryMetaAsStr() {
        if (this.directoryV1_9_5Entry != null && this.directoryV1_9_5Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_9_5Entry.getDirectoryEntryBase().getMeta());
        }
        else if (this.directoryV1_12_6Entry != null && this.directoryV1_12_6Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_12_6Entry.getDirectoryEntryBase().getMeta());
        }
        return "";
    }

    public void extractDirectoryEntryKimMailInfos(List<String> fadMailAttrs, List<String> fadKomLeDataAttrs, List<String> fadKimDataAttrs) {
        if (this.directoryV1_9_5Entry != null && this.directoryV1_9_5Entry.getFachdaten() != null) {
            for (Iterator<de.gematik.vzd.model.V1_9_5.Fachdaten> iterator = this.directoryV1_9_5Entry.getFachdaten().iterator(); iterator.hasNext(); ) {
                de.gematik.vzd.model.V1_9_5.Fachdaten fachdaten = iterator.next();
                if (fachdaten.getFAD1() == null) {
                    continue;
                }
                for (Iterator<de.gematik.vzd.model.V1_9_5.FAD1> fad1Iterator = fachdaten.getFAD1().iterator(); fad1Iterator.hasNext(); ) {
                    de.gematik.vzd.model.V1_9_5.FAD1 fad1 = fad1Iterator.next();
                    if (fad1.getMail() == null && fad1.getKomLeData() == null) {
                        continue;
                    }
                    for (Iterator<String> mailIterator = fad1.getMail().iterator(); mailIterator.hasNext(); ) {
                        String mail = mailIterator.next();
                        fadMailAttrs.add(mail);
                    }
                    for (Iterator<de.gematik.vzd.model.V1_9_5.FAD1KomLeDataInner> mailIterator = fad1.getKomLeData().iterator(); mailIterator.hasNext(); ) {
                        de.gematik.vzd.model.V1_9_5.FAD1KomLeDataInner komLeDataInner = mailIterator.next();
                        fadKomLeDataAttrs.add(komLeDataInner.getMail()+","+komLeDataInner.getVersion());
                    }
                }
            }
        }
        else if (this.directoryV1_12_6Entry != null && this.directoryV1_12_6Entry.getFachdaten() != null) {
            for (Iterator<Fachdaten> iterator = this.directoryV1_12_6Entry.getFachdaten().iterator(); iterator.hasNext(); ) {
                Fachdaten fachdaten = iterator.next();
                if (fachdaten.getFAD1() == null) {
                    continue;
                }
                for (Iterator<FAD1> fad1Iterator = fachdaten.getFAD1().iterator(); fad1Iterator.hasNext(); ) {
                    FAD1 fad1 = fad1Iterator.next();
                    if (fad1.getMail() == null && fad1.getKimData() == null && fad1.getKomLeData() == null) {
                        continue;
                    }
                    for (Iterator<String> mailIterator = fad1.getMail().iterator(); mailIterator.hasNext(); ) {
                        String mail = mailIterator.next();
                        fadMailAttrs.add(mail);
                    }
                    for (Iterator<FAD1KimDataInner> mailIterator = fad1.getKimData().iterator(); mailIterator.hasNext(); ) {
                        FAD1KimDataInner kimDataInner = mailIterator.next();
                        fadKimDataAttrs.add(kimDataInner.getMail()+","+kimDataInner.getVersion()+","+String.join(";",kimDataInner.getAppTags()));
                    }
                    for (Iterator<FAD1KomLeDataInner> mailIterator = fad1.getKomLeData().iterator(); mailIterator.hasNext(); ) {
                        FAD1KomLeDataInner komLeDataInner = mailIterator.next();
                        fadKomLeDataAttrs.add(komLeDataInner.getMail()+","+komLeDataInner.getVersion());
                    }
                }
            }
        }
    }

}
