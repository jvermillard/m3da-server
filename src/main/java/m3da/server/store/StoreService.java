package m3da.server.store;

import java.util.List;
import java.util.Map;

/**
 * Service for storing and querying the received data by client identifier
 */
public interface StoreService {

    /**
     * Enqueue some received data from the client
     * 
     * @param clientId the client unique identifier ("ID" in the M3DA header)
     * @param receptionInNanoSec the reception time in nanoseconds (see {@link System#nanoTime()}
     * @param newData the received M3DA data
     */
    public void enqueueReceivedData(String clientId, long receptionInNanoSec, List<Message> newData);

    /**
     * Get the last received data for a gven client
     * 
     * @param clientId the client unique identifier
     * @return the last N received data from the given client
     */
    public Map<Long, List<Message>> lastReceivedData(String clientId);

    /**
     * start the service
     */
    public void start();

    /**
     * stop the service
     */
    public void stop();
}
