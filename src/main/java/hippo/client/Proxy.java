package hippo.client;

import java.io.Serializable;

public final class Proxy implements Serializable {

    private static final long serialVersionUID = -251229051998409186L;

    private String type;

    private String id;

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type + "#" + id;
    }
}
