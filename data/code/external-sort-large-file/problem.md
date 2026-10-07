A file of integers is too large to sort in memory, so it must be sorted with an **external merge sort**. Simulate the algorithm exactly as described below and return the contents of "disk" after each stage.

You are given the file as the array `file` and the memory size `memory` (how many integers fit in RAM at once).

1. **Run creation (stage 0).** Read the file from the start in consecutive chunks of `memory` integers (the last chunk may be shorter). Sort each chunk ascending and write it out as a *run*. The runs keep the order of the chunks they came from.
2. **Merge passes (stages 1, 2, …).** While more than one run exists, do one pass: split the current runs, in order, into consecutive groups of `memory − 1` runs (one memory slot is reserved for the output buffer; the last group may be smaller). Merge each group into a single sorted run with a k-way merge. The merged runs, in group order, are the runs of the next stage.

Return an array `stages` where `stages[s]` is the list of runs after stage `s`. The last stage holds a single run: the sorted file. If the file is empty, the answer is `[[]]` (stage 0 with no runs, and no merge passes).

**Example 1**
Input: file = [5,1,9,3,7,2,8,6,4], memory = 3
Output: [[[1,5,9],[2,3,7],[4,6,8]],[[1,2,3,5,7,9],[4,6,8]],[[1,2,3,4,5,6,7,8,9]]]
Why: chunks of 3 become three runs. Each merge pass can combine 3 − 1 = 2 runs, so pass 1 merges the first two runs and carries [4,6,8] over; pass 2 merges the remaining two.

**Example 2**
Input: file = [4,2,7], memory = 5
Output: [[[2,4,7]]]
Why: the whole file fits in memory, so one run is created and no merging is needed.

**Constraints**
- 0 ≤ file.length ≤ 10⁴
- −10⁹ ≤ file[i] ≤ 10⁹
- 3 ≤ memory ≤ 10⁴

**Notes**: in a real system each run is a temporary file and only the current head of each run is held in memory during a merge, so use a min-heap over the run heads rather than concatenating and re-sorting. Duplicates are kept. A group containing a single run is copied unchanged into the next stage.
