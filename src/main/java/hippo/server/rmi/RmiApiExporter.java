package hippo.server.rmi;

import hippo.client.ScriptingSession;
import hippo.client.ScriptingSessionFactory;
import hippo.server.ApiExporter;
import hippo.server.ServerScriptingSession;
import hippo.server.SessionLocator;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;


public abstract class RmiApiExporter extends ApiExporter implements ScriptingSessionFactory, SessionLocator {


    private final Registry registry;

    public RmiApiExporter(Registry registry) {
        this.registry = registry;
    }

    @Override
    public ScriptingSession lookup(String sessionId) throws RemoteException {
        try {
            return (ScriptingSession) registry.lookup(sessionId);
        } catch (NotBoundException e) {
            return null;
        }
    }

    private String generateSessionId() {
        return getApiDefinition().getName() + "/" + UUID.randomUUID().toString();
    }


    @Override
    public ScriptingSession openSession() throws RemoteException {
        ServerScriptingSession session = makeSession();
        String id = generateSessionId();
        session.setId(id);

        session.defineApi(getApiDefinition());

        session.defineClassMapping(getClassMapping());
        session.setLocator(this);
        session.start();
        registerSession(session);
        return session;
    }

    protected void registerSession(ServerScriptingSession session) {
        try {
            Remote stub = UnicastRemoteObject.exportObject(session, 0);
            registry.bind(session.getId(), stub);
        } catch (AccessException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() {
        try {
            ScriptingSessionFactory stub = (ScriptingSessionFactory) UnicastRemoteObject.exportObject(this, 0);
            registry.bind(getApiDefinition().getName(), stub);
        } catch (AccessException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void stop() {
        try {
            registry.unbind(getApiDefinition().getName());
        } catch (AccessException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }
}
