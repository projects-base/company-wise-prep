Implement the class `RandomizedSet`, a set of integers where every operation runs in **O(1) average time**:

- `RandomizedSet()` creates an empty set.
- `boolean insert(int val)` adds `val` if it is not already present. Returns `true` if it was added, `false` if it was already there.
- `boolean remove(int val)` removes `val` if present. Returns `true` if it was removed, `false` if it was not there.
- `int getRandom()` returns one of the current elements, chosen **uniformly at random** (every element equally likely). It is only called when the set is non-empty.

**Input format**: two lines — the list of operation names, then the list of argument lists (the first operation is always the constructor). The output is the list of return values, with `null` for the constructor.

Because `getRandom` is random, the checker does not print the value it returned. Instead it prints `true` when the value was a current element of the set (and the value itself otherwise). On tests with many `getRandom` calls, the checker also counts how often each element came back; if the counts are far from uniform it appends a message saying so.

**Example 1**
Input:
["RandomizedSet","insert","remove","insert","getRandom","remove","insert","getRandom"]
[[],[1],[2],[2],[],[1],[2],[]]
Output: [null,true,false,true,true,true,false,true]
Why: after inserting 1 and 2, getRandom returns 1 or 2 (either is correct, shown as `true`). Then 1 is removed and inserting 2 again fails; the last getRandom must return 2.

**Example 2**
Input:
["RandomizedSet","insert","insert","remove","remove","insert","getRandom"]
[[],[-5],[-5],[-5],[-5],[7],[]]
Output: [null,true,false,true,false,true,true]

**Constraints**
- −2³¹ ≤ val ≤ 2³¹ − 1
- at most 2 · 10⁵ calls in total
- `getRandom` is only called on a non-empty set

**Notes**: keep the class name `RandomizedSet`. A hash set alone cannot pick a uniform random element in O(1) — think about what else you can store alongside it.
