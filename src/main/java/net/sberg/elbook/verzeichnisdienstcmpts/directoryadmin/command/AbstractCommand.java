package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public abstract class AbstractCommand {
    private String id = UUID.randomUUID().toString();
    private String uid;
    private String businessId;
    private String telematikId;
    private List<String> holder;
    public abstract EnumCommand command();
}
