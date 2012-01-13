package hippo.client;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ScriptingSessionFactory extends Remote {
    ScriptingSession openSession() throws RemoteException;
}
