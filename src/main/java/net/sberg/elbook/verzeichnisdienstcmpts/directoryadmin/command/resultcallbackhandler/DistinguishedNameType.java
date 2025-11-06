package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class DistinguishedNameType {
    private String cn;
    private String uid;
    private List<String> dc = new ArrayList<>();
    private List<String> ou = new ArrayList<>();
}
