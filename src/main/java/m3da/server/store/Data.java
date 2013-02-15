package m3da.server.store;

import java.io.Serializable;
import java.util.Map;

public class Data implements Serializable {

    private static final long serialVersionUID = 1L;

    public Data(String path, Map<Object, Object> data) {
        super();
        this.path = path;
        this.data = data;
    }

    private String path;

    private Map<Object, Object> data;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<Object, Object> getData() {
        return data;
    }

    public void setData(Map<Object, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Data [path=" + path + ", data=" + data + "]";
    }

}
