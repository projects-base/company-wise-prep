import java.util.*;

class KStacks {
    private final int[] values; // the one shared array
    private final int[] next;   // used slot: slot below it in its stack; free slot: next free slot
    private final int[] top;    // top slot of each stack, -1 if empty
    private int free;           // head of the free-slot list, -1 if full

    public KStacks(int k, int capacity) {
        values = new int[capacity];
        next = new int[capacity];
        top = new int[k];
        Arrays.fill(top, -1);
        for (int i = 0; i < capacity; i++) next[i] = i + 1 < capacity ? i + 1 : -1;
        free = 0;
    }

    public boolean push(int stack, int x) {
        if (free == -1) return false;
        int slot = free;
        free = next[slot];
        values[slot] = x;
        next[slot] = top[stack];
        top[stack] = slot;
        return true;
    }

    public int pop(int stack) {
        int slot = top[stack];
        if (slot == -1) return -1;
        top[stack] = next[slot];
        next[slot] = free;
        free = slot;
        return values[slot];
    }

    public int peek(int stack) {
        int slot = top[stack];
        return slot == -1 ? -1 : values[slot];
    }

    public boolean isEmpty(int stack) {
        return top[stack] == -1;
    }
}
