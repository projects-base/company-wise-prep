**Short answer:** Split both sentences into words and call the shorter one `b`. Count how many words match from the front, then how many match from the back without overlapping the front match. If those two counts together cover all of `b`, the extra words of the longer sentence form one contiguous gap, so the answer is true. It runs in linear time with two pointers.

## Approach

- **Brute force:** try every start and length of a phrase to delete from the longer sentence, then compare. O(n²) or worse.
- **Key insight:** inserting one phrase into `b` means `b` must equal "a prefix of `a`" followed by "a suffix of `a`". So take the longest common prefix of words, then the longest common suffix of what is left. If prefix + suffix covers `b`, it works.
- Compare **words**, not characters, so "My" does not match "Myself".
- Cap the suffix match at `b.length - i` so the prefix and suffix never use the same word of `b` twice.

## Solution

```java
class Solution {
    public boolean areSentencesSimilar(String sentence1, String sentence2) {
        String[] a = sentence1.split(" "), b = sentence2.split(" ");
        if (a.length < b.length) { String[] t = a; a = b; b = t; }   // b is the shorter
        int i = 0, j = 0;
        while (i < b.length && a[i].equals(b[i])) i++;                // common prefix
        while (j < b.length - i
                && a[a.length - 1 - j].equals(b[b.length - 1 - j])) j++; // common suffix
        return i + j >= b.length;
    }
}
```

## Complexity

- **Time:** O(n + m) in total characters, for splitting and comparing.
- **Space:** O(n + m) for the word arrays. Index pointers over the raw strings would avoid them, at the cost of messier code.

## Edge cases

- Identical sentences: true (insert the empty phrase).
- Same word count but different words: false, because the gap would have to be non-empty.
- "of" vs "A lot of words": the words would have to be added on both sides, so false.
- Repeated words such as "A A" vs "A A A": the cap on `j` prevents double counting.
- Comparison is case-sensitive: "a" is not "A".

Practise it in the app: Run / Submit on this page.
