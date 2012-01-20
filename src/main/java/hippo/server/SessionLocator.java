package hippo.server;

import hippo.client.ScriptingSession;

import java.rmi.RemoteException;


public interface SessionLocator {

    ScriptingSession lookup(String sessionId) throws RemoteException;
}
