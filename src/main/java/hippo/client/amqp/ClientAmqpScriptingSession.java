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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ShutdownSignalException e) {
        }

        Request req = makeRequest();
        req.setRequestType(Request.openSession);
        doRpcAndHandleError(req);
    }

    @Override
    public void end() {
        Request req = makeRequest();
        req.setRequestType(Request.closeSession);

        doRpcAndHandleError(req);
    }

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public Proxy newObject(String type, Object[] args) {
        Request req = makeRequest();
        req.setRequestType(Request.newObject);
        req.setTypeName(type);
        req.setArgs(args);

        return (Proxy) doRpcAndHandleError(req);
    }

    private Object resultOrThrow(Response response) {
        if (response.getException() != null) {
            throw new RuntimeException(response.getException());
        } else {
            return response.getResult();
        }
    }

    private Request makeRequest() {
        Request request = new Request();
        request.setSessionId(sessionId);
        return request;
    }

    @Override
    public Object invokeMethod(Proxy self, String method, Object[] args) {
        Request req = makeRequest();
        req.setRequestType(Request.invokeMethod);
        req.setSelf(self);
        req.setMethod(method);
        req.setArgs(args);

        return doRpcAndHandleError(req);
    }

    @Override
    public ApiDefinition getApiDefinition() {
        if (apiDefinition == null) {
            Request req = makeRequest();
            req.setRequestType(Request.getApiDefinition);

            apiDefinition = (ApiDefinition) doRpcAndHandleError(req);
        }
        return apiDefinition;
    }


    private Response rpc(Request request) throws ShutdownSignalException, IOException, TimeoutException {
        return (Response) Serializer.deserialize(rpc.primitiveCall(Serializer.serialize(request)));
    }

    @Override
    public Object getProperty(Proxy self, String property) {
        Request req = makeRequest();
        req.setRequestType(Request.getProperty);
        req.setPropertyName(property);
        req.setSelf(self);

        return doRpcAndHandleError(req);
    }

    @Override
    public void putProperty(Proxy self, String property, Object value) {
        Request req = makeRequest();
        req.setRequestType(Request.setProperty);
        req.setPropertyName(property);
        req.setSelf(self);
        req.setValue(value);

        doRpcAndHandleError(req);
    }

    @Override
    public Object getVariable(String name) {
        Request req = makeRequest();
        req.setRequestType(Request.getVariable);
        req.setVariableName(name);

        return doRpcAndHandleError(req);
    }

    private Object doRpcAndHandleError(Request r) {
        Response response;
        try {
            response = rpc(r);
        } catch (ShutdownSignalException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

        return resultOrThrow(response);
    }
}
