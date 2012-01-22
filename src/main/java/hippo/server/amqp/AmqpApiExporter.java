package hippo.server.amqp;

import hippo.client.amqp.Request;
import hippo.client.amqp.Response;
import hippo.client.amqp.Serializer;
import hippo.server.ApiExporter;
import hippo.server.ServerScriptingSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;


public abstract class AmqpApiExporter extends ApiExporter {

    private final Connection connection;

    private final ConcurrentMap<String, ServerScriptingSession> sessions;

    private RpcServer server;

    public AmqpApiExporter(Connection connection) throws IOException {
        this.connection = connection;
        this.sessions = new ConcurrentHashMap<String, ServerScriptingSession>();
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
                    Response res = rpc((Request) Serializer.deserialize(requestBody));
                    return Serializer.serialize(res);
                }
            };

            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        server.terminateMainloop();
    }
}
