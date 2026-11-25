**Short answer:** Loop over the words and add index `i` to the result when `words[i].indexOf(x) >= 0`. Every character may need to be read once, so O(total characters) is optimal for a single query.

## Approach

- **Direct scan:** this is already optimal. `indexOf` stops at the first match, so a word that contains the character is often cheap.
- **Key point:** return the **indices**, not the words, so use an indexed loop rather than a for-each.
- **Many queries on the same words:** pre-compute an inverted index from letter to word indices (see Variations).

Example: `words = ["leet", "code"]`, `x = 'e'` gives `[0, 1]`; `x = 'c'` gives `[1]`.

## Solution

```java
import java.util.*;

class Solution {
    public List<Integer> findWordsContaining(String[] words, char x) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            if (words[i].indexOf(x) >= 0) out.add(i);
        }
        return out;
    }
}
```

## Complexity

- **Time:** O(S), where S is the total number of characters.
- **Space:** O(1) beyond the output.

## Edge cases

- No word matches: return an empty list, not `null`.
- The character appears several times in one word: add the index once.
- The match is at position 0: test `>= 0`, not `> 0`.
- An empty word never matches.
- The same word appears twice: both indices are returned.

## Variations

- **Many queries:** build an inverted index once. Building costs O(S); each query then finds its list in O(1).

```java
import java.util.*;

class LetterIndex {
    private final List<List<Integer>> wordsByLetter = new ArrayList<>();

    LetterIndex(String[] words) {                          // lowercase a-z only
        for (int c = 0; c < 26; c++) wordsByLetter.add(new ArrayList<>());
        for (int i = 0; i < words.length; i++) {
            int mask = 0;                                  // letters already recorded for this word
            for (char ch : words[i].toCharArray()) {
                int bit = 1 << (ch - 'a');
                if ((mask & bit) == 0) wordsByLetter.get(ch - 'a').add(i);
                mask |= bit;
            }
        }
    }

    List<Integer> find(char x) {
        return Collections.unmodifiableList(wordsByLetter.get(x - 'a'));
    }
}
```

- **Any Unicode letter:** replace the 26 lists with a `Map<Integer, List<Integer>>` keyed by code point.
- **Contains all of several letters:** keep one 26-bit mask per word and test `(mask & need) == need`.
- **Case-insensitive:** lowercase the words and the query up front.
- **Stream one-liner:** `IntStream.range(0, words.length).filter(i -> words[i].indexOf(x) >= 0).boxed().toList()`.

Related: [T1 · Hashing, two pointers, sliding window and prefix sums](../academy/lessons/T1.md).
