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
        ServerScriptSession session = makeSession();
        String name = generateObjectName();

        ScriptingSession stub = (ScriptingSession) UnicastRemoteObject.exportObject(session, 0);
        try {
            registry.bind(name, stub);
        } catch (AlreadyBoundException e) {
            throw new RemoteException(e.getMessage());
        }

        return session;
    }

    private String generateObjectName() {
        return "Session-" + UUID.randomUUID().toString();
    }

    protected abstract ServerScriptSession makeSession();
}
