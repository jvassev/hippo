package hippo.client;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;


public class SessionCache {

    private Map<String, DefaultScriptingSession> sessions;

    public SessionCache() {
        this.sessions = new HashMap<String, DefaultScriptingSession>();
    }

    public void addSession(DefaultScriptingSession session) {
        try {
            sessions.put(session.getId(), session);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public DefaultScriptingSession findSession(String id) {
        return sessions.get(id);
    }
}
