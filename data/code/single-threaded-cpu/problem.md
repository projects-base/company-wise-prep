You are given `n` tasks numbered `0` to `n − 1`; `tasks[i] = [enqueueTime, processingTime]` says task `i` becomes available at time `enqueueTime` and needs `processingTime` units of uninterrupted CPU time. A single CPU runs them under these rules:

- If the CPU is idle and no task is available, it waits until the next task becomes available.
- If the CPU is idle and tasks are available, it starts the available task with the **shortest processing time**; ties go to the **smallest index**.
- Once started, a task runs to completion. The CPU can pick the next task at the very moment the previous one finishes (tasks that become available at that moment count as available).

Return the order in which the CPU processes the tasks.

**Example 1**
Input: tasks = [[1,2],[2,4],[3,2],[4,1]]
Output: [0,2,3,1]
Why: at time 1 only task 0 is available; it ends at 3. Tasks 1 and 2 are waiting, task 2 is shorter and ends at 5. Then task 3 (length 1) beats task 1, ending at 6, and task 1 runs last.

**Example 2**
Input: tasks = [[7,10],[7,12],[7,5],[7,4],[7,2]]
Output: [4,3,2,0,1]

**Constraints**
- 1 ≤ n ≤ 10⁵
- 1 ≤ enqueueTime, processingTime ≤ 10⁹

**Notes**: the clock can exceed the range of `int`. The hidden tests include 10,000 tasks.
