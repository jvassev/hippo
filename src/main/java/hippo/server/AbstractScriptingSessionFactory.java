package hippo.server;

import hippo.client.ApiDefinition;
import hippo.client.ScriptingSession;
import hippo.client.ScriptingSessionFactory;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractScriptingSessionFactory implements ScriptingSessionFactory, SessionLocator {

    private final Map<String, Class<?>> typesToClasses;

    private ApiDefinition apiDefinition;

    public AbstractScriptingSessionFactory() {
        typesToClasses = new HashMap<String, Class<?>>();
    }

    public void start() {

    }

    public void stop() {

    }

    @Override
    public ScriptingSession openSession() throws RemoteException {
        ServerScriptingSession session = makeSession();
        String id = generateSessionId();
        session.setId(id);

        session.defineApi(apiDefinition);

        session.defineClassMapping(typesToClasses);
        session.setLocator(this);
        session.start();
        registerSession(session);
        return session;
    }


    private String generateSessionId() {
        return apiDefinition.getName() + "/" + UUID.randomUUID().toString();
    }

    public void defineClassMapping(String name, Class<?> clazz) {
        typesToClasses.put(name, clazz);
    }

    public void defineApi(ApiDefinition apiDefinition) {
        this.apiDefinition = apiDefinition;
    }

    protected abstract void registerSession(ServerScriptingSession session);

    protected abstract ServerScriptingSession makeSession();


    public ApiDefinition getApiDefinition() {
        return apiDefinition;
    }
}
