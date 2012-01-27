package hippo.server.jms;

import hippo.client.ScriptingSession;
import hippo.client.jms.ClientJmsScriptingSession;
import hippo.server.SessionLocator;

import java.rmi.RemoteException;
import java.util.HashMap;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;


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
                session = new ClientJmsScriptingSession(createSession(), apiName, sessionId);
                foreginSessionByApiName.put(sessionId, session);
            } catch (JMSException e) {
                throw new RemoteException("", e);
            }
        }

        return session;
    }

    private Session createSession() throws JMSException {
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }
}
