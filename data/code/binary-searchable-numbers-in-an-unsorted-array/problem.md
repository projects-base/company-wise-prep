Consider this randomised search for a `target` in a sequence that is **not necessarily sorted**:

```
while the sequence is not empty:
    pick any element of the sequence as the pivot (uniformly at random)
    if pivot == target: return true
    if pivot < target: remove the pivot and everything to its left
    else:              remove the pivot and everything to its right
return false
```

On a sorted sequence this always finds the target; on an unsorted one it may throw the target away. Given an array `nums` of **distinct** integers, return how many of its values are guaranteed to be found when searched for, whichever pivots are picked.

**Example 1**
Input: nums = [7]
Output: 1
Why: the only possible pivot is 7 itself.

**Example 2**
Input: nums = [-1,5,2]
Output: 1
Why: -1 is always found. Searching for 5 fails if 2 is picked first (2 < 5, so 2 and everything to its left — including 5 — is removed). Searching for 2 fails if 5 is picked first (5 > 2 removes 5 and everything to its right, including 2).

**Example 3**
Input: nums = [1,3,2,4]
Output: 2
Why: 1 and 4 are always found; 3 and 2 can each be thrown away by picking the other one first.

**Constraints**
- 1 ≤ nums.length ≤ 10⁵
- −10⁵ ≤ nums[i] ≤ 10⁵
- all values are distinct

**Notes**: the hidden tests include 30,000 numbers, so checking each element against all others (O(n²)) is slow. An O(n) solution exists. Interview follow-up (not tested): what changes if duplicates are allowed?
