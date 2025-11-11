package net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.client;

import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.AbstractCommand;
import net.sberg.elbook.verzeichnisdienstcmpts.directoryadmin.command.resultcallbackhandler.DefaultCommandResultCallbackHandler;
import net.sberg.elbook.verzeichnisdienstcmpts.fhir.client.TiFhirProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TiVZDConnector {

    private static final Logger log = LoggerFactory.getLogger(TiVZDConnector.class);

    private Map<Object, ClientConnectionPool> poolMap = Collections.synchronizedMap(new HashMap<>());

    public DefaultCommandResultCallbackHandler executeCommand(AbstractCommand command, TiVZDProperties lTiVZDProperties) throws Exception {
        List<AbstractCommand> commands = new ArrayList<>();
        commands.add(command);
        return executeCommands(commands, lTiVZDProperties);
    }

    public DefaultCommandResultCallbackHandler executeCommands(List<AbstractCommand> commands, TiVZDProperties lTiVZDProperties) throws Exception {
        return execute(commands, lTiVZDProperties);
    }

    public List<ClientDetails> getClientDetails() {
        List<ClientDetails> details = new ArrayList<>();
        for (Iterator<Object> iterator = poolMap.keySet().iterator(); iterator.hasNext(); ) {
            Object mutex =  iterator.next();
            details.addAll(poolMap.get(mutex).createDetails());
        }
        details.sort((ClientDetails h1, ClientDetails h2) -> -1 * (h1.getLastUsed().compareTo(h2.getLastUsed())));
        return details;
    }

    public boolean deleteConnection(String id, Integer mutex) {
        synchronized (mutex) {
            if (!poolMap.containsKey(mutex)) {
                log.debug("clientpool connection with the mutex "+mutex+" not found");
                return false;
            }
            return poolMap.get(mutex).deleteConnection(id);
        }
    }

    private ClientImpl getConnection(TiVZDProperties lTiVZDProperties) throws Exception {
        Integer mutex = TiVZDProperties.checkMutex(lTiVZDProperties);
        synchronized (mutex) {
            if (!poolMap.containsKey(mutex)) {
                poolMap.put(mutex, new ClientConnectionPool(mutex, lTiVZDProperties));
            }
            ClientImpl client = poolMap.get(mutex).getConnection();
            log.debug("client with the id: "+client.getId()+" used");
            return client;
        }
    }

    private void releaseConnection(TiVZDProperties lTiVZDProperties, ClientImpl client) throws Exception {
        Integer mutex = TiVZDProperties.checkMutex(lTiVZDProperties);
        synchronized (mutex) {
            poolMap.get(mutex).releaseConnection(client);
        }
    }

    private DefaultCommandResultCallbackHandler execute(List<AbstractCommand> commands, TiVZDProperties lTiVZDProperties) throws Exception {
        ClientImpl client = getConnection(lTiVZDProperties);
        DefaultCommandResultCallbackHandler defaultCommandResultCallbackHandler = new DefaultCommandResultCallbackHandler(client).execute(commands);
        releaseConnection(lTiVZDProperties, client);
        return defaultCommandResultCallbackHandler;
    }

    public TiFhirProperties extractTiFhirProperties(TiVZDProperties lTiVZDProperties) throws Exception {
        ClientImpl client = getConnection(lTiVZDProperties);
        releaseConnection(lTiVZDProperties, client);
        return client.getTiFhirProperties();
    }
}
