package hippo.example;

import hippo.client.ApiDefinition;
import hippo.client.TypeDefinition;
import hippo.example.domain.Describer;
import hippo.server.ApiExporter;
import hippo.server.ServerScriptingSession;
import hippo.server.jms.JmsApiExporter;

public class DescriberServer {

    public static void main(String[] args) throws Exception {
        // final ApiExporter service = new
        // AmqpApiExporter(CounterServer.makeConnection())
        // final ApiExporter service = new
        // RmiApiExporter(CounterServer.makeRegistry()) {
        final ApiExporter service = new JmsApiExporter(CounterServer.makeJmsConnection()) {

            @Override
            public ServerScriptingSession makeSession() {
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
        service.start();
        Thread.sleep(100000000);
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
