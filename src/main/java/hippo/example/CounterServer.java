package hippo.example;

import hippo.client.ApiDefinition;
import hippo.client.PropertyDefinition;
import hippo.client.TypeDefinition;
import hippo.example.domain.Counter;
import hippo.server.DefaultScriptingSessionFactory;
import hippo.server.ServerScriptingSession;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class CounterServer {

    public static void main(String[] args) throws AccessException, RemoteException, AlreadyBoundException {
        String server = "localhost";
        if (args.length == 1) {
            server = args[0];
        }
        final Registry registry = LocateRegistry.getRegistry(server);


        final DefaultScriptingSessionFactory service = new DefaultScriptingSessionFactory(registry) {

            @Override
            protected ServerScriptingSession makeSession() {
                return new ServerScriptingSession() {

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
                    protected Object getVariableReal(String name) {
                        if (name.equals("env")) {
                            return new Counter(10);
                        } else {
                            return null;
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

                    @Override
                    protected Object getPropertyReal(Object instance, String name) {
                        Counter c = (Counter) instance;
                        if (name.equals("value")) {
                            return c.get();
                        } else {
                            return null;
                        }
                    }

                    @Override
                    protected Object putPropertyReal(Object instance, String name, Object value) {
                        throw new RuntimeException("no writable properties");
                    }

                    @Override
                    public void end() {
                        // callback to clean up
                    }
                };
            }
        };


        ApiDefinition apiDefinition = defineApi();

        service.defineApi(apiDefinition);
        service.defineClassMapping("Counter", Counter.class);
        service.bind();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    service.unbind();
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static ApiDefinition defineApi() {
        ApiDefinition apiDefinition = new ApiDefinition("Counter");

        TypeDefinition counter = new TypeDefinition("Counter");
        counter.defineMethod("get");
        counter.defineMethod("inc");
        counter.defineMethod("copy");
        counter.defineMethod("clone");
        PropertyDefinition prop = new PropertyDefinition("value");
        prop.setWritable(false);
        counter.defineProperty(prop);
        apiDefinition.defineType(counter);

        apiDefinition.defineVariable("env");
        return apiDefinition;
    }
}
