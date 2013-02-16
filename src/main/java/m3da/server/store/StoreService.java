package m3da.server.store;

import java.util.List;

public interface StoreService {

    public void enqueueData(String systemId, long reception, List<Message> newData);

    public List<Message> popData(String systemId);
}
