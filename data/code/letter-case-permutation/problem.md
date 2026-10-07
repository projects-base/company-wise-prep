You are given a string `s` made of English letters and digits. Each letter may be written either in lowercase or in uppercase; digits stay as they are. Return every distinct string you can produce this way. The strings may be returned in any order.

**Example 1**
Input: s = "a1b2"
Output: ["A1B2","A1b2","a1B2","a1b2"]
Why: two letters, two choices each → four strings.

**Example 2**
Input: s = "3z4"
Output: ["3Z4","3z4"]

**Constraints**
- 1 ≤ s.length ≤ 12
- s contains only English letters (either case) and digits

**Notes**: any order is accepted — the checker sorts your list (plain string order, so uppercase sorts before lowercase) before comparing. A string with no letters yields just itself.
