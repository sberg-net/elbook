package net.sberg.elbook.vzdclientcmpts;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientDetails {
    private LocalDateTime tokenvalidationDate;
    private String id;
    private Integer mutex;
    private int usedCount;
    private LocalDateTime lastUsed;
    private boolean inUse = false;
}
