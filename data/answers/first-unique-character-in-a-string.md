**Short answer:** Count every character in one pass, then scan the string again and return the first character whose count is 1. That's O(n) time. For lowercase letters an `int[26]` is the fastest counter; for any characters use a `HashMap`. The stream version groups into a `LinkedHashMap` so insertion order is kept, then takes the first entry with count 1.

## Approach

- **Brute force:** for each index `i`, scan the whole string to see if `s.charAt(i)` appears elsewhere. O(n²).
- **Key insight:** whether a character is unique depends only on its total count, which one pass can compute. A second pass in the original order finds the first one with count 1.
- **Optimal:** two passes with a frequency array. O(n) time, O(1) extra space (fixed alphabet).

## Solution

```java
class Solution {
    // LeetCode 387: return the index, or -1. Input: lowercase English letters.
    public int firstUniqChar(String s) {
        int[] count = new int[26];
        for (int i = 0; i < s.length(); i++) {
            count[s.charAt(i) - 'a']++;
        }
        for (int i = 0; i < s.length(); i++) {
            if (count[s.charAt(i) - 'a'] == 1) return i;
        }
        return -1;
    }

    // Any characters (upper case, digits, symbols)
    public static Optional<Character> firstUnique(String s) {
        Map<Character, Integer> count = new HashMap<>();
        for (char c : s.toCharArray()) count.merge(c, 1, Integer::sum);
        for (char c : s.toCharArray()) {
            if (count.get(c) == 1) return Optional.of(c);
        }
        return Optional.empty();
    }

    // Follow-up: Java stream + LinkedHashMap
    public static Optional<Character> firstUniqueStream(String s) {
        return s.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(Function.identity(),
                        LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() == 1)
                .map(Map.Entry::getKey)
                .findFirst();
    }
}
```

`firstUniqueStream("swiss")` → `w`. The `LinkedHashMap::new` argument is the important part: a plain `groupingBy` returns a `HashMap` with no defined order, so `findFirst` would return an arbitrary unique character.

## Complexity

- **Time:** O(n). Two linear passes; each array or hash operation is O(1). The stream version is also O(n): one pass to build the map, then a pass over at most k distinct keys.
- **Space:** O(1) for the `int[26]` (fixed size). O(k) for the map versions, where k is the number of distinct characters.

## Edge cases

- Empty string → `-1` / `Optional.empty()`.
- No unique character (`"aabb"`) → `-1`.
- Single character → index 0.
- Case sensitivity: `'A'` and `'a'` are different unless you normalise first.
- Characters outside the BMP (emoji) are two `char`s; use `s.codePoints()` if that matters.

## Variations

- **Stream of characters arriving one by one** (first unique so far): keep counts plus a queue (or `LinkedHashSet`) of candidates; pop from the front while the head's count is above 1. Amortised O(1) per character.
- **Return the last unique** character: scan the second pass from the end.
- **First repeating** character: one pass with a `HashSet`; the first `add` that returns `false`.
