**Short answer:** A permutation of `s1` is any window of `s2` with length `|s1|` and the same letter counts. Slide a fixed-size window over `s2`, keeping a 26-entry count difference between `s1` and the window. Track how many letters are out of balance, so each step is O(1). Total time O(|s1| + |s2|), space O(1).

## Approach

- **Brute force (the red flag).** Generate all permutations of `s1` and search for each in `s2`. That is O(|s1|!) and fails immediately. Interviewers expect you to reject it out loud.
- **Better.** For every start index in `s2`, count letters of the window and compare with `s1`: O(26 · |s2|) if you rebuild counts smartly, O(|s1| · |s2|) if you rebuild them naively.
- **Key insight.** Two strings are permutations of each other exactly when their letter counts match. Adjacent windows differ by one letter in and one letter out, so the counts can be updated in O(1).
- **Optimal.** Keep `diff[c] = count in s1 - count in window`. Keep `nonZero`, the number of letters whose diff is not zero. The window is a permutation when `nonZero == 0`. Each slide changes two entries and adjusts `nonZero` accordingly, so you never scan all 26 letters.

## Solution

```java
class Solution {
    public boolean checkInclusion(String s1, String s2) {
        int n = s1.length(), m = s2.length();
        if (n > m) return false;
        int[] diff = new int[26];
        for (int i = 0; i < n; i++) {
            diff[s1.charAt(i) - 'a']++;
            diff[s2.charAt(i) - 'a']--;
        }
        int nonZero = 0;
        for (int d : diff) if (d != 0) nonZero++;
        if (nonZero == 0) return true;
        for (int i = n; i < m; i++) {
            nonZero += change(diff, s2.charAt(i) - 'a', -1);     // letter enters the window
            nonZero += change(diff, s2.charAt(i - n) - 'a', +1); // letter leaves the window
            if (nonZero == 0) return true;
        }
        return false;
    }

    /** Applies delta to diff[c] and returns how the count of non-zero entries changed. */
    private int change(int[] diff, int c, int delta) {
        int before = diff[c] != 0 ? 1 : 0;
        diff[c] += delta;
        int after = diff[c] != 0 ? 1 : 0;
        return after - before;
    }
}
```

A simpler version compares the two 26-element arrays with `Arrays.equals` on every step. That is O(26 · |s2|), which is also fine and easier to write under time pressure. Mention the `nonZero` counter as the refinement.

## Complexity

- **Time:** O(|s1| + |s2|). Each character enters and leaves the window once.
- **Space:** O(1), a fixed 26-int array.

## Edge cases

- `s1` longer than `s2`: false.
- The match is the very first window: checked before the loop.
- Repeated letters (`s1 = "aab"`): counts handle multiplicity, a set would not.
- Equal strings: true.

## Variations

- **Find All Anagrams in a String (LC 438):** same window, collect every start index where `nonZero == 0`.
- **Unicode or large alphabets:** replace the array with a `HashMap<Character, Integer>`.
- **Minimum Window Substring:** variable-size window, the same "how many letters still missing" counter.

Practise it in the app: Run / Submit on this page.
