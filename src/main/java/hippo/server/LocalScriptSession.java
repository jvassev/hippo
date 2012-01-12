package hippo.server;

import hippo.client.LocalProxy;
import hippo.client.Proxy;
import hippo.client.impl.Util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

public abstract class LocalScriptSession {

    private BidiMap typesToClasses;

    private Map<String, Object> objects;

    public LocalScriptSession() {
        objects = new HashMap<String, Object>();
        typesToClasses = new DualHashBidiMap();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Set<String> getTypes() {
        return new HashSet(typesToClasses.keySet());
    }

    public void registerType(String type, Class<?> clazz) {
        typesToClasses.put(type, clazz);
    }

    public void end() {
        // throw new UnsupportedOperationException("implement me!");
    }

    public Proxy newObject(final String name, Object[] args) {
        LocalProxy proxy = makeProxy(name);
        Object instace = newInstaceReal(name, args);
        registerProxy(proxy, instace);

        return proxy;
    }

    private void registerProxy(LocalProxy proxy, Object instace) {
        objects.put(proxy.getId(), instace);
    }

    private LocalProxy makeProxy(final String name) {
        LocalProxy proxy = new LocalProxy();
        proxy.setId(UUID.randomUUID().toString());
        proxy.setType(name);
        return proxy;
    }

    public Object invoke(Proxy self, String name, Object[] args) {
        LocalProxy proxy = (LocalProxy) self;
        Object instance = getRealObject(proxy);
        unProxy(args);
        Object res = invokeReal(instance, name, args);
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
                    throw new IllegalStateException("cannot proxy or serialize " + res);
                }
            } else {
                LocalProxy resProxy = makeProxy(type);
                registerProxy(resProxy, res);
                return resProxy;
            }
        }
    }

    private Object getRealObject(LocalProxy proxy) {
        Object object = objects.get(proxy.getId());
        if (object == null) {
            throw new IllegalArgumentException("cannot find object behind " + proxy);
        }

        return object;
    }

    private void unProxy(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            Object o = args[i];
            if (o instanceof LocalProxy) {
                LocalProxy proxy = (LocalProxy) o;
                args[i] = getRealObject(proxy);
            }
        }
    }

    protected abstract Object invokeReal(Object instance, String name, Object[] args);

    protected abstract Object newInstaceReal(String name, Object[] args);
}
