Each node of an n-ary tree has a string `key`, an integer `value` and an ordered list of `children`. Within one node's children, keys are unique. Merge two such trees `A` and `B` into one:

- When two nodes are merged, the result has the same key, **B's value**, and children built as follows:
  1. go through A's children in order: if B's node has a child with the same key, merge that pair recursively; otherwise keep A's child (with its whole subtree) unchanged;
  2. then append B's children whose keys did **not** appear among A's children, in B's order, unchanged.
- The roots of A and B always have the same key and are merged with each other.
- If one tree is empty (`null`), the result is the other tree. If both are empty, return `null`.

**Input/output format**: a tree is written as `[key, value, [child1, child2, …]]` and an empty tree as `null`. Line 1 is A and line 2 is B. The `Node` class is provided:

```java
class Node {
    String key; int value; List<Node> children = new ArrayList<>();
    Node(String key, int value) { ... }
}
```

**Example 1**
Input:
A = ["root",1,[["a",2,[]],["b",3,[["x",4,[]]]]]]
B = ["root",10,[["b",30,[["y",5,[]]]],["c",6,[]]]]
Output: ["root",10,[["a",2,[]],["b",30,[["x",4,[]],["y",5,[]]]],["c",6,[]]]]
Why: "a" exists only in A and keeps its place. The two "b" nodes merge, take B's value 30, and get children x (from A) then y (B only). "c" exists only in B, so it goes last.

**Example 2**
Input:
A = null
B = ["r",1,[]]
Output: ["r",1,[]]

**Example 3**
Input:
A = ["r",1,[["k",1,[["m",1,[]]]]]]
B = ["r",2,[["j",0,[]],["k",2,[["n",3,[]],["m",9,[]]]]]]
Output: ["r",2,[["k",2,[["m",9,[]],["n",3,[]]]],["j",0,[]]]]
Why: A's child order comes first, so "k" comes before "j", and inside "k", "m" (from A) comes before "n".

**Constraints**
- each tree has at most 2·10⁴ nodes, with depth at most 500
- keys are non-empty strings of lowercase letters and digits; values fit in an `int`

**Notes**: you may build new nodes or reuse and modify the input nodes. Matching children by scanning B's list for every child of A costs O(children²) at nodes with many children. Use a map from key to child.
