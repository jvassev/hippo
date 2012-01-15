package hippo.server;

import hippo.client.ApiDefinition;
import hippo.client.ScriptingSession;
import hippo.client.ScriptingSessionFactory;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class DefaultScriptingSessionFactory implements ScriptingSessionFactory {

    private final Registry registry;

    private final Map<String, Class<?>> typesToClasses;

    private ApiDefinition apiDefinition;

    public DefaultScriptingSessionFactory(Registry registry) {
        this.registry = registry;
        typesToClasses = new HashMap<String, Class<?>>();
    }

    @Override
    public ScriptingSession openSession() throws RemoteException {
        ServerScriptingSession session = makeSession();
        String id = generateSessionId();
        session.setId(id);

        session.defineApi(apiDefinition);

        session.defineClassMapping(typesToClasses);

        session.start();
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


    public void bind() throws AccessException, RemoteException, AlreadyBoundException {
        ScriptingSessionFactory stub = (ScriptingSessionFactory) UnicastRemoteObject.exportObject(this, 0);
        registry.bind(apiDefinition.getName(), stub);
    }

    public void unbind() throws AccessException, RemoteException, NotBoundException {
        registry.unbind(apiDefinition.getName());
    }

    public void defineClassMapping(String name, Class<?> clazz) {
        typesToClasses.put(name, clazz);
    }

    public void defineApi(ApiDefinition apiDefinition) {
        this.apiDefinition = apiDefinition;
    }

    protected abstract ServerScriptingSession makeSession();
}
