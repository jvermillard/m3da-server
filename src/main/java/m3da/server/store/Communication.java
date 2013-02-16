package m3da.server.store;

import java.io.Serializable;
import java.util.List;

public class Communication implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long timestamp;

    private final List<Message> data;

    public Communication(long timestamp, List<Message> data) {
        super();
        this.timestamp = timestamp;
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Message> getData() {
        return data;
    }

}
