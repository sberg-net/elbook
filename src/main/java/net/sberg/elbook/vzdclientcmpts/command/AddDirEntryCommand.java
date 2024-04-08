package net.sberg.elbook.vzdclientcmpts.command;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AddDirEntryCommand extends AbstractCommand {

    //id attributes
    private List<String> domainId;

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

    private List<String> certContents = new ArrayList<>();

    @Override
    public EnumCommand command() {
        return EnumCommand.ADD_DIR_ENTRY;
    }
}
