Houses and heaters stand at integer positions on a straight line. Every heater has the same warming radius `r`: it warms every house within distance `r` of it. Given the positions of the `houses` and of the `heaters` (neither list is necessarily sorted), return the smallest radius `r` that warms every house.

**Example 1**
Input: houses = [1,2,3], heaters = [2]
Output: 1
Why: a heater at 2 with radius 1 reaches positions 1 to 3.

**Example 2**
Input: houses = [1,2,3,4], heaters = [1,4]
Output: 1

**Example 3**
Input: houses = [1,5], heaters = [2]
Output: 3
Why: the house at 5 is 3 away from the only heater.

**Constraints**
- 1 ≤ houses.length, heaters.length ≤ 3 · 10⁴
- 1 ≤ houses[i], heaters[i] ≤ 10⁹
- positions may repeat, and a house and a heater may share a position

**Notes**: the hidden tests include 10,000 houses and 10,000 heaters, so comparing every house with every heater (O(n·m)) is slow. Sort, then use binary search or two pointers.
