There are `n` jobs. Job `i` runs from `startTime[i]` to `endTime[i]` and pays `profit[i]`. Choose a set of jobs in which no two overlap in time, and return the largest total profit you can earn. A job that ends at time `X` does not overlap a job that starts at time `X`.

**Example 1**
Input: startTime = [1,2,3,3], endTime = [3,4,5,6], profit = [50,10,40,70]
Output: 120
Why: take the jobs [1,3] and [3,6] for 50 + 70.

**Example 2**
Input: startTime = [1,2,3,4,6], endTime = [3,5,10,6,9], profit = [20,20,100,70,60]
Output: 150
Why: take [1,3], [4,6] and [6,9] for 20 + 70 + 60.

**Example 3**
Input: startTime = [1,1,1], endTime = [2,3,4], profit = [5,6,4]
Output: 6

**Constraints**
- 1 ≤ n ≤ 5·10⁴
- 1 ≤ startTime[i] < endTime[i] ≤ 10⁹
- 1 ≤ profit[i] ≤ 10⁴

**Notes**: the answer fits in an `int`. Aim for O(n log n); the hidden tests include 50,000 jobs.
