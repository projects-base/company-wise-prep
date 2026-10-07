You are given facts of the form `A / B = k`: `equations[i] = [A, B]` and `values[i] = k`, where every name stands for an unknown positive real number. For each query `[C, D]` in `queries`, work out `C / D` from the facts, or return `-1.0` if it cannot be determined.

A query cannot be determined when either name never appears in the equations, or when the two names are not linked by any chain of facts. A name divided by itself is `1.0` — but only if that name appears in some equation.

The facts never contradict each other and no value is zero. Answers are printed with 5 decimal places.

**Example 1**
Input: equations = [["a","b"],["b","c"]], values = [2.0,3.0], queries = [["a","c"],["b","a"],["a","e"],["a","a"],["x","x"]]
Output: [6.00000,0.50000,-1.00000,1.00000,-1.00000]
Why: a/c = (a/b)·(b/c) = 6; b/a = 1/2; e and x are unknown names.

**Example 2**
Input: equations = [["a","b"],["b","c"],["bc","cd"]], values = [1.5,2.5,5.0], queries = [["a","c"],["c","b"],["bc","cd"],["cd","bc"]]
Output: [3.75000,0.40000,5.00000,0.20000]

**Example 3**
Input: equations = [["a","b"]], values = [0.5], queries = [["a","b"],["b","a"],["a","c"],["x","y"]]
Output: [0.50000,2.00000,-1.00000,-1.00000]

**Constraints**
- 1 ≤ equations.length ≤ 20, 1 ≤ queries.length ≤ 20
- names are 1–5 lowercase letters or digits
- 0.0 < values[i] ≤ 20.0
