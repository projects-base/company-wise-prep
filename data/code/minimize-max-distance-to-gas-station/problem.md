Gas stations sit at the integer positions `stations[0] < stations[1] < ... < stations[n-1]` along a straight road. You may build exactly `k` new stations, each at any real-valued position (not necessarily an integer). After building them, look at the largest gap between two neighbouring stations. Return the smallest value that this largest gap can be made.

**Example 1**
Input: stations = [1,2,3,4,5,6,7,8,9,10], k = 9
Output: 0.50000
Why: put one new station in the middle of each of the nine gaps of length 1.

**Example 2**
Input: stations = [23,24,36,39,46,56,57,65,84,98], k = 1
Output: 14.00000
Why: the widest gap is 84→98 (14) and the second widest is 65→84 (19); the single station goes into the 19-gap, splitting it into two 9.5s, so 14 remains the largest.

**Constraints**
- 2 ≤ stations.length ≤ 2000
- 0 ≤ stations[i] ≤ 10⁸, strictly increasing
- 0 ≤ k ≤ 10⁶

**Notes**: the result is printed rounded to 5 decimal places, so any answer within 10⁻⁶ of the true optimum is accepted. With k up to 10⁶, adding stations one at a time by scanning every gap is too slow; binary search on the answer (or a heap) is expected.
