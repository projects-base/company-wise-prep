You are given `itemTags`, where `itemTags[i]` is the list of tags (attributes) of item `i`. Two items are **linked** if they share at least one tag, and links chain together: if item A shares a tag with B, and B shares a (possibly different) tag with C, then A, B and C all belong to the same group — this is the same idea as merging accounts that share an email address.

Return the groups as lists of item indices. An item with no tags, or whose tags no other item has, forms a group on its own.

The groups and the indices inside each group may be returned in **any order**; the checker sorts each group ascending and then sorts the groups by their smallest index.

**Example 1**
Input: itemTags = [["red","small"],["blue"],["small","round"],["round"],["green"]]
Output: [[0,2,3],[1],[4]]
Why: 0 and 2 share "small", 2 and 3 share "round".

**Example 2**
Input: itemTags = [["a"],["b"],["c","a"],["b","d"],[]]
Output: [[0,2],[1,3],[4]]

**Example 3**
Input: itemTags = [["x","x"],["x"]]
Output: [[0,1]]
Why: a tag may repeat inside one item; it still links the two items.

**Constraints**
- 1 ≤ itemTags.length ≤ 10⁴
- 0 ≤ itemTags[i].length ≤ 10
- tags are non-empty strings of lowercase letters and digits, at most 10 characters

**Notes**: comparing every pair of items is O(n²) and slow on the hidden tests; map each tag to an item instead.
