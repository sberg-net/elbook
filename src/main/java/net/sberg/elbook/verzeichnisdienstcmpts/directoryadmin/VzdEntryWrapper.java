package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.vzd.model.V1_12_8.*;
import lombok.Data;
import net.sberg.elbook.common.StringUtils;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client.TiVZDProperties;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.EnumTriValue;

import java.io.File;
import java.util.Iterator;
import java.util.List;

@Data
public class VzdEntryWrapper {

    private DirectoryEntry directoryV1_12_8Entry;
    
    private LogEntry logV1_12_8Entry;
    
    private UserCertificate userV1_12_8Certificate;
    
    private DistinguishedName distinguishedNameV1_12_8;
    
    private TiVZDProperties tiVZDProperties;

    private VzdEntryWrapper() {}
    public VzdEntryWrapper(TiVZDProperties tiVZDProperties) {
        this.tiVZDProperties = tiVZDProperties;
        if (tiVZDProperties.getApiVersion().equals(TiVZDProperties.API_VERSION_V1_12_8)) {

            directoryV1_12_8Entry = new DirectoryEntry();
            BaseDirectoryEntry baseDirectoryEntry = new BaseDirectoryEntry();
            DistinguishedName dn = new DistinguishedName();
            baseDirectoryEntry.setDn(dn);
            directoryV1_12_8Entry.setDirectoryEntryBase(baseDirectoryEntry);

            logV1_12_8Entry = new LogEntry();

            userV1_12_8Certificate = new UserCertificate();
            userV1_12_8Certificate.setDn(new DistinguishedName());

            distinguishedNameV1_12_8 = new DistinguishedName();
        }
    }
    public VzdEntryWrapper(DirectoryEntry directoryV1_12_8Entry) {
        this.directoryV1_12_8Entry = directoryV1_12_8Entry;
    }
    
    public VzdEntryWrapper(LogEntry logV1_12_8Entry) {
        this.logV1_12_8Entry = logV1_12_8Entry;
    }
    
    public VzdEntryWrapper(UserCertificate userV1_12_8Certificate) {
        this.userV1_12_8Certificate = userV1_12_8Certificate;
    }
    
    public VzdEntryWrapper(DistinguishedName distinguishedNameV1_12_8) {
        this.distinguishedNameV1_12_8 = distinguishedNameV1_12_8;
    }
    
    public void writeDirectoryEntry(File f) throws Exception {
        if (this.directoryV1_12_8Entry != null) {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(f, this.directoryV1_12_8Entry);
        }
        else {
            throw new IllegalStateException("no directory entry found");
        }
    }

    public void setDirectoryEntryUid(String uid) {
        if (this.directoryV1_12_8Entry != null) {
            this.directoryV1_12_8Entry.getDirectoryEntryBase().getDn().setUid(uid);
        }
        else {
            throw new IllegalStateException("no directory entry found");
        }
    }

    public void setUserCertificateDnCn(String cn) {
        if (this.userV1_12_8Certificate != null) {
            this.userV1_12_8Certificate.getDn().setCn(cn);
        }
        else {
            throw new IllegalStateException("no usercertificate entry found");
        }
    }

    public String extractLogEntryLogTime() {
        if (this.logV1_12_8Entry != null) {
            return logV1_12_8Entry.getLogTime();
        }
        throw new IllegalStateException("no log entry found");
    }

    public String extractLogEntryOperation() {
        if (this.logV1_12_8Entry != null) {
            return logV1_12_8Entry.getOperation().getValue();
        }
        throw new IllegalStateException("no log entry found");
    }

    public String extractLogEntryClientID() {
        if (this.logV1_12_8Entry != null) {
            return logV1_12_8Entry.getClientID();
        }
        throw new IllegalStateException("no log entry found");
    }

    public Boolean extractLogEntryNoDataChanged() {
        if (this.logV1_12_8Entry != null) {
            return logV1_12_8Entry.getNoDataChanged();
        }
        throw new IllegalStateException("no log entry found");
    }

