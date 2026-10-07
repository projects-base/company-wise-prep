List all permutations of the numbers `1, 2, …, n` in increasing lexicographic order and number them from 1. Return the `k`-th permutation as a string of digits.

For n = 3 the list is "123", "132", "213", "231", "312", "321".

**Example 1**
Input: n = 3, k = 3
Output: "213"

**Example 2**
Input: n = 4, k = 9
Output: "2314"

**Example 3**
Input: n = 3, k = 1
Output: "123"

**Constraints**
- 1 ≤ n ≤ 9
- 1 ≤ k ≤ n!

**Notes**: you can work out each digit directly with factorials instead of generating the permutations one by one.
