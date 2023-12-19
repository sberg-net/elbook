package net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler;

import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.PagingInfo;

public interface ICommandResultCallbackHandler {
    void handleDirectoryEntry(AbstractCommand commandType, Object directoryEntry);
    void handleLogEntry(AbstractCommand commandType, Object logEntry);
    void handleUserCertificate(AbstractCommand commandType, Object userCertificate);
    void handleInfoObject(AbstractCommand commandType, Object infoObject);
    void handleDistinguishedName(AbstractCommand commandType, Object distinguishedName);
    void handlePagingInfo(AbstractCommand commandType, PagingInfo pagingInfo);
    void handle(AbstractCommand commandType, AbstractCommandResultCallbackHandler.ResultReason resultReason);
    void handle(AbstractCommand commandType, AbstractCommandResultCallbackHandler.ServerResult serverResult);
    void handle(AbstractCommand commandType, Exception exception);
}
