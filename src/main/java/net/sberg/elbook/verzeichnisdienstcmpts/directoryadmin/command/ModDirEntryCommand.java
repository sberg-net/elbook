package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command;

import lombok.Data;

import java.util.List;

@Data
public class ModDirEntryCommand extends AbstractCommand {

    //id attributes
    private List<String> domainId;
    private List<String> lanr;
    private String providedBy;

    //name attributes
    private String givenName;
    private String sn;
    private String cn;
    private String otherName;
    private String displayName;
    private String organization;
    private String title;

    //address attributes
    private String streetAddress;
    private String postalCode;
    private String localityName;
    private EnumStateOrProvinceName stateOrProvinceName;
    private String countryCode;

    //profession attributes
    private List<String> specialization;
    private EnumEntryType entryType;

    //system attributes
    private String maxKomLeAdr;
    private EnumTriValue active = EnumTriValue.UNDEFINED;
    private List<String> meta;

    @Override
    public EnumCommand command() {
        return EnumCommand.MOD_DIR_ENTRY;
    }

    public void fill(AddDirEntryCommand addDirEntryCommand) {
        setUid(addDirEntryCommand.getUid());
        setTelematikId(addDirEntryCommand.getTelematikId());
        setDomainId(addDirEntryCommand.getDomainId());
        setLanr(addDirEntryCommand.getLanr());
        setProvidedBy(addDirEntryCommand.getProvidedBy());

        setCn(addDirEntryCommand.getCn());
        setSn(addDirEntryCommand.getSn());
        setTitle(addDirEntryCommand.getTitle());
        setDisplayName(addDirEntryCommand.getDisplayName());
        setOtherName(addDirEntryCommand.getOtherName());
        setOrganization(addDirEntryCommand.getOrganization());
        setGivenName(addDirEntryCommand.getGivenName());

        setPostalCode(addDirEntryCommand.getPostalCode());
        setCountryCode(addDirEntryCommand.getCountryCode());
        setStreetAddress(addDirEntryCommand.getStreetAddress());
        setCountryCode(addDirEntryCommand.getCountryCode());
        setStateOrProvinceName(addDirEntryCommand.getStateOrProvinceName());

        setSpecialization(addDirEntryCommand.getSpecialization());
        setEntryType(addDirEntryCommand.getEntryType());

        setActive(addDirEntryCommand.getActive());
        setMeta(addDirEntryCommand.getMeta());
        setHolder(addDirEntryCommand.getHolder());
        setMaxKomLeAdr(addDirEntryCommand.getMaxKomLeAdr());

    }
}
