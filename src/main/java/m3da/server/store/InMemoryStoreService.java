package m3da.server.store;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In memory thread safe implementation of {@link StoreService}
 */
public class InMemoryStoreService implements StoreService {

    /** maximum messages in the queue for a client */
    private int maxMessage;

    public InMemoryStoreService(int maxMessage) {
        super();
        this.maxMessage = maxMessage;
    }

    private Map<String /* client id */, Map<Long /* reception nanos date */, List<Message>>> receivedData = new ConcurrentHashMap<String, Map<Long, List<Message>>>();

    private Map<String /* client id */, AtomicInteger /* number of waiting data per client */> receivedDataCounter = new ConcurrentHashMap<String, AtomicInteger>();

    @Override
    public void enqueueReceivedData(String clientId, long receptionInNanoSec, List<Message> newData) {
        Map<Long, List<Message>> msgQueue;
        synchronized (this) {
            msgQueue = receivedData.get(clientId);
            if (msgQueue == null) {
                msgQueue = new ConcurrentHashMap<Long, List<Message>>();
                receivedData.put(clientId, msgQueue);
                // initialize data counter
                receivedDataCounter.put(clientId, new AtomicInteger(0));
            }
        }

        msgQueue.put(receptionInNanoSec, newData);

        // check if we have too much received data
        AtomicInteger counter = receivedDataCounter.get(receptionInNanoSec);
        int delta = counter.incrementAndGet() - maxMessage;
        if (delta > 0) {
            // we should purge some message
            Iterator<Entry<Long, List<Message>>> iterator = msgQueue.entrySet().iterator();
            int size;
            do {
                iterator.remove();
                size = counter.decrementAndGet();
            } while (size > maxMessage);
        }
    }

    @Override
    public Map<Long, List<Message>> lastReceivedData(String clientId) {

        return receivedData.get(clientId);
    }

    @Override
    public void start() {
        // nothing to do here
    }

    @Override
    public void stop() {
        // nothing to do here
    }
}
