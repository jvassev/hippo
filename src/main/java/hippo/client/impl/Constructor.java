package hippo.client.impl;

import hippo.client.DefaultScriptingSession;
import hippo.client.Proxy;

import java.rmi.RemoteException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Constructor extends ScriptableObject implements Function {

    private static final long serialVersionUID = 1L;
    private final String name;
    private final DefaultScriptingSession session;

    public Constructor(DefaultScriptingSession session, String name) {
        this.session = session;
        this.name = name;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        throw new UnsupportedOperationException("this is not a method");
    }

    @Override
    public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
        try {
            Proxy proxy = session.newObject(name, session.proxyArgs(args));
            return new ScriptableProxyObject(session, proxy);
        } catch (RemoteException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public String getClassName() {
        return name;
    }
}