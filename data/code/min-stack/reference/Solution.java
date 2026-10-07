import java.util.*;

class MinStack {
    private int[] vals = new int[16], mins = new int[16]; // mins[i] = min of vals[0..i]
    private int size;

    public MinStack() {
    }

    public void push(int val) {
        if (size == vals.length) {
            vals = Arrays.copyOf(vals, size * 2);
            mins = Arrays.copyOf(mins, size * 2);
        }
        vals[size] = val;
        mins[size] = size == 0 ? val : Math.min(val, mins[size - 1]);
        size++;
    }

    public void pop() {
        size--;
    }

    public int top() {
        return vals[size - 1];
    }

    public int getMin() {
        return mins[size - 1];
    }
}
