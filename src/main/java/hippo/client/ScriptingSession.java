package hippo.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ScriptingSession extends Remote {

    void start() throws RemoteException;

    void end() throws RemoteException;

    String getId() throws RemoteException;

    Proxy newObject(String name, Object args[]) throws RemoteException;

    Object invokeMethod(Proxy self, String name, Object args[]) throws RemoteException;

    ApiDefinition getApiDefinition() throws RemoteException;

    Object getProperty(Proxy self, String property) throws RemoteException;

    void putProperty(Proxy self, String property, Object value) throws RemoteException;

    Object getVariable(String name) throws RemoteException;
}
