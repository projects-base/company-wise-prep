You must finish a list of jobs in the given order: job `i` can only start after every job before it is done. The jobs are spread over exactly `d` days, and every day must have at least one job. A day's difficulty is the largest difficulty among the jobs done that day, and the schedule's difficulty is the sum of the daily difficulties.

Given `jobDifficulty` and `d`, return the smallest possible schedule difficulty, or `-1` if the jobs cannot be spread over `d` days (fewer jobs than days).

**Example 1**
Input: jobDifficulty = [6,5,4,3,2,1], d = 2
Output: 7
Why: day 1 does the first five jobs (max 6), day 2 does the last job (max 1): 6 + 1 = 7.

**Example 2**
Input: jobDifficulty = [9,9,9], d = 4
Output: -1
Why: three jobs cannot fill four days.

**Example 3**
Input: jobDifficulty = [1,1,1], d = 3
Output: 3

**Constraints**
- 1 ≤ jobDifficulty.length ≤ 300
- 0 ≤ jobDifficulty[i] ≤ 1000
- 1 ≤ d ≤ 10

**Notes**: the jobs keep their order — each day takes a contiguous block of the list.
