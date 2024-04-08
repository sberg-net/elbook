package net.sberg.elbook.vzdclientcmpts.command;

import lombok.Data;

import java.util.List;

@Data
public class ReadDirEntryCommand extends AbstractCommand {

    //id attributes
    private List<String> domainId;
    private String telematikIdSubstr;

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
    private List<String> professionOid;
    private EnumEntryType entryType;

    //system attributes
    private String maxKomLeAdr;
    private EnumTriValue active = EnumTriValue.UNDEFINED;
    private List<String> meta;
    private String changeDateTimeFrom;
    private String changeDateTimeTo;
    private EnumTriValue personalEntry = EnumTriValue.UNDEFINED;
    private EnumTriValue dataFromAuthority = EnumTriValue.UNDEFINED;
    private Boolean baseEntryOnly = Boolean.TRUE;
    private String providedBy;

    @Override
    public EnumCommand command() {
        return EnumCommand.READ_DIR_ENTRY;
    }
}
