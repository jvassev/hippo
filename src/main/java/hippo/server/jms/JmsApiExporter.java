package hippo.server.jms;

import hippo.client.remoting.Request;
import hippo.client.remoting.Response;
import hippo.server.ApiExporter;
import hippo.server.ServerScriptingSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;


public abstract class JmsApiExporter extends ApiExporter {

    private final Connection connection;

    private final ConcurrentMap<String, ServerScriptingSession> sessions;

    private Queue requestQueue;

    private Queue replyQueue;

    private MessageProducer producer;

    private Session session;


    public JmsApiExporter(Connection connection) throws IOException {
        this.connection = connection;
        this.sessions = new ConcurrentHashMap<String, ServerScriptingSession>();
    }

    @Override
    public void start() throws IOException {
        String apiName = getApiDefinition().getName();
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            requestQueue = session.createQueue(apiName + ".request");
            replyQueue = session.createQueue(apiName + ".reply");
            producer = session.createProducer(replyQueue);
            MessageConsumer consumer = session.createConsumer(requestQueue);
            consumer.setMessageListener(new MessageListener() {

                @Override
                public void onMessage(Message m) {
                    try {
                        handleMessage((ObjectMessage) m);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
            connection.start();
        } catch (JMSException e) {
            throw new IOException(e);
        }
    }

    protected void handleMessage(ObjectMessage message) throws JMSException {
        Request request = (Request) message.getObject();
        Response response = rpc(request);
        ObjectMessage result = session.createObjectMessage();
        result.setObject(response);
        result.setJMSCorrelationID(message.getJMSCorrelationID());
        producer.send(result, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_DELIVERY_MODE, 60 * 1000);
    }

    private Response rpc(Request request) {
        ServerScriptingSession session = findLocalSession(request);
        Response response = new Response();

        if (request.getRequestType() == Request.openSession) {
            if (session == null) {
                try {
                    session = makeSession();
                    session.setId(request.getSessionId());
                    session.defineApi(getApiDefinition());
                    session.defineClassMapping(getClassMapping());
                    session.setLocator(new LocalSessionLocator(connection));
                    session.start();
                    registerSession(session);
                } catch (Throwable e) {
                    response.setException(e);
                }
            }
        } else {
            synchronized (session) {
                try {
                    if (request.getRequestType() == Request.getApiDefinition) {
                        response.setResult(getApiDefinition());
                    } else if (request.getRequestType() == Request.getVariable) {
                        response.setResult(session.getVariable(request.getVariableName()));
                    } else if (request.getRequestType() == Request.newObject) {
                        response.setResult(session.newObject(request.getTypeName(), request.getArgs()));
                    } else if (request.getRequestType() == Request.invokeMethod) {
                        response.setResult(session.invokeMethod(request.getSelf(), request.getMethod(),
                                request.getArgs()));
                    } else if (request.getRequestType() == Request.setProperty) {
                        session.putProperty(request.getSelf(), request.getPropertyName(), request.getValue());
                    } else if (request.getRequestType() == Request.getProperty) {
                        response.setResult(session.getProperty(request.getSelf(), request.getPropertyName()));
                    } else if (request.getRequestType() == Request.closeSession) {
                        session.end();
                    } else {
                        IllegalArgumentException e = new IllegalArgumentException("unknown request type"
                                + request.getRequestType());
                        response.setException(e);
                    }
                } catch (Exception e) {
                    response.setException(e);
                }
            }
        }

        return response;
    }

    private ServerScriptingSession findLocalSession(Request request) {
        return sessions.get(request.getSessionId());
    }

    private void registerSession(ServerScriptingSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    public void stop() {
        try {
            connection.close();
        } catch (JMSException e) {
            // ignore
        }
    }
}
