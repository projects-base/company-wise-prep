You have `tasks` identical jobs, each taking exactly `duration` time units, and a set of machines. Machine `j` becomes available at time `available[j]`. From then on it can run jobs one after another, one at a time, with no gaps needed. A job cannot be split or moved between machines once it starts.

Return two numbers `[T, M]`:

1. `T`: the earliest time by which all jobs can be finished using any of the machines;
2. `M`: the **fewest** machines that can still finish all jobs by time `T`. You choose which machines to use.

**Example 1**
Input: available = [0,2,5], tasks = 5, duration = 3
Output: [8,3]
Why: by time 8 the machines can finish 2, 2 and 1 jobs (machine 0 runs [0,3) and [3,6), and so on). That is 5 in total, and by time 7 they can finish only 3. No two machines can do 5 jobs by time 8, so all 3 are needed.

**Example 2**
Input: available = [0,0,10], tasks = 4, duration = 5
Output: [10,2]
Why: machines 0 and 1 each finish 2 jobs by time 10. Machine 2 only starts at 10, so it is not needed.

**Example 3**
Input: available = [3], tasks = 1, duration = 7
Output: [10,1]

**Constraints**
- 1 ≤ available.length ≤ 10⁵
- 0 ≤ available[j] ≤ 10⁹
- 1 ≤ tasks ≤ 10⁹
- 1 ≤ duration ≤ 10⁴

**Notes**: T can be as large as about 10¹³, so use `long`. Assigning jobs one at a time is too slow when `tasks` is 10⁹. Binary search on T instead: by time T, machine j can finish ⌊(T − available[j]) / duration⌋ jobs, or 0 if it is not yet available.
