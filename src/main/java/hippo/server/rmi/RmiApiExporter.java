package hippo.server.rmi;

import hippo.client.ScriptingSession;
import hippo.client.ScriptingSessionFactory;
import hippo.server.ApiExporter;
import hippo.server.ServerScriptingSession;
import hippo.server.SessionLocator;

import java.io.IOException;
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
        ServerScriptingSession session;
        try {
            session = makeSession();
        } catch (IOException e) {
            throw new RemoteException("", e);
        }

        String id = generateSessionId();
        session.setId(id);

        session.defineApi(getApiDefinition());

        session.defineClassMapping(getClassMapping());
        session.setLocator(this);
        session.start();
        try {
            registerSession(session);
        } catch (AlreadyBoundException e) {
            throw new RemoteException("", e);
        }
        return session;
    }

    protected void registerSession(ServerScriptingSession session) throws RemoteException, AlreadyBoundException {
        Remote stub = UnicastRemoteObject.exportObject(session, 0);
        registry.bind(session.getId(), stub);
    }

    @Override
    public void start() throws IOException {
        try {
            ScriptingSessionFactory stub = (ScriptingSessionFactory) UnicastRemoteObject.exportObject(this, 0);
            registry.bind(getApiDefinition().getName(), stub);
        } catch (AccessException e) {
            throw new IOException(e);
        } catch (RemoteException e) {
            throw new IOException(e);
        } catch (AlreadyBoundException e) {
            throw new IOException(e);
        }
    }


    @Override
    public void stop() throws IOException {
        try {
            registry.unbind(getApiDefinition().getName());
        } catch (AccessException e) {
            throw new IOException(e);
        } catch (RemoteException e) {
            throw new IOException(e);
        } catch (NotBoundException e) {
            throw new IOException(e);
        }
    }
}
