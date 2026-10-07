There are `n` nodes in a row, numbered `0` to `n − 1`, and node `i` starts with `tokens[i]` tokens. Two players take turns; the first player moves first. A move is: pick any node `i` that has at least one token and any node `j` with `j > i`, and move **one** token from `i` to `j`. A player who cannot make a move (all tokens are on the last node) **loses**.

Both players play perfectly. Return `true` if the first player wins, `false` otherwise.

**Example 1**
Input: tokens = [1,0]
Output: true
Why: the first player moves the token to node 1, and the second player has no move.

**Example 2**
Input: tokens = [2,0]
Output: false
Why: each move sends one token to node 1. The first player moves one, the second player moves the other, and the first player is stuck.

**Example 3**
Input: tokens = [1,1,0]
Output: true
Why: move the token on node 0 to node 1, giving [0,2,0]. Nodes 1 and 2 now look exactly like Example 2 with the opponent to move, so the opponent loses.

**Constraints**
- 1 ≤ n ≤ 10⁵
- 0 ≤ tokens[i] ≤ 10⁹

**Notes**: the game can last for an astronomically long time, so simulation or a search over positions is hopeless for big inputs. Think about how a single token behaves on its own, then how independent tokens combine.