    public String extractDistinguishedNameCn() {
        if (this.distinguishedNameV1_12_8 != null) {
            return this.distinguishedNameV1_12_8.getCn();
        }
        throw new IllegalStateException("no distinguishedName entry found");
    }

    public String extractDistinguishedNameUid() {
        if (this.distinguishedNameV1_12_8 != null) {
            return this.distinguishedNameV1_12_8.getUid();
        }
        throw new IllegalStateException("no distinguishedName entry found");
    }

    public String extractUserCertificateDnCn() {
        if (this.userV1_12_8Certificate != null) {
            return this.userV1_12_8Certificate.getDn().getCn();
        }
        throw new IllegalStateException("no usercertificate entry found");
    }

    public Boolean extractUserCertificateActive() {
        if (this.userV1_12_8Certificate != null) {
            return this.userV1_12_8Certificate.getActive();
        }
        throw new IllegalStateException("no usercertificate entry found");
    }

    public String extractUserCertificatePublicKeyAlgorithm() {
        if (this.userV1_12_8Certificate != null) {
            return this.userV1_12_8Certificate.getPublicKeyAlgorithm();
        }
        throw new IllegalStateException("no usercertificate entry found");
    }

    public String extractUserCertificateContent() {
        if (this.userV1_12_8Certificate != null) {
            return this.userV1_12_8Certificate.getUserCertificate();
        }
        throw new IllegalStateException("no usercertificate entry found");
    }

    public String extractUserCertificateNotAfter() {
        if (this.userV1_12_8Certificate != null) {
            return this.userV1_12_8Certificate.getNotAfter();
        }
        throw new IllegalStateException("no usercertificate entry found");
    }

    public String extractUserCertificateNotBefore() {
        if (this.userV1_12_8Certificate != null) {
            return this.userV1_12_8Certificate.getNotBefore();
        }
        throw new IllegalStateException("no usercertificate entry found");
    }

    public String extractUserCertificateTelematikId() {
        if (this.userV1_12_8Certificate != null) {
            return this.userV1_12_8Certificate.getTelematikID();
        }
        throw new IllegalStateException("no usercertificate found");
    }

    public EnumTriValue extractDirectoryEntryPersonalEntry() {
        if (this.directoryV1_12_8Entry != null) {
            return EnumTriValue.getFromBool(this.directoryV1_12_8Entry.getDirectoryEntryBase().getPersonalEntry());
        }
        throw new IllegalStateException("no directory entry found");
    }

    public EnumTriValue extractDirectoryEntryDataFromAuthority() {
        if (this.directoryV1_12_8Entry != null) {
            return EnumTriValue.getFromBool(this.directoryV1_12_8Entry.getDirectoryEntryBase().getDataFromAuthority());
        }
        throw new IllegalStateException("no directory entry found");
    }

