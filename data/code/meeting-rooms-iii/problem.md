There are `n` rooms numbered `0` to `n − 1`. You get a list of meetings, where `meetings[i] = [start, end]` is the half-open interval `[start, end)`. All start times are distinct. Meetings are handed out in order of their **original** start time using these rules:

1. A meeting goes to the **lowest-numbered** room that is free at its start time.
2. If no room is free, the meeting waits until a room frees up. It then takes the room that frees up **earliest** (lowest number on a tie) and keeps its **original duration**, so it now ends at `freeTime + (end − start)`.
3. When several meetings are waiting, the one with the earlier original start goes first.

Return the number of the room that hosted the most meetings. On a tie, return the lowest room number.

**Example 1**
Input: n = 2, meetings = [[0,10],[1,5],[2,7],[3,4]]
Output: 0
Why: [0,10] → room 0, [1,5] → room 1. [2,7] waits for room 1 and runs [5,10). [3,4] waits and takes room 0 at 10, running [10,11). Both rooms host 2 meetings, so the answer is 0.

**Example 2**
Input: n = 3, meetings = [[1,20],[2,10],[3,5],[4,9],[6,8]]
Output: 1
Why: rooms 1 and 2 each host 2 meetings, and room 1 is the lower number.

**Constraints**
- 1 ≤ n ≤ 100
- 1 ≤ meetings.length ≤ 10⁵
- 0 ≤ start < end ≤ 5·10⁵, all start values distinct

**Notes**: delayed end times can exceed the `int` range, so use `long` for times. The hidden tests include 100,000 meetings.
