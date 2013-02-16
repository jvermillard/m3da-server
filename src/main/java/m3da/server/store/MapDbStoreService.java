package m3da.server.store;

import java.io.File;
import java.util.List;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class MapDbStoreService implements StoreService {

    private BTreeMap<String, List<Message>> data;

    private DB db;

    public void start() {
        db = DBMaker.newFileDB(new File("storedb.mapdb")).closeOnJvmShutdown().make();

        data = db.getTreeMap("systemdata");
    }

    public void stop() {
        db.close();
    }

    @Override
    public void enqueueData(String systemId, long reception, List<Message> newData) {
        data.put(systemId + "#" + reception, newData);

    }

    @Override
    public List<Message> popData(String systemId) {
        String key = data.higherKey(systemId + "#");
        if (key != null && key.startsWith(systemId + "#")) {
            return data.remove(key);
        } else {
            return null;
        }
    }
}