    public EnumTriValue extractDirectoryEntryActive() {
        if (this.directoryV1_12_8Entry != null) {
            return EnumTriValue.getFromBool(this.directoryV1_12_8Entry.getDirectoryEntryBase().getActive());
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryTelematikId() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getTelematikID();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryChangeDateTime() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getChangeDateTime();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public List<String> extractDirectoryEntryHolder() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getHolder();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public List<String> extractDirectoryEntryMeta() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getMeta();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryMaxKOMLEadr() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getMaxKOMLEadr();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryStateOrProvinceName() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getStateOrProvinceName();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntrySn() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getSn();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryGivenName() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getGivenName();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryOtherName() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getOtherName();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryDisplayName() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getDisplayName();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryCn() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getCn();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryTitle() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getTitle();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryOrganization() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getOrganization();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryStreetAddress() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getStreetAddress();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryPostalCode() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getPostalCode();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryLocalityName() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getLocalityName();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryCountryCode() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getCountryCode();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public List<String> extractDirectoryEntrySpecialization() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getSpecialization();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public List<String> extractDirectoryEntryDomainID() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getDomainID();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public List<String> extractDirectoryEntryLanr() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getLanr();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryProvidedBy() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getProvidedBy();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public List<String> extractDirectoryEntryEntryType() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getEntryType();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryUid() {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getDn().getUid();
        }
        throw new IllegalStateException("no directory entry found");
    }

    public boolean checkDirectoryEntryHolder(String holder) {
        if (this.directoryV1_12_8Entry != null) {
            return this.directoryV1_12_8Entry.getDirectoryEntryBase().getHolder().isEmpty() || (!this.directoryV1_12_8Entry.getDirectoryEntryBase().getHolder().isEmpty() && this.directoryV1_12_8Entry.getDirectoryEntryBase().getHolder().contains(holder));
        }
        throw new IllegalStateException("no directory entry found");
    }

    public String extractDirectoryEntryDomainIDAsStr() {
        if (this.directoryV1_12_8Entry != null && this.directoryV1_12_8Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_12_8Entry.getDirectoryEntryBase().getDomainID());
        }
        return "";
    }
    public String extractDirectoryEntryLanrAsStr() {
        if (this.directoryV1_12_8Entry != null && this.directoryV1_12_8Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_12_8Entry.getDirectoryEntryBase().getLanr());
        }
        return "";
    }
    public String extractDirectoryEntrySpecializationAsStr() {
        if (this.directoryV1_12_8Entry != null && this.directoryV1_12_8Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_12_8Entry.getDirectoryEntryBase().getSpecialization());
        }
        return "";
    }
    public String extractDirectoryEntryProfessionOIDAsStr() {
        if (this.directoryV1_12_8Entry != null && this.directoryV1_12_8Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_12_8Entry.getDirectoryEntryBase().getProfessionOID());
        }
        return "";
    }
    public String extractDirectoryEntryHolderAsStr() {
        if (this.directoryV1_12_8Entry != null && this.directoryV1_12_8Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_12_8Entry.getDirectoryEntryBase().getHolder());
        }
        return "";
    }
    public String extractDirectoryEntryMetaAsStr() {
        if (this.directoryV1_12_8Entry != null && this.directoryV1_12_8Entry.getDirectoryEntryBase() != null) {
            return StringUtils.listToString(directoryV1_12_8Entry.getDirectoryEntryBase().getMeta());
        }
        return "";
    }

    public void extractDirectoryEntryKimMailInfos(List<String> fadMailAttrs, List<String> fadKomLeDataAttrs, List<String> fadKimDataAttrs) {
        if (this.directoryV1_12_8Entry != null && this.directoryV1_12_8Entry.getFachdaten() != null) {
            for (Iterator<de.gematik.vzd.model.V1_12_8.Fachdaten> iterator = this.directoryV1_12_8Entry.getFachdaten().iterator(); iterator.hasNext(); ) {
                de.gematik.vzd.model.V1_12_8.Fachdaten fachdaten = iterator.next();
                if (fachdaten.getFAD1() == null) {
                    continue;
                }
                for (Iterator<de.gematik.vzd.model.V1_12_8.FAD1> fad1Iterator = fachdaten.getFAD1().iterator(); fad1Iterator.hasNext(); ) {
                    de.gematik.vzd.model.V1_12_8.FAD1 fad1 = fad1Iterator.next();
                    if (fad1.getMail() == null && fad1.getKomLeData() == null) {
                        continue;
                    }
                    for (Iterator<String> mailIterator = fad1.getMail().iterator(); mailIterator.hasNext(); ) {
                        String mail = mailIterator.next();
                        fadMailAttrs.add(mail);
                    }
                    for (Iterator<FAD1KomLeDataInner> mailIterator = fad1.getKomLeData().iterator(); mailIterator.hasNext(); ) {
                        FAD1KomLeDataInner komLeDataInner = mailIterator.next();
                        fadKomLeDataAttrs.add(komLeDataInner.getMail()+","+komLeDataInner.getVersion());
                    }
                    for (Iterator<FAD1KimDataInner> mailIterator = fad1.getKimData().iterator(); mailIterator.hasNext(); ) {
                        FAD1KimDataInner kimDataInner = mailIterator.next();
                        fadKimDataAttrs.add(kimDataInner.getMail()+","+kimDataInner.getVersion()+","+String.join(";",kimDataInner.getAppTags()));
                    }
                }
            }
        }
    }

}
