package net.sberg.elbook.vzdclientcmpts.command;

import lombok.Data;

@Data
public class PagingInfo {
    private int pagingSize = 100;
    private int maxSize = -1;
    private String pagingCookie;
    private boolean oneTimePaging;
}
