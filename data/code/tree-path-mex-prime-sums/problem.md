For a set of positive integers `S`, define **MEX-P(S)** as the smallest prime `p` such that at least one element of `S` is *not* divisible by `p`. For example, MEX-P({6, 12}) = 5 (both are divisible by 2 and 3, but not by 5), and MEX-P({3}) = 2.

You are given a tree with `n` nodes labelled `0..n-1`, where node `i` has value `values[i]` and `edges` lists the `n − 1` edges. For two nodes `u` and `v`, let `path(u, v)` be the set of values on the unique path between them, both ends included (`path(u, u)` is just `{values[u]}`). For every node `u`, compute

`answer[u] = Σ over all nodes v (including v = u) of MEX-P(path(u, v))`

and return the array `answer`.

**Example 1**
Input: values = [6,2,3], edges = [[0,1],[0,2]]
Output: [10,8,6]
Why: for u = 0, the paths give MEX-P({6}) = 5, MEX-P({6,2}) = 3 and MEX-P({6,3}) = 2, which add up to 10.

**Example 2**
Input: values = [30,30,7], edges = [[0,1],[1,2]]
Output: [16,16,6]
Why: for u = 0: MEX-P({30}) = 7, MEX-P({30,30}) = 7, MEX-P({30,30,7}) = 2.

**Example 3**
Input: values = [1], edges = []
Output: [2]

**Constraints**
- 1 ≤ n ≤ 10⁵
- 1 ≤ values[i] ≤ 10⁹
- `edges` form a tree

**Notes**: checking every pair of nodes is O(n²) and too slow on the hidden tests. Hint: think about which primes can possibly be the answer when values are at most 10⁹.
