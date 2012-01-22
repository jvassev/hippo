package hippo.client.amqp;

import hippo.client.ApiDefinition;
import hippo.client.Proxy;
import hippo.client.ScriptingSession;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClient;
import com.rabbitmq.client.ShutdownSignalException;


public class ClientAmqpScriptingSession implements ScriptingSession {

    private final Channel channel;

    private final String apiName;

    private RpcClient rpc;

    private ApiDefinition apiDefinition;

    private final String sessionId;

    public ClientAmqpScriptingSession(Channel channel, String apiName, String sessionId) {
        this.channel = channel;
        this.apiName = apiName;
        this.sessionId = sessionId;
        try {
            rpc = new RpcClient(channel, apiName, "");
            Request request = makeRequest();
            request.setRequestType(Request.openSession);
            rpc(request);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ShutdownSignalException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void end() {
        try {
            Request r = makeRequest();
            r.setRequestType(Request.closeSession);
            rpc(r);
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ShutdownSignalException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public Proxy newObject(String type, Object[] args) {
        Request r = makeRequest();
        r.setRequestType(Request.newObject);
        r.setTypeName(type);
        r.setArgs(args);
        try {
            return (Proxy) rpc(r).getResult();
        } catch (ShutdownSignalException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    private Request makeRequest() {
        Request request = new Request();
        request.setSessionId(sessionId);
        return request;
    }

    @Override
    public Object invokeMethod(Proxy self, String method, Object[] args) {
        Request r = makeRequest();
        r.setRequestType(Request.invokeMethod);
        r.setSelf(self);
        r.setMethod(method);
        r.setArgs(args);
        try {
            return rpc(r).getResult();
        } catch (ShutdownSignalException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    @Override
    public ApiDefinition getApiDefinition() {
        if (apiDefinition == null) {
            Request r = makeRequest();
            r.setRequestType(Request.getApiDefinition);
            try {
                apiDefinition = (ApiDefinition) rpc(r).getResult();
            } catch (ShutdownSignalException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
        return apiDefinition;
    }


    private Response rpc(Request request) throws ShutdownSignalException, IOException, TimeoutException {
        return (Response) Serializer.deserialize(rpc.primitiveCall(Serializer.serialize(request)));
    }

    @Override
    public Object getProperty(Proxy self, String property) {
        Request r = makeRequest();
        r.setRequestType(Request.getProperty);
        r.setPropertyName(property);
        r.setSelf(self);
        try {
            return rpc(r).getResult();
        } catch (ShutdownSignalException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    @Override
    public void putProperty(Proxy self, String property, Object value) {
        Request r = makeRequest();
        r.setRequestType(Request.setProperty);
        r.setPropertyName(property);
        r.setSelf(self);
        r.setValue(value);
        try {
            rpc(r).getResult();
        } catch (ShutdownSignalException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    @Override
    public Object getVariable(String name) {
        Request r = makeRequest();
        r.setRequestType(Request.getVariable);
        r.setVariableName(name);
        try {
            return rpc(r).getResult();
        } catch (ShutdownSignalException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }
}
