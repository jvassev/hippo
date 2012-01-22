package hippo.server;

import hippo.client.ApiDefinition;

import java.util.HashMap;
import java.util.Map;


public abstract class ApiExporter {

    private final Map<String, Class<?>> typesToClasses;

    private ApiDefinition apiDefinition;

    public ApiExporter() {
        typesToClasses = new HashMap<String, Class<?>>();
    }

    public void defineClassMapping(String name, Class<?> clazz) {
        typesToClasses.put(name, clazz);
    }

    public void defineApi(ApiDefinition apiDefinition) {
        this.apiDefinition = apiDefinition;
    }


    public Map<String, Class<?>> getClassMapping() {
        return typesToClasses;
    }


    public ApiDefinition getApiDefinition() {
        return apiDefinition;
    }

    public abstract void start();

    public abstract void stop();

    public abstract ServerScriptingSession makeSession();
}