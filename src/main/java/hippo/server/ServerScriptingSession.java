package hippo.server;

import hippo.client.ApiDefinition;
import hippo.client.ForeignObject;
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

    private SessionLocator locator;

    public ServerScriptingSession() {
        objects = new HashMap<String, Object>();
        variables = new HashMap<String, Object>();
        typesToClasses = new DualHashBidiMap();
    }

    @Override
    public ApiDefinition getApiDefinition() throws RemoteException {
        return apiDefinition;
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
        proxy.setSessionId(id);
        return proxy;
    }

    @Override
    public Object invokeMethod(Proxy self, String name, Object[] args) {
        Object instance = getRealObject(self);
        unProxy(args);
        Object res = invokeReal(instance, name, args);
        return toProxy(res);
    }

    public Object getRealObject(Proxy proxy) {
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

    private Object unProxy(Object removeValue) {
        if (removeValue instanceof Proxy) {
            Proxy proxy = (Proxy) removeValue;
            Object real = getRealObject(proxy);
            if (real != null) {
                return real;
            } else {
                // Proxy from other session
                return new ForeignObject(proxy, this, locator);
            }
        } else {
            return removeValue;
        }
    }

    public Object toProxy(Object localValue) {
        if (localValue == null) {
            return null;
        } else if (localValue instanceof String || Util.isWrapperType(localValue.getClass())) {
            return localValue;
        } else if (localValue instanceof ForeignObject) {
            ForeignObject fo = (ForeignObject) localValue;
            return fo.getProxy();
        } else {
            Class<?> cls = localValue.getClass();
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
                if (localValue instanceof Serializable) {
                    return localValue;
                } else {
                    throw new IllegalStateException("cannot proxy nor serialize " + localValue);
                }
            } else {
                Proxy proxy = makeProxy(type);
                registerProxy(proxy, localValue);
                return proxy;
            }
        }
    }

    void defineApi(ApiDefinition apiDefinition) {
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

    void setId(String id) {
        this.id = id;
    }

    void defineClassMapping(Map<String, Class<?>> mapping) {
        for (Map.Entry<String, Class<?>> e : mapping.entrySet()) {
            typesToClasses.put(e.getKey(), e.getValue());
        }
    }

    public void start() {
    }

    @Override
    public void end() {
    }

    void setLocator(SessionLocator locator) {
        this.locator = locator;
    }

    protected abstract Object getVariableReal(String name);

    protected abstract Object invokeReal(Object instance, String name, Object[] args);

    protected abstract Object newInstaceReal(String name, Object[] args);

    protected abstract Object getPropertyReal(Object instance, String name);

    protected abstract Object putPropertyReal(Object instance, String name, Object value);
}
