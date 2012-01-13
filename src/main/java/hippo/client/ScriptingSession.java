package hippo.client;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface ScriptingSession extends Remote {

    void start() throws RemoteException;

    void end() throws RemoteException;

    Proxy newObject(String name, Object args[]) throws RemoteException;

    Object invoke(Proxy self, String name, Object args[]) throws RemoteException;

    Set<String> getTypes() throws RemoteException;
}
