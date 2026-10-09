**Short answer:** Do not generate permutations. With `n` numbers, each choice of first digit owns a block of `(n-1)!` permutations. So convert `k-1` to the factorial number system: the first digit index is `(k-1) / (n-1)!`, then recurse on the remainder with the remaining digits. That is O(n²) with a list, trivial for n ≤ 9.

## Approach

- **Brute force.** Call "next permutation" `k - 1` times, or generate all `n!` permutations in order. Up to 9! = 362,880 permutations of length 9 works, but it misses the point and the interviewer wants the direct method.
- **Key insight.** Sorted permutations of `1..n` group by first digit. Permutations starting with `1` are ranks 0 to `(n-1)! - 1`, starting with `2` are the next `(n-1)!`, and so on. So the first digit is the `((k-1) / (n-1)!)`-th smallest unused number. The remainder `(k-1) % (n-1)!` is the rank inside that block, and the same reasoning applies to the next position.
- **Optimal.** Use 0-based rank `k - 1`. For each position from left to right, divide by the factorial of the remaining length, pick and remove that index from the list of unused numbers, keep the remainder.

Worked example, n = 4, k = 9: rank 8. 3! = 6, 8 / 6 = 1, pick the 2nd of [1,2,3,4] = 2, rank 2. 2! = 2, 2 / 2 = 1, pick the 2nd of [1,3,4] = 3, rank 0. Then 1 and 4. Answer "2314".

## Solution

```java
import java.util.*;

class Solution {
    public String getPermutation(int n, int k) {
        int[] fact = new int[n + 1];
        fact[0] = 1;
        for (int i = 1; i <= n; i++) fact[i] = fact[i - 1] * i;
        List<Integer> left = new ArrayList<>();
        for (int i = 1; i <= n; i++) left.add(i);
        k--; // 0-based rank
        StringBuilder sb = new StringBuilder();
        for (int i = n; i >= 1; i--) {
            int idx = k / fact[i - 1];
            k %= fact[i - 1];
            sb.append(left.remove(idx)); // remove(int index), not remove(Object)
        }
        return sb.toString();
    }
}
```

## Complexity

- **Time:** O(n²) because `ArrayList.remove(index)` shifts elements. For n ≤ 9 that is nothing. With large n, a Fenwick tree finding the k-th unused element brings it to O(n log n).
- **Space:** O(n).

## Edge cases

- `k = 1`: the sorted sequence "12…n".
- `k = n!`: the reversed sequence "n…21".
- `n = 1`: "1".
- Forgetting `k--` gives an off-by-one at every block boundary. Test `n = 3, k = 2` ("132") and `k = 3` ("213").

## Variations

- **Inverse problem (rank of a given permutation):** for each position, count unused numbers smaller than the current digit and multiply by the factorial of the remaining length (Lehmer code).
- **Next Permutation (LC 31):** the in-place step-by-step version.
- **Large n:** factorials overflow `int` at 13! and `long` at 21!; use `BigInteger` or cap at the needed length.

Practise it in the app: Run / Submit on this page.
