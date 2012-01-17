package hippo.example.domain;


public class Describer {

    public String describe(Object o) {
        return o.toString();
    }

    public Object touch(Object o) {
        return o;
    }
}
