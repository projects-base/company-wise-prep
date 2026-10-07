A log store holds entries grouped by category, and `counts[i]` is the number of entries in category `i`. To save space it applies one cap `X` to every category:

- a category with at most `X` entries keeps all of them;
- a category with more than `X` entries keeps only its latest `X` entries.

So the store retains `min(counts[0], X) + min(counts[1], X) + …` entries in total. Return the **largest** integer `X ≥ 0` for which the total retained is at most `maxEntries`. If everything already fits (the sum of all counts is ≤ `maxEntries`), no cap is needed; return `-1`.

**Example 1**
Input: counts = [5,3,8,2], maxEntries = 12
Output: 3
Why: with X = 3 the store keeps 3 + 3 + 3 + 2 = 11 ≤ 12. With X = 4 it keeps 4 + 3 + 4 + 2 = 13 > 12.

**Example 2**
Input: counts = [4,4], maxEntries = 8
Output: -1
Why: all 8 entries fit, so no cap is needed.

**Example 3**
Input: counts = [10,10,10], maxEntries = 2
Output: 0
Why: even X = 1 keeps 3 entries, so only X = 0 works.

**Constraints**
- 1 ≤ counts.length ≤ 10⁵
- 1 ≤ counts[i] ≤ 10⁹
- 0 ≤ maxEntries ≤ 10¹⁴

**Notes**: the total retained only grows as X grows, so you can binary search on X, or sort the counts and sweep. Increasing X one step at a time is far too slow when counts reach 10⁹. Totals need a `long`.
