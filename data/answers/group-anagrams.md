**Short answer:** Anagrams become identical once their letters are sorted, so the sorted string is a perfect group key. Put each word into a `Map<String, List<String>>` under its sorted key and return the map's values. With streams it is one line: `Collectors.groupingBy(sortedKey)`. For long words, a 26-letter count signature avoids the sort.

## Approach

- **Brute force:** compare every pair of words with an anagram check and merge groups. O(n² · k).
- **Key insight:** two words are anagrams exactly when they have the same canonical form. Sorting the letters gives that form (`"eat"`, `"tea"`, `"ate"` → `"aet"`).
- **Optimal:** one pass, hash each word by its canonical key. O(n · k log k) with sorting, or O(n · k) with a letter-count key.

## Solution

```java
class Solution {
    // Classic loop
    public List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> groups = new HashMap<>();
        for (String s : strs) {
            char[] chars = s.toCharArray();
            Arrays.sort(chars);
            groups.computeIfAbsent(new String(chars), k -> new ArrayList<>()).add(s);
        }
        return new ArrayList<>(groups.values());
    }

    // Follow-up: Stream API with groupingBy on the sorted key
    public List<List<String>> groupAnagramsStream(String[] strs) {
        return new ArrayList<>(Arrays.stream(strs)
                .collect(Collectors.groupingBy(Solution::sortedKey))
                .values());
    }

    private static String sortedKey(String s) {
        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    // Count-based key: O(k) per word instead of O(k log k). Lowercase a-z only.
    private static String countKey(String s) {
        int[] count = new int[26];
        for (char c : s.toCharArray()) count[c - 'a']++;
        return Arrays.toString(count);      // e.g. "[1, 0, 0, ...]", unambiguous
    }
}
```

Input `["eat","tea","tan","ate","nat","bat"]` → `[["eat","tea","ate"], ["tan","nat"], ["bat"]]` (group order may vary).

To keep groups in the order they first appear, use `groupingBy(Solution::sortedKey, LinkedHashMap::new, Collectors.toList())`.

## Complexity

- **Time:** O(n · k log k) with the sorted key, where n is the number of words and k the maximum word length: each word is sorted once, and the map operations cost O(k) for hashing the key. With the count key, O(n · k).
- **Space:** O(n · k): the map stores every word plus one key per group.

## Edge cases

- Empty array → empty list.
- Empty string `""` → its own group with key `""`.
- A single word → one group.
- Duplicates (`["a","a"]`) → same group, both kept.
- Uppercase or Unicode input: the count key with `int[26]` breaks; the sorted key still works (normalise case first if "Tea" and "eat" should match).

## Variations

- **Valid anagram (two strings):** compare counts in an `int[26]`: O(k).
- **Find all anagrams of p in s:** sliding window with two count arrays.
- **Return only groups of size > 1:** filter the values after grouping.
- **Why not use the sum or product of letters as the key?** Sums collide (`"ad"` and `"bc"`); products of primes work but overflow for long words.

Deeper: [C4 · The optimisation playbook: brute force to optimal, out loud](../academy/lessons/C4.md).
