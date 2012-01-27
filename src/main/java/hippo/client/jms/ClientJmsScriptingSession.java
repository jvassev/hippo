package hippo.client.jms;

import hippo.client.ApiDefinition;
import hippo.client.Proxy;
import hippo.client.ScriptingSession;
import hippo.client.remoting.Request;
import hippo.client.remoting.Response;

import java.util.UUID;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;


public class ClientJmsScriptingSession implements ScriptingSession {

    private final Session session;

    private final String apiName;

    private ApiDefinition apiDefinition;

    private final String sessionId;

    private Queue requestQueue;

    private Queue replyQueue;

    private MessageProducer producer;

    public ClientJmsScriptingSession(Session session, String apiName, String sessionId) throws JMSException {
        this.session = session;
        this.apiName = apiName;
        this.sessionId = sessionId;

        requestQueue = session.createQueue(apiName + ".request");
        replyQueue = session.createQueue(apiName + ".reply");

        producer = session.createProducer(requestQueue);

        Request req = makeRequest(Request.openSession);
        doRpcAndHandleError(req);
    }

    @Override
    public void end() {
        Request req = makeRequest(Request.closeSession);

        doRpcAndHandleError(req);
        try {
            producer.close();
            session.close();
        } catch (JMSException e) {
            // logger.error("", e);
            e.printStackTrace();
        }
    }

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public Proxy newObject(String type, Object[] args) {
        Request req = makeRequest(Request.newObject);
        req.setTypeName(type);
        req.setArgs(args);

        return (Proxy) doRpcAndHandleError(req);
    }

    private Object resultOrThrow(Response response) {
        if (response.getException() != null) {
            throw new RuntimeException("error occured in API server", response.getException());
        } else {
            return response.getResult();
        }
    }

    private Request makeRequest(int type) {
        Request request = new Request();
        request.setSessionId(sessionId);
        request.setRequestType(type);
        return request;
    }

    @Override
    public Object invokeMethod(Proxy self, String method, Object[] args) {
        Request req = makeRequest(Request.invokeMethod);
        req.setSelf(self);
        req.setMethod(method);
        req.setArgs(args);

        return doRpcAndHandleError(req);
    }

    @Override
    public ApiDefinition getApiDefinition() {
        if (apiDefinition == null) {
            Request req = makeRequest(Request.getApiDefinition);
            apiDefinition = (ApiDefinition) doRpcAndHandleError(req);
        }
        return apiDefinition;
    }


    private Response rpc(Request request) throws JMSException {
        String correlationId = UUID.randomUUID().toString();

        ObjectMessage message = session.createObjectMessage();
        message.setObject(request);
        message.setJMSCorrelationID(correlationId);
        message.setJMSReplyTo(replyQueue);


        producer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_DELIVERY_MODE, 60 * 1000);
        MessageConsumer consumer = session.createConsumer(replyQueue, "JMSCorrelationID = '" + correlationId + "'");
        ObjectMessage result = (ObjectMessage) consumer.receive();
        consumer.close();
        return (Response) result.getObject();
    }

    @Override
    public Object getProperty(Proxy self, String property) {
        Request req = makeRequest(Request.getProperty);
        req.setPropertyName(property);
        req.setSelf(self);

        return doRpcAndHandleError(req);
    }

    @Override
    public void putProperty(Proxy self, String property, Object value) {
        Request req = makeRequest(Request.setProperty);
        req.setPropertyName(property);
        req.setSelf(self);
        req.setValue(value);

        doRpcAndHandleError(req);
    }

    @Override
    public Object getVariable(String name) {
        Request req = makeRequest(Request.getVariable);
        req.setVariableName(name);

        return doRpcAndHandleError(req);
    }

    private Object doRpcAndHandleError(Request r) {
        Response response;
        try {
            response = rpc(r);
        } catch (JMSException e) {
            return handleRpcException(e);
        }

        return resultOrThrow(response);
    }

    private Object handleRpcException(Exception e) {
        throw new RuntimeException("error in RPC", e);
    }
}
