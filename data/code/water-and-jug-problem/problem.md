You have several jugs; `jugs[i]` is the capacity of jug `i` in litres, and all jugs start empty. You have an unlimited water supply, and the jugs have no markings. In one step you may:

- fill any jug to the top,
- empty any jug completely, or
- pour water from one jug into another until the first is empty or the second is full.

Return `true` if, after some sequence of steps, the jugs together contain **exactly** `target` litres (summed over all jugs).

**Example 1**
Input: jugs = [3,5], target = 4
Output: true
Why: writing the contents as (3-litre jug, 5-litre jug): fill the 5 → (0,5); pour into the 3 → (3,2); empty the 3 → (0,2); pour → (2,0); fill the 5 → (2,5); pour until the 3 is full → (3,4); empty the 3 → (0,4).

**Example 2**
Input: jugs = [2,6], target = 5
Output: false
Why: every amount you can produce is even.

**Example 3**
Input: jugs = [6,10,15], target = 1
Output: true
Why: every pair of these jugs shares a common factor, yet with all three together 1 litre can be measured.

**Constraints**
- 1 ≤ jugs.length ≤ 1000
- 1 ≤ jugs[i] ≤ 10⁶
- 0 ≤ target ≤ 10⁹

**Notes**: searching over states is far too slow for large capacities; look for a number-theory characterisation.
