package hippo.client.jms;

import hippo.client.ScriptingSession;
import hippo.client.ScriptingSessionFactory;

import java.util.UUID;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;


class ClientJmsScriptingSessionFactory implements ScriptingSessionFactory {

    private final String apiName;

    private final Connection connection;

    public ClientJmsScriptingSessionFactory(Connection connection, String apiName) {
        this.connection = connection;
        this.apiName = apiName;
    }

    @Override
    public ScriptingSession openSession() {
        try {
            String sessionId = generateSessionId();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            return new ClientJmsScriptingSession(session, apiName, sessionId);
        } catch (JMSException e) {
            throw new RuntimeException("error opening session", e);
        }
    }

    private String generateSessionId() {
        return apiName + "/" + UUID.randomUUID().toString();
    }
}
