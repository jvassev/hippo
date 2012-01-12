package hippo.client.impl;

import hippo.client.Proxy;
import hippo.client.ScriptingSession;

import java.rmi.RemoteException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Method extends ScriptableObject implements Function {

    private static final long serialVersionUID = -6098701947260250943L;
    private final Proxy proxy;
    private final ScriptingSession session;
    private final String name;

    public Method(String name, ScriptingSession session, Proxy proxy) {
        this.name = name;
        this.session = session;
        this.proxy = proxy;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        Object res;
        try {
            res = session.invoke(proxy, name, session.proxyArgs(args));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        return session.wrap(res);
    }

    @Override
    public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
        throw new UnsupportedOperationException("implement me!");
    }

    @Override
    public String getClassName() {
        return "Function";
    }
}
