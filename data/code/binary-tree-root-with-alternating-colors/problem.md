You are given an undirected tree with `n` nodes numbered `0` to `n-1` (`edges` holds its `n − 1` edges) in which **every node has at most 3 neighbours**. Each node is painted a colour: `colors[i]` is the colour of node `i`, written as an uppercase letter. You are also given a colour `pattern` — a string of distinct uppercase letters.

Choosing a node `r` as the root turns the tree into a rooted tree. Call `r` a **good root** when both hold:

1. the rooted tree is a **binary tree** — every node has at most 2 children. (Since every node has at most 3 neighbours, this only requires `r` itself to have at most 2 neighbours.)
2. the colours follow the pattern by depth: every node at depth `d` (the root has depth 0) has colour `pattern[d mod pattern.length]`.

Return all good roots in increasing order (an empty list if there are none).

This combines the interview question and its follow-ups: with a one-letter pattern and every node painted that colour, it is "find a root that makes the tree binary"; `"BW"` is the black/white alternating-by-depth version; `"RGB"` is the three-colour repeating sequence. Checking every candidate root with its own traversal is O(n²) — find an O(n) method.

**Example 1**
Input: n = 5, edges = [[0,1],[1,2],[1,3],[3,4]], colors = "BWBBW", pattern = "BW"
Output: [0,2,3]
Why: neighbouring nodes always differ in colour, so any black node with at most 2 neighbours works. Nodes 1 and 4 are white, and a root must have colour `pattern[0]` = B.

**Example 2**
Input: n = 4, edges = [[0,1],[1,2],[1,3]], colors = "RGBB", pattern = "RGB"
Output: [0]
Why: from root 0 the depths are 0, 1, 2, 2 — colours R, G, B, B as required. Node 1 has three neighbours, and nodes 2 and 3 are not red.

**Example 3**
Input: n = 4, edges = [[0,1],[1,2],[1,3]], colors = "GRGG", pattern = "RG"
Output: []
Why: the only red node is 1, but rooted there it would have three children.

**Constraints**
- 1 ≤ n ≤ 12000
- every node has at most 3 neighbours; the edges form a tree
- colors.length = n; colors and pattern use uppercase letters; 1 ≤ pattern.length ≤ 26 with distinct letters

**Notes**: the result may be printed in any order — it is sorted before comparing. A colour that does not occur in `pattern` can never be at a valid depth, so it rules out every root. A single node (n = 1) is a good root if its colour is `pattern[0]`.
