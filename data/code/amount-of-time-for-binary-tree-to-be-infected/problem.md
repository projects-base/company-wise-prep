A fire starts at minute 0 at the node of a binary tree whose value is `start`. Each minute, the fire spreads from every burning node to its unburnt neighbours — its left child, its right child and its parent. All node values are distinct. Return the number of minutes until the whole tree is burning.

**Example 1**
Input: root = [1,5,3,null,4,10,6,9,2], start = 3
Output: 4
Why: minute 1 burns 1, 10 and 6; minute 2 burns 5; minute 3 burns 4; minute 4 burns 9 and 2.

**Example 2**
Input: root = [1], start = 1
Output: 0

**Constraints**
- 1 ≤ number of nodes ≤ 10⁵
- 1 ≤ Node.val ≤ 10⁵, all values distinct
- a node with value `start` exists

**Notes**: nodes do not store parent pointers here — you have to account for the fire moving upward yourself. The hidden tests include a tree with 12,000 nodes.
