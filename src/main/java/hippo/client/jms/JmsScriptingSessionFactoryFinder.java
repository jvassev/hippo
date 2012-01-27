package hippo.client.jms;

import hippo.client.ScriptingSessionFactory;
import hippo.client.ScriptingSessionFactoryFinder;

import javax.jms.Connection;


public class JmsScriptingSessionFactoryFinder implements ScriptingSessionFactoryFinder {


    private final Connection connection;

    public JmsScriptingSessionFactoryFinder(Connection connection) {
        this.connection = connection;
    }

    @Override
    public ScriptingSessionFactory findFactory(String apiName) {
        return new ClientJmsScriptingSessionFactory(connection, apiName);
    }
}
