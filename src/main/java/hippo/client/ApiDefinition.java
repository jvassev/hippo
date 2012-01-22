package hippo.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ApiDefinition implements Serializable {

    private static final long serialVersionUID = -3298264233533615798L;

    private final Map<String, TypeDefinition> types;

    private final Set<String> variables;

    private final String name;

    public ApiDefinition(String name) {
        this.name = name;
        variables = new HashSet<String>();
        types = new HashMap<String, TypeDefinition>();
    }

    public ApiDefinition defineType(TypeDefinition type) {
        types.put(type.getName(), type);
        return this;
    }

    public ApiDefinition defineVariable(String name) {
        variables.add(name);
        return this;
    }

    public boolean variableDefined(String name) {
        return variables.contains(name);
    }

    public Map<String, TypeDefinition> getTypes() {
        return types;
    }

    public TypeDefinition findType(String name) {
        return types.get(name);
    }

    public String getName() {
        return name;
    }
}
