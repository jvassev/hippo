package hippo.server;

import hippo.client.Proxy;
import hippo.client.ScriptingSessionService;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class DefaultScriptingSessionService implements ScriptingSessionService {

    private ConcurrentMap<String, LocalScriptSession> sessions;

    public DefaultScriptingSessionService() {
        sessions = new ConcurrentHashMap<String, LocalScriptSession>();
    }

    @Override
    public synchronized String newSession() {
        String id = UUID.randomUUID().toString();
        LocalScriptSession session = makeSession();
        sessions.put(id, session);
        return id;
    }

    @Override
    public Proxy newObject(String sessionId, String name, Object[] args) {
        return findSession(sessionId).newObject(name, args);
    }

    private LocalScriptSession findSession(String sessionId) {
        LocalScriptSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("cannot find session for " + sessionId);
        } else {
            return session;
        }
    }

    @Override
    public Object invoke(String sessionId, Proxy self, String name, Object[] args) {
        return findSession(sessionId).invoke(self, name, args);
    }

    @Override
    public Set<String> getTypes(String sessionId) {
        return findSession(sessionId).getTypes();
    }

    @Override
    public synchronized void destroySession(String sessionId) {
        sessions.remove(sessionId);
    }

    protected abstract LocalScriptSession makeSession();
}
