package net.sberg.elbook.vzdclientcmpts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(ClientConnectionPool.class);

    private LinkedBlockingQueue<ClientImpl> connections = new LinkedBlockingQueue<>();
    private List<ClientImpl> usedConnections = new ArrayList<>();
    private Integer mutex;
    private LocalDateTime lastTimeGet;
    private LocalDateTime lastTimeReleased;
    private TiVZDProperties tiVZDProperties;

    public ClientConnectionPool(Integer mutex, TiVZDProperties tiVZDProperties) {
        this.mutex = mutex;
        this.tiVZDProperties = tiVZDProperties;
    }

    public boolean deleteConnection(String id) {
        synchronized (mutex) {
            ClientImpl toDeleteClient = null;
            for (Iterator<ClientImpl> iterator = connections.iterator(); iterator.hasNext(); ) {
                ClientImpl next =  iterator.next();
                if (next.getId().equals(id)) {
                    toDeleteClient = next;
                    break;
                }
            }
            if (toDeleteClient != null) {
                connections.remove(toDeleteClient);
                log.debug(mutex+ " - client with the id "+id+" deleted");
                return true;
            }
            log.debug(mutex+ " - client with the id "+id+" not found");
            return false;
        }
    }

    public ClientImpl getConnection() throws Exception {
        synchronized (mutex) {
            lastTimeGet = LocalDateTime.now();
            log.debug(mutex+ " - try to get connection (" + usedConnections.size() + " / " + connections.size() + ")");
            ClientImpl connection = null;
            if (connections.isEmpty()) {
                connections.add(new ClientImpl(tiVZDProperties, tiVZDProperties.getAuthId()+"_"+tiVZDProperties.determineEnvironment()+"_"+System.nanoTime()));
            }
            connection = connections.take();
            connection.use();
            usedConnections.add(connection);
            log.debug(mutex+ " - "+connection.getId()+ " - connection found and return (" + usedConnections.size() + " / " + connections.size() + ")");
            return connection;
        }
    }

    public List<ClientDetails> createDetails() {
        synchronized (mutex) {
            List<ClientDetails> details = new ArrayList<>();
            for (Iterator<ClientImpl> iter = connections.iterator(); iter.hasNext(); ) {
                ClientImpl client = iter.next();
                details.add(client.createDetails(mutex, false));
            }
            for (Iterator<ClientImpl> iter = usedConnections.iterator(); iter.hasNext(); ) {
                ClientImpl client = iter.next();
                details.add(client.createDetails(mutex, true));
            }
            return details;
        }
    }

    public void releaseConnection(ClientImpl client) {
        synchronized (mutex) {
            lastTimeReleased = LocalDateTime.now();
            log.debug(mutex+ " - "+client.getId()+ " - try to releaseConnection (" + usedConnections.size() + " / " + connections.size() + ")");
            if (usedConnections.contains(client) && !connections.contains(client)) {
                usedConnections.remove(client);
                if (client.getUsedCount() > 60000) {
                    log.debug(mutex + " - " + client.getId() + " - connection found and reached limit of 20000");
                }
                else {
                    connections.add(client);
                    log.debug(mutex + " - " + client.getId() + " - connection found and released (" + usedConnections.size() + " / " + connections.size() + ")");
                }
            }
            else if (usedConnections.contains(client) && connections.contains(client)) {
                usedConnections.remove(client);
                log.debug(mutex+ " - "+client.getId()+ " - ALERT!!! connection found in usedConnections and connections -> released (" + usedConnections.size() + " / " + connections.size() + ")");
            }
            else if (!usedConnections.contains(client) && connections.contains(client)) {
                connections.add(client);
                log.debug(mutex+ " - "+client.getId()+ " - ALERT!!! connection found in connections -> released (" + usedConnections.size() + " / " + connections.size() + ")");
            }
            else if (!usedConnections.contains(client) && !connections.contains(client)) {
                log.debug(mutex+ " - "+client.getId()+ " - ALERT!!! connection not found (" + usedConnections.size() + " / " + connections.size() + ")");
            }
        }
    }
}
