import java.util.*;

class StreamSampler {
    private final Random rng;
    private long seen;
    private int chosen;

    public StreamSampler(Random rng) {
        this.rng = rng;
    }

    public void add(int x) {
        seen++;
        // keep the new element with probability 1/seen
        if (seen <= Integer.MAX_VALUE ? rng.nextInt((int) seen) == 0 : rng.nextDouble() * seen < 1) chosen = x;
    }

    public int sample() {
        return chosen;
    }
}
