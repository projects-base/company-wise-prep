**Short answer:** Each token is its own Nim heap. A token on node `i` can be moved to any later node, and it is dead on the last node, so it behaves exactly like a Nim heap of size `n − 1 − i` that you may shrink to any smaller size. The whole game is the sum of independent games, so by Sprague-Grundy the first player wins iff the XOR of all heap sizes is non-zero. Two tokens on the same node cancel, so only nodes with an odd token count matter.

## Approach

- **Brute force:** memoised game search over token distributions. The state space explodes and token counts reach 10⁹, so this only works for toy inputs. It is useful to confirm small cases (Example 2 and 3).
- **Insight 1: one token alone.** Label a token by its distance to the end, `d = n − 1 − i`. A move sends it to some `j > i`, giving distance `n − 1 − j`, which can be any value from `0` to `d − 1`. At `d = 0` there is no move. That is precisely a Nim heap of size `d`.
- **Insight 2: tokens do not interact.** A move touches one token only, and a token's options never depend on where other tokens are (any number may share a node). So the position is a disjunctive sum of Nim heaps. Its Grundy value is the XOR of the heap sizes.
- **Insight 3: pairs cancel.** `x ^ x = 0`, so `tokens[i]` copies of heap `d` contribute `d` if `tokens[i]` is odd, else nothing. That removes the 10⁹ factor.

## Solution

```java
class Solution {
    public boolean firstPlayerWins(int[] tokens) {
        int n = tokens.length, x = 0;
        for (int i = 0; i < n; i++) {
            if ((tokens[i] & 1) == 1) x ^= n - 1 - i;   // heap of size n-1-i
        }
        return x != 0;
    }
}
```

Check Example 3, `[1,1,0]`: heaps of size 2 and 1, `2 ^ 1 = 3 ≠ 0`, first player wins. The winning move turns the XOR to 0: shrink heap 2 to 1 (move the token from node 0 to node 1), leaving `1 ^ 1 = 0`.

## Complexity

- **Time O(n):** one pass.
- **Space O(1).**

## Edge cases

- All tokens on the last node, or no tokens: XOR is 0, first player loses (cannot move).
- `n = 1`: the only node is the last node; always a loss.
- Even counts everywhere: XOR 0, loss (Example 2).

## Variations

- "Staircase Nim" is a different game (moving any number of coins one step down); do not confuse the two. Here one token moves any distance, which is what makes each token an ordinary Nim heap.
- If a move could take several tokens at once from node `i`, tokens would no longer be independent and the analysis changes; ask the interviewer to confirm "one token per move".

Practise it in the app: Run / Submit on this page.
