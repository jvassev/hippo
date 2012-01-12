package hippo.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface ScriptingSessionService extends Remote {
    String newSession() throws RemoteException;

    // remote
    public abstract Proxy newObject(String sessionId, String name, Object args[]) throws RemoteException;

    // remote
    public abstract Object invoke(String sessionId, Proxy self, String name, Object args[]) throws RemoteException;

    // remote, can be cached
    public abstract Set<String> getTypes(String sessionId) throws RemoteException;

    // remote, can be cached
    public void destroySession(String sessionId) throws RemoteException;
}
