package hippo.example;

import hippo.client.ApiDefinition;
import hippo.client.TypeDefinition;
import hippo.example.domain.Describer;
import hippo.server.DefaultScriptingSessionFactory;
import hippo.server.ServerScriptingSession;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DescriberServer {

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
                        throw new IllegalArgumentException("sdf");
                    }

                    @Override
                    protected Object getVariableReal(String name) {
                        if (name.equals("describer")) {
                            return new Describer();
                        } else {
                            return null;
                        }
                    }

                    @Override
                    protected Object invokeReal(Object instance, String name, Object[] args) {
                        Describer d = (Describer) instance;
                        if (name.equals("describe")) {
                            return d.describe(args[0]);
                        } else if (name.equals("touch")) {
                            return d.touch(args[0]);
                        } else {
                            throw new IllegalArgumentException("unknown method");
                        }
                    }

                    @Override
                    protected Object getPropertyReal(Object instance, String name) {
                        throw new IllegalArgumentException("unknown property");
                    }

                    @Override
                    protected Object putPropertyReal(Object instance, String name, Object value) {
                        throw new IllegalArgumentException("unknown property");
                    }
                };
            }
        };


        ApiDefinition apiDefinition = defineApi();

        service.defineApi(apiDefinition);
        service.defineClassMapping("Describer", Describer.class);
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
        ApiDefinition apiDefinition = new ApiDefinition("Describer");

        TypeDefinition describer = new TypeDefinition("Describer");
        apiDefinition.defineType(describer);
        describer.defineMethod("describe");
        describer.defineMethod("touch");

        apiDefinition.defineVariable("describer");
        return apiDefinition;
    }
}
