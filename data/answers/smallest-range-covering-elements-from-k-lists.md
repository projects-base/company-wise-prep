**Short answer:** Keep one pointer per list; the current range runs from the smallest pointed value to the largest. Record it, then advance the pointer of the smallest value, using a min-heap for the minimum and a variable for the maximum, for O(N log k) over N total numbers.

## Picture it

`A = [4,10,15,24,26]`, `B = [0,9,12,20]`, `C = [5,18,22,30]`. The heap holds one value per list.

| step | values (A, B, C) | min | max | width | best range | action |
|---|---|---|---|---|---|---|
| 1 | 4, 0, 5 | 0 | 5 | 5 | [0, 5] | advance B to 9 |
| 2 | 4, 9, 5 | 4 | 9 | 5 | [0, 5] | advance A to 10 |
| 3 | 10, 9, 5 | 5 | 10 | 5 | [0, 5] | advance C to 18 |
| 4 | 10, 9, 18 | 9 | 18 | 9 | [0, 5] | advance B to 12 |
| 5 | 10, 12, 18 | 10 | 18 | 8 | [0, 5] | advance A to 15 |
| 6 | 15, 12, 18 | 12 | 18 | 6 | [0, 5] | advance B to 20 |
| 7 | 15, 20, 18 | 15 | 20 | 5 | [0, 5] | advance A to 24 |
| 8 | 24, 20, 18 | 18 | 24 | 6 | [0, 5] | advance C to 22 |
| 9 | 24, 20, 22 | 20 | 24 | 4 | [20, 24] | B is exhausted, stop |

Ties (steps 2, 3 and 7) do not replace the best, because the smaller start wins.

**The picture in one sentence:** only moving the smallest value can shrink the range, so this is a k-way merge that remembers the running maximum.

## Approach

- **Brute force:** for every pair of values as a range, check each list for a value inside. Cubic or worse.
- **K pointers, linear scan:** at each step scan the k pointed values for min and max, then advance the min. O(N · k).
- **Key insight:** each pointer is at its list's first value that is not below the current min. So the max is the smallest possible right end for this left end, and only moving the min can help.
- **When to stop:** when the minimum's list is exhausted. No later range could include that list.

## Solution

```java
import java.util.*;

class Solution {
    private record Node(int value, int list, int index) {}

    public int[] smallestRange(List<List<Integer>> nums) {
        PriorityQueue<Node> heap = new PriorityQueue<>(Comparator.comparingInt(Node::value));
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < nums.size(); i++) {
            int v = nums.get(i).get(0);
            heap.add(new Node(v, i, 0));
            max = Math.max(max, v);
        }

        int bestLo = 0, bestHi = 0;
        long bestWidth = Long.MAX_VALUE;
        while (true) {
            Node min = heap.poll();
            long width = (long) max - min.value();
            if (width < bestWidth) {                       // strict: the earlier, smaller start wins ties
                bestWidth = width;
                bestLo = min.value();
                bestHi = max;
            }
            List<Integer> list = nums.get(min.list());
            int next = min.index() + 1;
            if (next == list.size()) return new int[] {bestLo, bestHi};
            int v = list.get(next);
            heap.add(new Node(v, min.list(), next));
            max = Math.max(max, v);
        }
    }
}
```

`list.get(next)` is O(1) on an `ArrayList`; keep an iterator per list if they may be linked lists.

## Complexity

- **Time:** O(N log k). Each of the N numbers enters the heap at most once, and the heap holds k nodes.
- **Space:** O(k) for the heap.

## Edge cases

- One list: the range is `[first, first]`, width 0.
- A value shared by all lists: width 0.
- Duplicates and negative numbers: nothing special.
- An empty list makes the task impossible. LeetCode rules it out, so validate it if your input may contain one.
- Widths use `long`, so extreme `int` values cannot overflow.

## Follow-up: improve the k-pointer brute force with a priority queue

- **The bottleneck:** each step scans all k pointed values to find the minimum and maximum, so the total is O(N · k).
- **Minimum:** keep the k values in a min-heap. Finding and replacing the minimum becomes O(log k).
- **Maximum:** no second heap is needed. Only the minimum is ever removed, so the max changes only when the new value is larger.
- **Result:** O(N log k), which is the solution above. For k = 3,500 each step touches about 12 heap levels instead of scanning 3,500 values.

## Variations

- **Sort and slide:** put all (value, list) pairs in one sorted array, then find the smallest window containing all k lists. O(N log N) time and O(N) space, like Minimum Window Substring.
- **Return the chosen elements too:** copy the k current values whenever the best improves.
- **Lists too big for memory:** the heap version reads each list as a stream.

Related: [Merge K Sorted Lists](merge-k-sorted-lists.md), [Minimum Window Substring](minimum-window-substring.md), [T3 · Linked lists, intervals, heaps and top-K](../academy/lessons/T3.md).
