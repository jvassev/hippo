package hippo.client;

import java.rmi.RemoteException;
import java.util.Set;

public interface ScriptingSession {

    void start() throws RemoteException;

    // close remote session
    void end();

    // local
    Object wrap(Object res);

    // local
    Object[] proxyArgs(Object[] args);

    // remote
    Proxy newObject(String name, Object args[]) throws RemoteException;

    // remote
    Object invoke(Proxy self, String name, Object args[]) throws RemoteException;

    // remote, can be cached
    Set<String> getTypes() throws RemoteException;
}
