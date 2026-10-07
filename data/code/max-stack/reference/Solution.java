import java.util.*;

class MaxStack {
    private final Deque<Integer> values = new ArrayDeque<>();
    private final Deque<Integer> maxima = new ArrayDeque<>(); // maxima.peek() = max of values

    public MaxStack() {
    }

    public void push(int x) {
        values.push(x);
        maxima.push(maxima.isEmpty() ? x : Math.max(x, maxima.peek()));
    }

    public int pop() {
        maxima.pop();
        return values.pop();
    }

    public int top() {
        return values.peek();
    }

    public int peekMax() {
        return maxima.peek();
    }
}
