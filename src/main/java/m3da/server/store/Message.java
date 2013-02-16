package m3da.server.store;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /** path */
    private final String path;

    /** associated data */
    private final Map<String, List<?>> data;

    public Message(String path, Map<String, List<?>> data) {
        super();
        this.path = path;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<?>> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Data [path=" + path + ", data=" + data + "]";
    }

}
