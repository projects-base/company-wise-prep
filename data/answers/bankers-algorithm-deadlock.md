**Short answer:** The Banker's algorithm is a deadlock *avoidance* algorithm. Each process declares its maximum need up front; before granting a request, the OS pretends to grant it and checks whether the resulting state is *safe*, meaning some order exists in which every process can still finish. If the state is safe the request is granted, otherwise the process waits. The same "can everyone finish?" check, run with current requests instead of maximum needs, is the deadlock *detection* algorithm.

## Explanation

With `n` processes and `m` resource types the algorithm keeps four tables:

- `Available[m]` - free instances of each resource.
- `Max[n][m]` - the most each process may ever request.
- `Allocation[n][m]` - what each process holds now.
- `Need[n][m] = Max - Allocation` - what each process may still ask for.

**Safety check:**

1. `Work = Available`, `Finish[i] = false` for all `i`.
2. Find a process `i` with `Finish[i] == false` and `Need[i] <= Work` (element-wise). If none, go to step 4.
3. Pretend it runs to completion and releases everything: `Work += Allocation[i]`, `Finish[i] = true`. Go to step 2.
4. The state is safe if every `Finish[i]` is true. The order you picked them in is a *safe sequence*.

**Request check** for process `i` asking for `Request[i]`:

1. If `Request > Need[i]`, it is an error (it lied about its max).
2. If `Request > Available`, it must wait.
3. Otherwise tentatively apply: `Available -= Request`, `Allocation[i] += Request`, `Need[i] -= Request`. Run the safety check. Safe: keep it. Unsafe: roll back and make it wait.

The check costs O(m x n^2) in the simple form.

**Detection** is the same loop, but uses each process's *current outstanding request* in place of `Need`. Processes left with `Finish == false` at the end are deadlocked. A process holding nothing starts as finished.

## Example

5 processes, resources A B C, `Available = (3,3,2)`.

```text
      Alloc   Max     Need
P0    0 1 0   7 5 3   7 4 3
P1    2 0 0   3 2 2   1 2 2
P2    3 0 2   9 0 2   6 0 0
P3    2 1 1   2 2 2   0 1 1
P4    0 0 2   4 3 3   4 3 1

Work=(3,3,2): P1 fits (1,2,2) -> Work=(5,3,2)
              P3 fits (0,1,1) -> Work=(7,4,3)
              P4 fits (4,3,1) -> Work=(7,4,5)
              P0 fits (7,4,3) -> Work=(7,5,5)
              P2 fits (6,0,0) -> Work=(10,5,7)
Safe sequence: <P1, P3, P4, P0, P2>
```

## Pitfalls and follow-ups

- **Unsafe is not the same as deadlocked.** An unsafe state *may* lead to deadlock; the algorithm just refuses to take that risk.
- **Why is it rarely used in real OSes?** Processes don't know their max need, the number of processes changes, and the check runs on every request. Real systems prefer prevention (lock ordering) or detection plus recovery.
- **The four Coffman conditions?** Mutual exclusion, hold and wait, no preemption, circular wait. Prevention breaks one of them; avoidance (Banker's) keeps the system out of unsafe states.
- **Recovery after detection?** Kill a victim process, or preempt its resources and roll it back.
- **Single instance per resource?** A wait-for graph is enough; a cycle means deadlock.

Related: [B2 · Locks: synchronized, ReentrantLock, deadlock](../academy/lessons/B2.md).
