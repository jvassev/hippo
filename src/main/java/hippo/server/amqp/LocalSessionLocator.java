package hippo.server.amqp;

import hippo.client.ScriptingSession;
import hippo.client.amqp.ClientAmqpScriptingSession;
import hippo.server.SessionLocator;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;

import com.rabbitmq.client.Connection;


public class LocalSessionLocator implements SessionLocator {

    private HashMap<String, ScriptingSession> foreginSessionByApiName;

    private Connection connection;

    public LocalSessionLocator(Connection connection) {
        this.connection = connection;
        this.foreginSessionByApiName = new HashMap<String, ScriptingSession>();
    }

    @Override
    public ScriptingSession lookup(String sessionId) throws RemoteException {
        String apiName = sessionId.substring(0, sessionId.indexOf('/'));
        ScriptingSession session = foreginSessionByApiName.get(apiName);
        if (session == null) {
            try {
                session = new ClientAmqpScriptingSession(connection.createChannel(), apiName, sessionId);
                foreginSessionByApiName.put(sessionId, session);
            } catch (IOException e) {
                throw new RemoteException("", e);
            }
        }

        return session;
    }
}
