Companies form **partnerships**. Each partnership `[x, y]` links two different companies directly, in both directions. Two companies are in the same **network** if you can get from one to the other by following partnerships (through any number of intermediate companies).

Implement the class `PartnerNetwork`:

- `PartnerNetwork(int[][] partnerships)` builds the structure from the list of partnerships.
- `boolean areConnected(int x, int y)` returns `true` if `x` and `y` are **direct** partners.
- `boolean areRelated(int x, int y)` returns `true` if `x` and `y` are in the same network but are **not** direct partners.

A company id that never appears in any partnership is alone in its own network. Queries always use two different ids.

**Input format**: two lines — the list of operation names, then the list of argument lists (the first operation is always the constructor, whose single argument is the partnership list). The output is the list of return values, with `null` for the constructor.

**Example 1**
Input:
["PartnerNetwork","areConnected","areRelated","areRelated","areConnected","areRelated"]
[[[[1,2],[2,3],[4,5]]],[1,2],[1,2],[1,3],[3,1],[1,4]]
Output: [null,true,false,true,false,false]
Why: 1–2 is a direct partnership, so they are connected but not "related". 1 and 3 share a network through 2 without a direct link. Company 4 is in a different network.

**Example 2**
Input:
["PartnerNetwork","areRelated","areConnected","areRelated"]
[[[[7,8],[8,7]]],[8,7],[8,7],[7,9]]
Output: [null,false,true,false]
Why: a duplicate partnership changes nothing, and company 9 has no partners at all.

**Constraints**
- 0 ≤ partnerships.length ≤ 10⁴
- 0 ≤ company id ≤ 10⁹, and the two ids of a partnership differ
- at most 10⁴ queries, each with x ≠ y

**Notes**: aim to answer each query in O(1) (average) after preprocessing — a fresh graph search per query will be slow on the hidden tests. Keep the class name `PartnerNetwork`.
