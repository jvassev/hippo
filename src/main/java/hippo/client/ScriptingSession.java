package hippo.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ScriptingSession extends Remote {

    void end() throws RemoteException;

    String getId() throws RemoteException;

    Proxy newObject(String type, Object args[]) throws RemoteException;

    Object invokeMethod(Proxy self, String method, Object args[]) throws RemoteException;

    ApiDefinition getApiDefinition() throws RemoteException;

    Object getProperty(Proxy self, String property) throws RemoteException;

    void putProperty(Proxy self, String property, Object value) throws RemoteException;

    Object getVariable(String name) throws RemoteException;
}
