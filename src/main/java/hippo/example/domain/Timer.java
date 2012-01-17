package hippo.example.domain;


public class Timer {

    private long start;

    public Timer() {
        start = System.currentTimeMillis();
    }

    public double getElapsed() {
        return (System.currentTimeMillis() - start) / 1000.0;
    }
}
