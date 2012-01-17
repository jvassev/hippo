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

public abstract class DefaultScriptingSessionFactory implements ScriptingSessionFactory, SessionLocator {

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
        session.setLocator(this);
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
        return apiDefinition.getName() + "/" + UUID.randomUUID().toString();
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

    @Override
    public ScriptingSession lookup(String sessionId) {
        try {
            return (ScriptingSession) registry.lookup(sessionId);
        } catch (AccessException e) {
            e.printStackTrace();
            return null;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        } catch (NotBoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected abstract ServerScriptingSession makeSession();
}
