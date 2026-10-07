Design a key–value cache with a fixed capacity that evicts the **least frequently used** entry when it is full. Implement the class `LFUCache`:

- `LFUCache(int capacity)` — creates an empty cache that can hold `capacity` entries.
- `int get(int key)` — returns the value stored for `key`, or `-1` if the key is not in the cache.
- `void put(int key, int value)` — stores `value` under `key`, replacing the old value if the key is already present. If the key is new and the cache already holds `capacity` entries, first remove one entry: the one with the smallest use count; if several share that smallest count, remove the one whose last use is the oldest. Then insert the new key.

Every `get` of a key that is present and every `put` (insert or update) counts as one use of that key. A newly inserted key starts with a use count of 1. When a key is evicted its count is forgotten. Both operations should run in O(1) average time.

**Example 1**
Input:
["LFUCache","put","put","get","put","get","get","put","get","get","get"]
[[2],[1,1],[2,2],[1],[3,3],[2],[3],[4,4],[1],[3],[4]]
Output: [null,null,null,1,null,-1,3,null,-1,3,4]
Why: after get(1), key 1 has count 2 and key 2 has count 1, so put(3,3) evicts key 2. Then get(3) gives keys 1 and 3 both count 2; key 1 was used longer ago, so put(4,4) evicts key 1.

**Example 2**
Input:
["LFUCache","put","get","put","get","get"]
[[1],[2,1],[2],[3,2],[2],[3]]
Output: [null,null,1,null,-1,2]

**Constraints**
- 1 ≤ capacity ≤ 10⁴
- 0 ≤ key ≤ 10⁵, 0 ≤ value ≤ 10⁹
- at most 2 × 10⁵ calls to `get` and `put`

**Notes**: the input is two lines — the method names, then their argument lists. The output lists each call's return value, with `null` for the constructor and for `put`. (Interview reports describe a variant with score-based eviction; this challenge uses the standard LFU rules above.)
