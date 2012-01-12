package hippo.client.impl;

import java.util.HashSet;

public class Util {
    private static final HashSet<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    private static HashSet<Class<?>> getWrapperTypes() {
        HashSet<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }

    public static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

}
