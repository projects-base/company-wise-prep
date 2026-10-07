import java.util.*;

class MinHeap {
    private int[] a = new int[16];
    private int n;

    public MinHeap() {
    }

    public void offer(int x) {
        if (n == a.length) a = Arrays.copyOf(a, n * 2);
        a[n] = x;
        siftUp(n++);
    }

    public Integer poll() {
        if (n == 0) return null;
        int min = a[0];
        a[0] = a[--n];
        siftDown(0);
        return min;
    }

    public Integer peek() {
        return n == 0 ? null : a[0];
    }

    public int size() {
        return n;
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (a[parent] <= a[i]) break;
            swap(i, parent);
            i = parent;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int l = 2 * i + 1, r = l + 1, smallest = i;
            if (l < n && a[l] < a[smallest]) smallest = l;
            if (r < n && a[r] < a[smallest]) smallest = r;
            if (smallest == i) return;
            swap(i, smallest);
            i = smallest;
        }
    }

    private void swap(int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
