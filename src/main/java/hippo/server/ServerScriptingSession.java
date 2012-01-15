package hippo.server;

import hippo.client.ApiDefinition;
import hippo.client.Proxy;
import hippo.client.ScriptingSession;
import hippo.client.impl.Util;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

public abstract class ServerScriptingSession implements ScriptingSession {

    private final BidiMap typesToClasses;

    private final Map<String, Object> objects;

    private final Map<String, Object> variables;

    private ApiDefinition apiDefinition;

    private String id;

    public ServerScriptingSession() {
        objects = new HashMap<String, Object>();
        variables = new HashMap<String, Object>();
        typesToClasses = new DualHashBidiMap();
    }

    @Override
    public ApiDefinition getApiDefinition() throws RemoteException {
        return apiDefinition;
    }

    public void defineClassMapping(String type, Class<?> clazz) {
        typesToClasses.put(type, clazz);
    }

    @Override
    public void end() {
        // throw new UnsupportedOperationException("implement me!");
    }

    @Override
    public Proxy newObject(final String name, Object[] args) {
        Object instace = newInstaceReal(name, args);
        return (Proxy) toProxy(instace);
    }

    private void registerProxy(Proxy proxy, Object instace) {
        objects.put(proxy.getId(), instace);
    }

    private Proxy makeProxy(final String name) {
        Proxy proxy = new Proxy();
        proxy.setId(UUID.randomUUID().toString());
        proxy.setType(name);
        proxy.setId(id);
        return proxy;
    }

    @Override
    public Object invokeMethod(Proxy self, String name, Object[] args) {
        Object instance = getRealObject(self);
        unProxy(args);
        Object res = invokeReal(instance, name, args);
        return toProxy(res);
    }

    private Object getRealObject(Proxy proxy) {
        Object object = objects.get(proxy.getId());
        return object;
    }

    private void unProxy(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = unProxy(args[i]);
        }
    }

    @Override
    public Object getProperty(Proxy self, String property) throws RemoteException {
        Object instance = getRealObject(self);
        Object res = getPropertyReal(instance, property);
        return toProxy(res);
    }

    @Override
    public void putProperty(Proxy self, String property, Object value) throws RemoteException {
        Object instance = getRealObject(self);
        putPropertyReal(instance, property, unProxy(value));
    }

    private Object unProxy(Object o) {
        if (o instanceof Proxy) {
            Proxy proxy = (Proxy) o;
            Object real = getRealObject(proxy);
            if (real != null) {
                return real;
            } else {
                throw new IllegalStateException("cannot unproxy" + proxy);
            }
        } else {
            return o;
        }
    }

    private Object toProxy(Object res) {
        if (res == null) {
            return null;
        } else if (res instanceof String || Util.isWrapperType(res.getClass())) {
            return res;
        } else {
            Class<?> cls = res.getClass();
            String type;
            do {
                type = (String) typesToClasses.inverseBidiMap().get(cls);
                if (type != null) {
                    break;
                } else {
                    cls = cls.getSuperclass();
                }
            } while (cls != null);

            if (type == null) {
                if (res instanceof Serializable) {
                    return res;
                } else {
                    throw new IllegalStateException("cannot proxy nor serialize " + res);
                }
            } else {
                Proxy proxy = makeProxy(type);
                registerProxy(proxy, res);
                return proxy;
            }
        }
    }

    public void defineApi(ApiDefinition apiDefinition) {
        this.apiDefinition = apiDefinition;
    }

    @Override
    public Object getVariable(String name) throws RemoteException {
        Object var = variables.get(name);
        if (var == null) {
            var = getVariableReal(name);
            variables.put(name, var);
        }

        return toProxy(var);
    }

    @Override
    public String getId() throws RemoteException {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected abstract Object getVariableReal(String name);

    protected abstract Object invokeReal(Object instance, String name, Object[] args);

    protected abstract Object newInstaceReal(String name, Object[] args);

    protected abstract Object getPropertyReal(Object instance, String name);

    protected abstract Object putPropertyReal(Object instance, String name, Object value);

}
