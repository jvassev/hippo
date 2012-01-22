package hippo.client.amqp;

import hippo.client.ScriptingSessionFactory;
import hippo.client.ScriptingSessionFactoryFinder;

import com.rabbitmq.client.Connection;


public class AmqpScriptingSessionFactoryFinder implements ScriptingSessionFactoryFinder {


    private final Connection connection;

    public AmqpScriptingSessionFactoryFinder(Connection connection) {
        this.connection = connection;
    }

    @Override
    public ScriptingSessionFactory findFactory(String apiName) {
        return new ClientAmqpScriptingSessionFactory(connection, apiName);
    }
}
