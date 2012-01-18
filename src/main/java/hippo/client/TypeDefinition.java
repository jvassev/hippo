package hippo.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TypeDefinition implements Serializable {

    private static final long serialVersionUID = -2661720616551667553L;

    private final String name;

    private Set<String> methods;

    private Map<String, PropertyDefinition> properties;

    private boolean instantiatable;

    public TypeDefinition(String name) {
        instantiatable = true;
        this.name = name;
        methods = new HashSet<String>();
        properties = new HashMap<String, PropertyDefinition>();
    }

    public void defineMethod(String name) {
        methods.add(name);
    }

    public void defineProperty(PropertyDefinition prop) {
        properties.put(prop.getName(), prop);
    }

    public String getName() {
        return name;
    }

    public boolean isMethod(String name) {
        return methods.contains(name);
    }

    public boolean isProperty(String name) {
        return properties.keySet().contains(name);
    }


    public boolean isInstantiatable() {
        return instantiatable;
    }


    public void setInstantiatable(boolean instantiatable) {
        this.instantiatable = instantiatable;
    }

    public PropertyDefinition getProperty(String name) {
        return properties.get(name);
    }
}
