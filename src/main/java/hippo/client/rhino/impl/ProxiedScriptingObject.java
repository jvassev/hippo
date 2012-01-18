package hippo.client.rhino.impl;

import hippo.client.Proxy;
import hippo.client.ScriptingSession;
import hippo.client.TypeDefinition;
import hippo.client.rhino.DefaultScriptingSession;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ProxiedScriptingObject extends ScriptableObject {

    private static final long serialVersionUID = -7779023254342628386L;

    private final Proxy proxy;

    private final DefaultScriptingSession session;

    private final Map<String, Method> methods = new HashMap<String, Method>();

    private final TypeDefinition type;

    public ProxiedScriptingObject(DefaultScriptingSession session, Proxy proxy) {
        this.session = session;
        this.proxy = proxy;
        try {
            this.type = session.getApiDefinition().findType(proxy.getType());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getClassName() {
        return proxy.getType();
    }

    @Override
    public Object get(String name, Scriptable start) {
        if (type.isMethod(name)) {
            return makeMethod(name);
        } else if (type.isProperty(name)) {
            return getProperty(name);
        } else {
            return super.get(name, start);
        }
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        if (type.isMethod(name)) {
            throw new IllegalStateException("cannot override API method " + name);
        } else if (type.isProperty(name)) {
            if (type.getProperty(name).isWritable()) {
                putProperty(name, value);
            } else {
                throw new EvaluatorException("'" + name + "' is read-only");
            }
        } else {
            super.put(name, start, value);
        }
    }

    private void putProperty(String name, Object value) {
        Object[] args = new Object[] { value };
        session.toProxies(args);
        try {
            session.putProperty(proxy, name, args[0]);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getProperty(String name) {
        try {
            Object res = session.getProperty(proxy, name);
            return session.toJs(res);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private Object makeMethod(String name) {
        Method method = methods.get(name);
        if (method == null) {
            method = new Method(name, session, proxy);
            methods.put(name, method);
        }

        return method;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public ScriptingSession getSession() {
        return session;
    }
}
