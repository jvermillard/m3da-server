package m3da.server.store;

import java.util.List;

public interface StoreService {

    public void enqueueData(String systemId, long reception, List<Data> newData);

    public List<Data> popData(String systemId);
}
