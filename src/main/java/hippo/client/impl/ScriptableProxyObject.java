package hippo.client.impl;

import hippo.client.DefaultScriptingSession;
import hippo.client.Proxy;
import hippo.client.ScriptingSession;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ScriptableProxyObject extends ScriptableObject {

    private static final long serialVersionUID = -7779023254342628386L;
    private final Proxy proxy;
    private final DefaultScriptingSession session;
    private final Map<String, Method> methods = new HashMap<String, Method>();

    public ScriptableProxyObject(DefaultScriptingSession session, Proxy proxy) {
        this.session = session;
        this.proxy = proxy;
    }

    @Override
    public String getClassName() {
        return proxy.getType();
    }

    @Override
    public Object get(String name, Scriptable start) {
        Object object = super.get(name, start);
        if (object == Scriptable.NOT_FOUND) {
            return makeMethod(name);
        } else {
            return object;
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

    public boolean isFromSession(ScriptingSession other) {
        return getSession() == other;
    }
}
