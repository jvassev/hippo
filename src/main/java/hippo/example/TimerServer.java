package hippo.example;

import hippo.client.ApiDefinition;
import hippo.client.PropertyDefinition;
import hippo.client.TypeDefinition;
import hippo.example.domain.Timer;
import hippo.server.ApiExporter;
import hippo.server.ServerScriptingSession;
import hippo.server.jms.JmsApiExporter;

public class TimerServer {

    public static void main(String[] args) throws Exception {
        // final ApiExporter service = new
        // AmqpApiExporter(CounterServer.makeConnection())
        // final ApiExporter service = new
        // RmiApiExporter(CounterServer.makeRegistry()) {
        final ApiExporter service = new JmsApiExporter(CounterServer.makeJmsConnection()) {

            @Override
            public ServerScriptingSession makeSession() {
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
        Thread.sleep(100000000);
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
