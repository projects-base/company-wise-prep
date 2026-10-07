import java.util.*;

class MovingAverage {
    private final int[] window;
    private int count, head;
    private long sum;

    public MovingAverage(int size) {
        window = new int[size];
    }

    public double next(int val) {
        if (count == window.length) {
            sum -= window[head];
        } else {
            count++;
        }
        window[head] = val;
        sum += val;
        head = (head + 1) % window.length;
        return (double) sum / count;
    }
}
