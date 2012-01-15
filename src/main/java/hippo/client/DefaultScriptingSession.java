package hippo.client;

import hippo.client.impl.ProxiedScriptingObject;
import hippo.client.impl.Util;

import java.rmi.RemoteException;

public class DefaultScriptingSession implements ScriptingSession {

    private final ScriptingSessionFactory service;

    private ApiDefinition apiDefinition;

    private ScriptingSession delegate;

    private String id;

    public DefaultScriptingSession(ScriptingSessionFactory service) {
        this.service = service;
    }

    @Override
    public void end() {
        try {
            delegate.end();
        } catch (RemoteException e) {
        }
    }

    public Object toJs(Object res) {
        if (res == null) {
            return null;
        } else if (res instanceof String || Util.isWrapperType(res.getClass())) {
            return res;
        } else if (res instanceof Proxy) {
            Proxy proxy = (Proxy) res;
            return new ProxiedScriptingObject(this, proxy, apiDefinition.findType(proxy.getType()));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Object[] toProxies(Object[] args) {
        Object[] res = args.clone();

        for (int i = 0; i < res.length; i++) {
            Object o = res[i];

            if (o instanceof ProxiedScriptingObject) {
                ProxiedScriptingObject spo = (ProxiedScriptingObject) o;
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
        return delegate.newObject(name, args);
    }

    @Override
    public Object invokeMethod(Proxy self, String name, Object[] args) throws RemoteException {
        return delegate.invokeMethod(self, name, args);
    }

    @Override
    public void start() throws RemoteException {
        this.delegate = service.openSession();
    }

    @Override
    public ApiDefinition getApiDefinition() throws RemoteException {
        if (apiDefinition == null) {
            apiDefinition = delegate.getApiDefinition();
        }
        return apiDefinition;
    }

    @Override
    public Object getProperty(Proxy self, String property) throws RemoteException {
        return delegate.getProperty(self, property);
    }

    @Override
    public Object getVariable(String name) throws RemoteException {
        return delegate.getVariable(name);
    }

    @Override
    public void putProperty(Proxy self, String property, Object value) throws RemoteException {
        delegate.putProperty(self, property, value);
    }

    @Override
    public String getId() throws RemoteException {
        if (id == null) {
            id = delegate.getId();
        }
        return id;
    }
}
