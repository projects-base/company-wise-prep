Build a fixed-capacity key-value cache that throws out the **least recently used** entry when it runs out of room. Implement the class `LRUCache`:

- `LRUCache(int capacity)` creates an empty cache that can hold at most `capacity` entries.
- `int get(int key)` returns the value stored for `key`, or `-1` if the key is not present. A successful lookup counts as a use of that key.
- `void put(int key, int value)` stores `value` under `key`, overwriting any previous value (this also counts as a use). If inserting a new key would make the cache hold more than `capacity` entries, first remove the entry that was used least recently.

Both operations must run in O(1) average time.

**Input format**: two lines — the list of operation names, then the list of argument lists (the first operation is always the constructor). The output is the list of return values, with `null` for the constructor and for `put`.

**Example 1**
Input:
["LRUCache","put","put","get","put","get","put","get","get","get"]
[[2],[1,1],[2,2],[1],[3,3],[2],[4,4],[1],[3],[4]]
Output: [null,null,null,1,null,-1,null,-1,3,4]
Why: `get(1)` makes key 1 recent, so `put(3,3)` evicts key 2. Then `put(4,4)` evicts key 1 (now the least recently used).

**Example 2**
Input:
["LRUCache","put","put","put","get","get"]
[[1],[1,10],[1,20],[2,30],[1],[2]]
Output: [null,null,null,null,-1,30]
Why: updating key 1 does not add an entry; inserting key 2 into a full cache of size 1 evicts key 1.

**Constraints**
- 1 ≤ capacity ≤ 3000
- 0 ≤ key ≤ 10⁴, 0 ≤ value ≤ 10⁵
- at most 2 · 10⁵ calls to `get` and `put`

**Notes**: write your code in the class `LRUCache` (keep that name). The hidden tests include a long sequence of operations on a large cache, so O(capacity) work per call will be noticeably slow.
