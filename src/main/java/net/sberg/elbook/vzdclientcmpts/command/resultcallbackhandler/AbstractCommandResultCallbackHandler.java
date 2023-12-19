package net.sberg.elbook.vzdclientcmpts.command.resultcallbackhandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.sberg.elbook.vzdclientcmpts.command.AbstractCommand;
import net.sberg.elbook.vzdclientcmpts.command.PagingInfo;
import org.springframework.http.HttpHeaders;

import java.util.*;

public abstract class AbstractCommandResultCallbackHandler implements ICommandResultCallbackHandler {

    @Data
    @AllArgsConstructor
    public static class ResultReason {
        public static final String NO_SEARCH_RESULTS = "Keine Suchergebnisse";
        public static final String BAD_REQUEST = "Die Suche konnte nicht fehlerfrei verarbeitet werden.";
        public static final String TOO_MANY_RESULTS = "Zu viele Ergebnisse. Bitte schränken Sie die Suche ein.";
        public static final String PRE_EXECUTE_VALIDATION_FAILED = "Die Validierung vor dem Ausführen des Befehls ist fehlgeschlagen";
        private boolean result;
        private String resultReason;
    }

    @Data
    @AllArgsConstructor
    public static class ServerResult {
        public static final String HEADER_RS_DIRECTORY_ADMIN_ERROR = "RS-DIRECTORY-ADMIN-ERROR";
        public static final String HEADER_X_MAXKOMLEADR_LIMIT = "X-MAXKOMLEADR-LIMIT";
        private int statusCode;
        private HttpHeaders headers;
    }

    private Map<String, List> directoryEntries = new HashMap<>();
    private Map<String, List> logEntries = new HashMap<>();
    private Map<String, List> userCertificates = new HashMap<>();
    private Map<String, List> distinguishedNames = new HashMap<>();
    private Map<String, List> infoObjects = new HashMap<>();
    private Map<String, PagingInfo> pagingInfos = new HashMap<>();
    private Map<String, List<Exception>> exceptions = new HashMap<>();
    private Map<String, List<ResultReason>> resultReasons = new HashMap<>();
    private Map<String, List<ServerResult>> serverResults = new HashMap<>();

    public List getDistinguishedNames(String commandId) { return distinguishedNames.get(commandId) == null?new ArrayList():distinguishedNames.get(commandId); }
    public Iterator<String> getDistinguishedNameIter() {
        return distinguishedNames.keySet().iterator();
    }

    public List<Exception> getExceptions(String commandId) { return exceptions.get(commandId) == null?new ArrayList():exceptions.get(commandId); }
    public Iterator<String> getExceptionIter() {
        return exceptions.keySet().iterator();
    }

    public List getDirectoryEntries(String commandId) { return directoryEntries.get(commandId) == null?new ArrayList():directoryEntries.get(commandId); }
    public Iterator<String> getDirectoryEntriesIter() {
        return directoryEntries.keySet().iterator();
    }

    public List getLogEntries(String commandId) { return logEntries.get(commandId) == null?new ArrayList():logEntries.get(commandId); }
    public Iterator<String> getLogEntriesIter() {
        return logEntries.keySet().iterator();
    }

    public List getUserCertificates(String commandId) { return userCertificates.get(commandId) == null?new ArrayList():userCertificates.get(commandId); }
    public Iterator<String> getUserCertificatesIter() {
        return userCertificates.keySet().iterator();
    }

    public List<ResultReason> getResultReasons(String commandId) { return resultReasons.get(commandId) == null?new ArrayList():resultReasons.get(commandId); }
    public Iterator<String> getResultReasonIter() {
        return resultReasons.keySet().iterator();
    }

    public List<ServerResult> getServerResults(String commandId) { return serverResults.get(commandId) == null?new ArrayList():serverResults.get(commandId); }
    public Iterator<String> getServerResultIter() {
        return serverResults.keySet().iterator();
    }

    public List getInfoObjects(String commandId) { return infoObjects.get(commandId) == null?new ArrayList():infoObjects.get(commandId); }
    public Iterator<String> getInfoObjectsIter() {
        return infoObjects.keySet().iterator();
    }

    public PagingInfo getPagingInfo(String commandId) { return pagingInfos.get(commandId); }
    public Iterator<String> getPagingInfoIter() {
        return pagingInfos.keySet().iterator();
    }

    @Override
    public void handle(AbstractCommand command, ServerResult serverResult) {
        if (!serverResults.containsKey(command.getId())) {
            serverResults.put(command.getId(), new ArrayList<>());
        }
        serverResults.get(command.getId()).add(serverResult);
    }

    @Override
    public void handleDirectoryEntry(AbstractCommand command, Object directoryEntry) {
        if (!directoryEntries.containsKey(command.getId())) {
            directoryEntries.put(command.getId(), new ArrayList<>());
        }
        directoryEntries.get(command.getId()).add(directoryEntry);
    }

    @Override
    public void handleLogEntry(AbstractCommand command, Object logEntry) {
        if (!logEntries.containsKey(command.getId())) {
            logEntries.put(command.getId(), new ArrayList<>());
        }
        logEntries.get(command.getId()).add(logEntry);
    }

    @Override
    public void handleUserCertificate(AbstractCommand command, Object userCertificate) {
        if (!userCertificates.containsKey(command.getId())) {
            userCertificates.put(command.getId(), new ArrayList<>());
        }
        userCertificates.get(command.getId()).add(userCertificate);
    }

    @Override
    public void handleInfoObject(AbstractCommand command, Object infoObject) {
        if (!infoObjects.containsKey(command.getId())) {
            infoObjects.put(command.getId(), new ArrayList<>());
        }
        infoObjects.get(command.getId()).add(infoObject);
    }

    @Override
    public void handleDistinguishedName(AbstractCommand command, Object distinguishedName) {
        if (!distinguishedNames.containsKey(command.getId())) {
            distinguishedNames.put(command.getId(), new ArrayList<>());
        }
        distinguishedNames.get(command.getId()).add(distinguishedName);
    }

    @Override
    public void handlePagingInfo(AbstractCommand command, PagingInfo pagingInfo) {
        pagingInfos.put(command.getId(), pagingInfo);
    }

    @Override
    public void handle(AbstractCommand command, ResultReason resultReason) {
        if (!resultReasons.containsKey(command.getId())) {
            resultReasons.put(command.getId(), new ArrayList<>());
        }
        resultReasons.get(command.getId()).add(resultReason);
    }

    @Override
    public void handle(AbstractCommand command, Exception exception) {
        if (!exceptions.containsKey(command.getId())) {
            exceptions.put(command.getId(), new ArrayList<>());
        }
        exceptions.get(command.getId()).add(exception);
    }

    public void clear() {
        this.directoryEntries.clear();
        this.logEntries.clear();
        this.distinguishedNames.clear();
        this.exceptions.clear();
        this.resultReasons.clear();
        this.serverResults.clear();
        this.userCertificates.clear();
        this.infoObjects.clear();
        this.pagingInfos.clear();
    }
}
