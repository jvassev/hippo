package hippo.server;

import hippo.Util;
import hippo.client.Proxy;
import hippo.client.ScriptingSession;
import hippo.client.TypeDefinition;

import java.rmi.RemoteException;

public class ForeignObject {

    private final Proxy proxy;

    private final ServerScriptingSession self;

    private final SessionLocator locator;

    private ScriptingSession proxysSession;

    private TypeDefinition type;

    ForeignObject(Proxy proxy, ServerScriptingSession self, SessionLocator locator) {
        this.proxy = proxy;
        this.self = self;
        this.locator = locator;
        try {
            this.type = getProxysSession().getApiDefinition().findType(proxy.getType());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public Object invoke(String method, Object... args) throws RemoteException {
        Object value = getProxysSession().invokeMethod(proxy, method, toProxy(args));
        return toJava(value);
    }


    public Object getProperty(String name) throws RemoteException {
        Object value = getProxysSession().getProperty(proxy, name);
        return toJava(value);
    }


    public void putProperty(String name, Object value) throws RemoteException {
        getProxysSession().putProperty(proxy, name, toProxy(value));
    }

    private Object toJava(Object remoteValue) throws RemoteException {
        if (remoteValue == null) {
            return null;
        } else if (Util.isWrapperType(remoteValue.getClass())) {
            return remoteValue;
        } else if (remoteValue instanceof Proxy) {
            Proxy p = (Proxy) remoteValue;
            if (p.getSessionId().equals(self.getId())) {
                return self.getRealObject(p);
            } else {
                return new ForeignObject(p, self, locator);
            }
        } else {
            return remoteValue;
        }
    }


    public Proxy getProxy() {
        return proxy;
    }

    private Object[] toProxy(Object... args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = toProxy(args[i]);
        }
        return args;
    }

    private Object toProxy(Object localValue) {
        if (localValue instanceof ForeignObject) {
            ForeignObject fo = (ForeignObject) localValue;
            return fo.getProxy();
        } else if (localValue instanceof Proxy) {
            Proxy p = (Proxy) localValue;
            return p;
        } else {
            return self.toProxy(localValue);
        }
    }

    private ScriptingSession getProxysSession() {
        if (proxysSession == null) {
            proxysSession = locator.lookup(proxy.getSessionId());
        }
        return proxysSession;
    }


    @Override
    public String toString() {
        return "ForeignObject [proxy=" + proxy + "]";
    }


    public TypeDefinition getType() {
        return type;
    }
}