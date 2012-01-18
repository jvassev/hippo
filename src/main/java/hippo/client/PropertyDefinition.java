package hippo.client;

import java.io.Serializable;


public class PropertyDefinition implements Serializable {

    private static final long serialVersionUID = -4109397367919431065L;

    private String name;

    private boolean writable;


    public boolean isWritable() {
        return writable;
    }

    public PropertyDefinition(String name) {
        writable = true;
        this.name = name;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public String getName() {
        return name;
    }
}
