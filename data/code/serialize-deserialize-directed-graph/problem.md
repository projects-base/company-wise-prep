Design a `Codec` that turns a directed graph into a `String` and back. Each `Node` has an integer `val` and an ordered list `neighbors` of outgoing edges. The graph may contain cycles, self-loops and several nodes with the same value, so a node is identified by the object itself, not by its value. `serialize(root)` encodes every node reachable from `root`; `deserialize(data)` must build a **new** graph of fresh `Node` objects with exactly the same shape: same values, same edges, and the same order inside every `neighbors` list. An empty graph is `root = null`.

```java
class Node {            // provided by the judge, do not redeclare it
    int val;
    List<Node> neighbors = new ArrayList<>();
    Node(int val) { this.val = val; }
}
```

**How the tests work**: the input gives `values` (node `i` has value `values[i]`) and `adjacency` (`adjacency[i]` lists the indices node `i` points to, in order); node 0 is the root. The judge calls `serialize` on one `Codec`, then **scrambles the original graph**, then calls `deserialize` on a second `Codec`. It prints the rebuilt graph by walking it breadth-first from the root, numbering nodes in the order they are first seen, as `[[val,[neighbour numbers…]],…]`.

**Example 1**
Input: values = [1,2,3,4], adjacency = [[1,3],[2],[0,3],[]]
Output: [[1,[1,2]],[2,[3]],[4,[]],[3,[0,2]]]
Why: the rebuilt graph matches the original. The node with value 4 is the root's second neighbour, so the walk numbers it 2.

**Example 2**
Input: values = [7,7], adjacency = [[1,0],[0]]
Output: [[7,[1,0]],[7,[0]]]
Why: two different nodes share the value 7 and must stay different.

**Example 3**
Input: values = [], adjacency = []
Output: []
Why: the empty graph serialises to a string that deserialises back to `null`.

**Constraints**
- 0 ≤ number of nodes ≤ 10⁴, 0 ≤ number of edges ≤ 3·10⁴
- −10⁹ ≤ val ≤ 10⁹
- edges may form cycles and self-loops; a node never lists the same neighbour twice
- nodes not reachable from the root are ignored
- `deserialize` must return fresh nodes and can rely only on the string (the original graph is changed before it is called)
