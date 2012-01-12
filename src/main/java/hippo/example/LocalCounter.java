package hippo.example;

import org.mozilla.javascript.ScriptableObject;

public class LocalCounter extends ScriptableObject {
    private static final long serialVersionUID = 7080429343972626993L;
    private int i;

    public LocalCounter() {

    }

    public LocalCounter(int i) {
        this.i = i;
    }

    public void jsConstructor(int i) {
        this.i = i;
    }

    public void jsFunction_inc() {
        i++;
    }

    public int jsFunction_get() {
        return i;
    }

    public Object jsFunction_clone() {
        return new LocalCounter(i);
    }

    public void jsFunction_copy(LocalCounter other) {
        this.i = other.i;
    }

    @Override
    public String getClassName() {
        return "LocalCounter";
    }
}
