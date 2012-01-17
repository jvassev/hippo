package hippo.example.domain;

public class Counter {

    private int i;

    public Counter() {

    }

    public Counter(int i) {
        this.i = i;
    }

    public void inc() {
        i++;
    }

    public int get() {
        return i;
    }

    @Override
    public Object clone() {
        return new Counter(i);
    }

    public void copy(Counter other) {
        this.i = other.i;
    }
}
