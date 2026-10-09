**Short answer:** Use an external merge sort. First, read the file in chunks that fit in memory, sort each chunk and write it out as a sorted run. Then merge runs in passes. With `M` memory slots you can merge `M − 1` runs at once, one input slot per run plus one output buffer, using a min-heap over the heads of the runs. Repeat until one run is left. The total work is O(N log N), and the number of passes is about `⌈log_(M−1)(N/M)⌉`.

## Approach

- **Why not just sort:** the data does not fit in RAM. Random access to disk is slow, so the algorithm must read and write in long sequential streams.
- **Run creation:** chunks of `M` numbers, each sorted in memory. You get `⌈N/M⌉` runs.
- **Key insight, the k-way merge:** to merge `k` sorted runs you only need the current head of each run in memory. A min-heap of `(run, position)` cursors gives the next smallest value in O(log k). Pop it, write it out, then push that run's next element.
- **Fan-in:** a larger `k` means fewer passes over the disk, and disk passes are the real cost. Memory limits `k` to `M − 1` here. Runs are merged in groups of `M − 1`. A group with a single run is copied unchanged.
- The problem asks us to simulate this and record the runs after each stage.

## Solution

```java
import java.util.*;

class Solution {
    public int[][][] externalSort(int[] file, int memory) {
        List<int[][]> stages = new ArrayList<>();

        // Stage 0: sorted runs of at most `memory` numbers.
        List<int[]> runs = new ArrayList<>();
        for (int start = 0; start < file.length; start += memory) {
            int[] chunk = Arrays.copyOfRange(file, start, Math.min(file.length, start + memory));
            Arrays.sort(chunk);
            runs.add(chunk);
        }
        stages.add(runs.toArray(new int[0][]));

        // Merge passes with fan-in memory - 1.
        int fanIn = memory - 1;
        while (runs.size() > 1) {
            List<int[]> next = new ArrayList<>();
            for (int g = 0; g < runs.size(); g += fanIn) {
                next.add(mergeGroup(runs.subList(g, Math.min(runs.size(), g + fanIn))));
            }
            runs = next;
            stages.add(runs.toArray(new int[0][]));
        }
        return stages.toArray(new int[0][][]);
    }

    /** k-way merge: a min-heap holds one cursor (run, position) per run. */
    private int[] mergeGroup(List<int[]> group) {
        int total = 0;
        for (int[] r : group) total += r.length;
        int[] out = new int[total];
        PriorityQueue<int[]> heap = new PriorityQueue<>(
            (x, y) -> Integer.compare(group.get(x[0])[x[1]], group.get(y[0])[y[1]]));
        for (int r = 0; r < group.size(); r++) if (group.get(r).length > 0) heap.add(new int[] {r, 0});
        int k = 0;
        while (!heap.isEmpty()) {
            int[] cur = heap.poll();
            int[] run = group.get(cur[0]);
            out[k++] = run[cur[1]];
            if (cur[1] + 1 < run.length) heap.add(new int[] {cur[0], cur[1] + 1});
        }
        return out;
    }
}
```

The comparator uses `Integer.compare`, not subtraction, because the values go down to −10⁹ and `a − b` could overflow. An empty file gives no runs, so the loop never runs and the answer is `[[]]`.

## Complexity

- **Run creation:** O(N log M).
- **Each merge pass:** O(N log k) with `k = M − 1`. There are `⌈log_k(⌈N/M⌉)⌉` passes. The total is O(N log N) comparisons.
- **I/O, which is what matters in real life:** each pass reads and writes the whole file once, so the I/O is O(N · passes). That is why a large fan-in matters.
- **Space:** O(k) for the heap. The simulation holds everything in arrays, but a real one holds only one buffer per run.

## Edge cases

- Empty file: `[[]]`.
- The file fits in memory: one run, no merge passes.
- A last chunk shorter than `memory`, and a last group with a single run (carried over unchanged).
- Duplicates are kept. The merge is a plain min-merge, so ties can come out in any order, which is fine for integers.

## Variations and real-world follow-ups

- **In real Java:** each run is a temp file written with a `BufferedWriter` or `DataOutputStream`. The merge holds one `BufferedReader` per run, and the heap stores `(value, readerIndex)`.
- **Replacement selection:** run generation with a heap produces runs about `2M` long on random input, so there are fewer runs.
- **Bigger I/O buffers:** reading pages per run instead of single values. That trades fan-in for fewer disk seeks.
- **Parallel or distributed:** sort the chunks on many machines, then merge (MapReduce shuffle, Spark sort). Range-partitioning first means each output partition can be sorted independently.
- **Same idea elsewhere:** PostgreSQL uses an external merge sort when a sort does not fit in `work_mem`, and `EXPLAIN ANALYZE` reports it as `Sort Method: external merge`.
- **Merge k Sorted Lists (LeetCode 23)** is the in-memory version of the merge step.

Related: [C2 · Where memory goes in Java solutions](../academy/lessons/C2.md), [Q5 · Indexes and reading EXPLAIN](../academy/lessons/Q5.md).

Practise it in the app: Run / Submit on this page.
