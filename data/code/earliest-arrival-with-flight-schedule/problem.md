A package must travel from airport `source` to airport `destination`. Airports are numbered `0` to `n − 1`. Each flight is `flights[i] = [from, to, departure, arrival]`: it leaves `from` at time `departure` and lands at `to` at time `arrival` (always `departure < arrival`).

The package is ready at `source` at time `startTime`. It can be put on a flight leaving airport `a` only if the flight departs **at or after** the time the package is at `a` (`departure ≥ time at a`; zero layover is fine). Return the **earliest time** the package can be at `destination`, or `-1` if it can never get there. If `source == destination`, the answer is `startTime`.

**Example 1**
Input: n = 4, flights = [[0,1,1,4],[1,2,5,7],[0,2,2,9],[2,3,8,10],[2,3,10,12]], source = 0, destination = 3, startTime = 0
Output: 10
Why: 0 → 1 (lands at 4), 1 → 2 (leaves at 5, lands at 7), 2 → 3 (leaves at 8, lands at 10). The direct 0 → 2 flight lands too late for the 8 o'clock departure.

**Example 2**
Input: n = 3, flights = [[0,1,5,6],[1,2,3,4]], source = 0, destination = 2, startTime = 0
Output: -1
Why: the only flight out of airport 1 leaves at 3, before the package arrives there at 6.

**Example 3**
Input: n = 2, flights = [[0,1,1,2]], source = 0, destination = 1, startTime = 2
Output: -1
Why: the package is only ready at time 2, after the flight has left.

**Constraints**
- 1 ≤ n ≤ 10⁴
- 0 ≤ flights.length ≤ 10⁴
- 0 ≤ from, to < n
- 0 ≤ departure < arrival ≤ 10⁹
- 0 ≤ startTime ≤ 10⁹

**Notes**: the earliest arrival is not always reached with the fewest flights. Aim for about O(F log F).
