package hippo.client;

import java.io.Serializable;

public class LocalProxy implements Proxy, Serializable {

    private static final long serialVersionUID = -251229051998409186L;
    private String type;
    private String id;

    private transient String sessionId;

    @Override
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
        return type + "#" + id + "#" + sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}