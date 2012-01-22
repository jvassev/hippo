package hippo.example;

import hippo.client.ApiDefinition;
import hippo.client.PropertyDefinition;
import hippo.client.TypeDefinition;
import hippo.example.domain.Counter;
import hippo.server.ApiExporter;
import hippo.server.ServerScriptingSession;
import hippo.server.amqp.AmqpApiExporter;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.util.Arrays;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class CounterServer {

    public static void main(String[] args) throws AlreadyBoundException, IOException {
        // String server = "localhost";
        // if (args.length == 1) {
        // server = args[0];
        // }
        // final Registry registry = LocateRegistry.getRegistry(server);

        final ApiExporter service = new AmqpApiExporter(makeConnection()) {

            // final AbstractScriptingSessionFactory service = new
            // RmiScriptingSessionFactory(registry) {

            @Override
            public ServerScriptingSession makeSession() {
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
        service.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                service.stop();
            }
        });
    }

    public static Connection makeConnection() throws IOException {
        ConnectionFactory cf = new ConnectionFactory();
        cf.setHost("localhost");
        cf.setPort(5672);
        cf.setUsername("guest");
        cf.setPassword("guest");
        cf.setVirtualHost("/");
        return cf.newConnection();
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
