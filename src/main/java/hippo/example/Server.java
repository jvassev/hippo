package hippo.example;

import hippo.client.ScriptingSessionService;
import hippo.server.DefaultScriptingSessionService;
import hippo.server.LocalScriptSession;

import java.rmi.AccessException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class Server {
    public static void main(String[] args) throws AccessException, RemoteException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager() {
                @Override
                public void checkConnect(String host, int port) {

                }

                @Override
                public void checkConnect(String host, int port, Object context) {
                }
            });
        }
        ScriptingSessionService service = new DefaultScriptingSessionService() {
            @Override
            protected LocalScriptSession makeSession() {
                return new LocalScriptSession() {
                    {
                        registerType("Counter", Counter.class);
                    }

                    @Override
                    protected Object newInstaceReal(String name, Object[] args) {
                        if (name.equals("Counter")) {
                            return new Counter();
                        } else {
                            throw new IllegalArgumentException("cannot how to make " + name + " from "
                                    + Arrays.toString(args));
                        }
                    }

                    @Override
                    protected Object invokeReal(Object instance, String name, Object[] args) {
                        Counter c = (Counter) instance;
                        if (name.equals("inc")) {
                            c.inc();
                            return null;
                        } else if (name.equals("get")) {
                            return c.get();
                        } else if (name.equals("clone")) {
                            return c.clone();
                        } else if (name.equals("copy")) {
                            c.copy((Counter) args[0]);
                            return null;
                        } else {
                            throw new IllegalArgumentException("cannot how to call " + instance + "#" + name + " with "
                                    + Arrays.toString(args));
                        }
                    }
                };
            }
        };

        String name = "ScriptingSessionRegistry";
        ScriptingSessionService stub = (ScriptingSessionService) UnicastRemoteObject.exportObject(service, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(name, stub);
    }
}
