package hippo.client;

import hippo.client.impl.ScriptableProxyObject;
import hippo.client.impl.Util;

import java.rmi.RemoteException;
import java.util.Set;

public class DefaultScriptingSession implements ScriptingSession {

    private final ScriptingSessionService service;
    private String sessionId;
    private Set<String> types;

    public DefaultScriptingSession(ScriptingSessionService service) {
        this.service = service;
    }

    @Override
    public void end() {
        try {
            service.destroySession(sessionId);
        } catch (RemoteException e) {
        }
    }

    @Override
    public Object wrap(Object res) {
        if (res == null) {
            return null;
        } else if (res instanceof String || Util.isWrapperType(res.getClass())) {
            return res;
        } else if (res instanceof LocalProxy) {
            LocalProxy proxy = (LocalProxy) res;
            return new ScriptableProxyObject(this, proxy);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Object[] proxyArgs(Object[] args) {
        Object[] res = args.clone();

        for (int i = 0; i < res.length; i++) {
            Object o = res[i];

            if (o instanceof ScriptableProxyObject) {
                ScriptableProxyObject spo = (ScriptableProxyObject) o;
                if (spo.isFromSession(this)) {
                    res[i] = spo.getProxy();
                } else {
                    throw new IllegalStateException("dont know how to proxy other session's data");
                }
            }
        }

        return res;
    }

    @Override
    public Proxy newObject(String name, Object[] args) throws RemoteException {
        return service.newObject(sessionId, name, args);
    }

    @Override
    public Object invoke(Proxy self, String name, Object[] args) throws RemoteException {
        return service.invoke(sessionId, self, name, args);
    }

    @Override
    public Set<String> getTypes() throws RemoteException {
        if (types == null) {
            types = service.getTypes(sessionId);
        }
        return types;
    }

    @Override
    public void start() throws RemoteException {
        sessionId = service.newSession();
    }
}
