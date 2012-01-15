package hippo.example;

import hippo.client.ApiDefinition;
import hippo.client.TypeDefinition;
import hippo.server.DefaultScriptingSessionFactory;
import hippo.server.ServerScriptingSession;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TimerServer {

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

                    private Timer timer;

                    @Override
                    public void start() {
                        timer = new Timer();
                    }

                    @Override
                    protected Object newInstaceReal(String name, Object[] args) {
                        throw new IllegalArgumentException("sdf");
                    }

                    @Override
                    protected Object getVariableReal(String name) {
                        if (name.equals("timer")) {
                            return timer;
                        } else {
                            return null;
                        }
                    }

                    @Override
                    protected Object invokeReal(Object instance, String name, Object[] args) {
                        throw new IllegalArgumentException("sdf");
                    }

                    @Override
                    protected Object getPropertyReal(Object instance, String name) {
                        Timer c = (Timer) instance;
                        if (name.equals("elapsed")) {
                            return c.getElapsed();
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
        service.defineClassMapping("Timer", Timer.class);
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
        ApiDefinition apiDefinition = new ApiDefinition("Timer");

        TypeDefinition counter = new TypeDefinition("Timer");
        counter.defineProperty("elapsed");
        apiDefinition.defineType(counter);

        apiDefinition.defineVariable("timer");
        return apiDefinition;
    }
}
