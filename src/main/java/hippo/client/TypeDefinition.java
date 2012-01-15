package hippo.client;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class TypeDefinition implements Serializable {
    private static final long serialVersionUID = -2661720616551667553L;

    private final String name;
    private Set<String> methods;
    private Set<String> properties;

    public TypeDefinition(String name) {
        this.name = name;
        methods = new HashSet<String>();
        properties = new HashSet<String>();
    }

    public void defineMethod(String name) {
        methods.add(name);
    }

    public void defineProperty(String name) {
        properties.add(name);
    }

    public String getName() {
        return name;
    }

    public boolean isMethod(String name) {
        return methods.contains(name);
    }

    public boolean isProperty(String name) {
        return properties.contains(name);
    }
}
