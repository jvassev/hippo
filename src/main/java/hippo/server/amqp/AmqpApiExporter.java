package hippo.server.amqp;

import hippo.client.ScriptingSession;
import hippo.client.amqp.ClientAmqpScriptingSession;
import hippo.client.amqp.Request;
import hippo.client.amqp.Response;
import hippo.client.amqp.Serializer;
import hippo.server.ApiExporter;
import hippo.server.ServerScriptingSession;
import hippo.server.SessionLocator;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.RpcServer;


public abstract class AmqpApiExporter extends ApiExporter implements SessionLocator {

    private final Connection connection;

    private final Map<String, ServerScriptingSession> sessions;

    private RpcServer server;

    private Map<String, ServerScriptingSession> foreginSessionByApiName;

    public AmqpApiExporter(Connection connection) throws IOException {
        this.connection = connection;
        sessions = new HashMap<String, ServerScriptingSession>();
        foreginSessionByApiName = new HashMap<String, ServerScriptingSession>();
    }

    @Override
    public void start() {
        try {
            String exchange = getApiDefinition().getName();
            String queue = getApiDefinition().getName() + "-request";
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(exchange, "fanout");
            channel.queueDeclare(queue, false, false, true, null);
            channel.queueBind(queue, exchange, "");

            server = new RpcServer(channel, queue) {

                @Override
                public byte[] handleCall(byte[] requestBody, BasicProperties replyProperties) {
                    Response res = AmqpApiExporter.this.rpc((Request) Serializer.deserialize(requestBody));
                    return Serializer.serialize(res);
                }
            };
            new Thread() {

                @Override
                public void run() {
                    try {
                        server.mainloop();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Response rpc(Request request) {
        Response response = new Response();
        if (request.getRequestType() == Request.openSession) {
            ServerScriptingSession session = findLocalSession(request);
            if (session == null) {
                session = makeSession();
                session.setId(request.getSessionId());
                session.defineApi(getApiDefinition());
                session.defineClassMapping(getClassMapping());
                session.setLocator(this);
                session.start();
                registerSession(session);
            }
        } else if (request.getRequestType() == Request.getApiDefinition) {
            response.setResult(getApiDefinition());
        } else if (request.getRequestType() == Request.getVariable) {
            ServerScriptingSession session = findLocalSession(request);
            response.setResult((Serializable) session.getVariable(request.getVariableName()));
        } else if (request.getRequestType() == Request.newObject) {
            ServerScriptingSession session = findLocalSession(request);
            response.setResult(session.newObject(request.getTypeName(), request.getArgs()));
        } else if (request.getRequestType() == Request.invokeMethod) {
            ServerScriptingSession session = findLocalSession(request);
            response.setResult((Serializable) session.invokeMethod(request.getSelf(), request.getMethod(),
                    request.getArgs()));
        } else if (request.getRequestType() == Request.setProperty) {
            ServerScriptingSession session = findLocalSession(request);
            session.putProperty(request.getSelf(), request.getPropertyName(), request.getValue());
        } else if (request.getRequestType() == Request.getProperty) {
            ServerScriptingSession session = findLocalSession(request);
            response.setResult((Serializable) session.getProperty(request.getSelf(), request.getPropertyName()));
        } else if (request.getRequestType() == Request.closeSession) {
            ServerScriptingSession session = findLocalSession(request);
            session.end();
        } else {
            IllegalArgumentException e = new IllegalArgumentException("unknown request type" + request.getTypeName());
            response.setException(e);
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
        server.terminateMainloop();
    }

    @Override
    public ScriptingSession lookup(String sessionId) throws RemoteException {
        String apiName = sessionId.substring(0, sessionId.indexOf('/'));
        try {
            return new ClientAmqpScriptingSession(connection.createChannel(), apiName, sessionId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
