package hippo.example;

import hippo.client.ApiDefinition;
import hippo.client.PropertyDefinition;
import hippo.client.TypeDefinition;
import hippo.example.domain.Timer;
import hippo.server.AbstractScriptingSessionFactory;
import hippo.server.ServerScriptingSession;
import hippo.server.rmi.RmiScriptingSessionFactory;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
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


        final AbstractScriptingSessionFactory service = new RmiScriptingSessionFactory(registry) {

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
                };
            }
        };


        ApiDefinition apiDefinition = defineApi();

        service.defineApi(apiDefinition);
        service.defineClassMapping("Timer", Timer.class);
        service.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                service.stop();
            }
        });
    }

    private static ApiDefinition defineApi() {
        ApiDefinition apiDefinition = new ApiDefinition("Timer");

        TypeDefinition timer = new TypeDefinition("Timer");
        PropertyDefinition prop = new PropertyDefinition("elapsed");
        prop.setWritable(false);
        timer.defineProperty(prop);
        apiDefinition.defineType(timer);

        apiDefinition.defineVariable("timer");
        return apiDefinition;
    }
}
