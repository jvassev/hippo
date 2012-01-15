package hippo.server;

import hippo.client.ScriptingSession;
import hippo.client.ScriptingSessionFactory;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public abstract class DefaultScriptingSessionService implements ScriptingSessionFactory {

    private final Registry registry;

    public DefaultScriptingSessionService(Registry registry) {
        this.registry = registry;
    }

    @Override
    public ScriptingSession openSession() throws RemoteException {
        ServerScriptingSession session = makeSession();
        String id = generateSessionId();
        session.setId(id);
        ScriptingSession stub = (ScriptingSession) UnicastRemoteObject.exportObject(session, 0);
        try {
            registry.bind(id, stub);
        } catch (AlreadyBoundException e) {
            throw new RemoteException(e.getMessage());
        }

        return session;
    }

    private String generateSessionId() {
        return "Session-" + UUID.randomUUID().toString();
    }

    protected abstract ServerScriptingSession makeSession();
}
