A sentence is a list of words separated by single spaces, with no leading or trailing spaces. Two sentences are *similar* if you can insert one contiguous run of words (possibly empty) into one of them, at any position, so that it becomes exactly the other. The inserted run must be separated from neighbouring words by spaces, so words cannot be split. Given `sentence1` and `sentence2`, return `true` if they are similar.

**Example 1**
Input: sentence1 = "My name is Haley", sentence2 = "My Haley"
Output: true
Why: inserting "name is" between "My" and "Haley" in sentence2 gives sentence1.

**Example 2**
Input: sentence1 = "of", sentence2 = "A lot of words"
Output: false
Why: one insertion cannot add words both before and after "of".

**Example 3**
Input: sentence1 = "Eating right now", sentence2 = "Eating"
Output: true
Why: insert "right now" at the end.

**Constraints**
- 1 ≤ sentence1.length, sentence2.length ≤ 10⁵
- sentences contain only upper- and lower-case English letters and single spaces between words
- comparison is case-sensitive
