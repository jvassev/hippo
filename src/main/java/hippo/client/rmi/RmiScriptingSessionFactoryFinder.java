package hippo.client.rmi;

import hippo.client.ScriptingSessionFactory;
import hippo.client.ScriptingSessionFactoryFinder;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;


public class RmiScriptingSessionFactoryFinder implements ScriptingSessionFactoryFinder {

    private final Registry registry;

    public RmiScriptingSessionFactoryFinder(Registry registry) {
        this.registry = registry;
    }


    @Override
    public ScriptingSessionFactory findFactory(String apiName) {
        try {
            return (ScriptingSessionFactory) registry.lookup(apiName);
        } catch (AccessException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }
}
