package hippo.server.rmi;

import hippo.client.ScriptingSession;
import hippo.client.ScriptingSessionFactory;
import hippo.server.AbstractScriptingSessionFactory;
import hippo.server.ServerScriptingSession;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public abstract class RmiScriptingSessionFactory extends AbstractScriptingSessionFactory {


    private final Registry registry;

    public RmiScriptingSessionFactory(Registry registry) {
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

    @Override
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
