package hippo.client.amqp;

import hippo.client.ScriptingSession;
import hippo.client.ScriptingSessionFactory;

import java.io.IOException;
import java.util.UUID;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;


class ClientAmqpScriptingSessionFactory implements ScriptingSessionFactory {

    private final String apiName;

    private final Connection connection;

    public ClientAmqpScriptingSessionFactory(Connection connection, String apiName) {
        this.connection = connection;
        this.apiName = apiName;
    }

    @Override
    public ScriptingSession openSession() {
        try {
            String sessionId = generateSessionId();
            Channel channel = connection.createChannel();
            return new ClientAmqpScriptingSession(channel, apiName, sessionId);
        } catch (IOException e) {
            throw new RuntimeException("error opening session", e);
        }
    }

    private String generateSessionId() {
        return apiName + "/" + UUID.randomUUID().toString();
    }
}
