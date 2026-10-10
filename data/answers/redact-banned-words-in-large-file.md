**Short answer:** Lowercase the banned words into a `HashSet`. Scan the text once: copy separator characters as they are, and for each maximal run of letters and digits, look it up (lowercased) in the set and append either the word or `REDACTED` to a `StringBuilder`. That is O(total length). For a file too large for memory, stream it in chunks and carry the trailing partial word into the next chunk.

## Picture it

Example 2: `text = "Classic CAT-cat; cats!"`, `ban = {"cat"}`.

| Step | i | Character class | Token / char | Lowercased in ban? | Appended |
|---|---|---|---|---|---|
| 1 | 0 | word run 0–6 | "Classic" | no | Classic |
| 2 | 7 | separator | ' ' | — | ' ' |
| 3 | 8 | word run 8–10 | "CAT" | "cat" yes | REDACTED |
| 4 | 11 | separator | '-' | — | - |
| 5 | 12 | word run 12–14 | "cat" | yes | REDACTED |
| 6 | 15, 16 | separators | ';' ' ' | — | ; and space |
| 7 | 17 | word run 17–20 | "cats" | no | cats |
| 8 | 21 | separator | '!' | — | ! |

Output: "Classic REDACTED-REDACTED; cats!".

**The picture in one sentence:** tokenise once into maximal letter/digit runs, so whole-word matching is automatic and each word costs one hash-set lookup.

## Approach

- **Brute force.** For every banned word, call `replaceAll` on the text. That is O(text × banned), and naive regexes also replace inside other words ("cat" in "concat").
- **Key insight.** Tokenise once, then each word costs one O(word length) hash lookup. Whole-word matching falls out of the tokeniser: a word is a maximal run of `[A-Za-z0-9]`, so "cats" is a different token from "cat".
- **Case-insensitive match:** lowercase both the banned words and each token with `Locale.ROOT` (the default locale can change the result, for example Turkish dotless i).

## Solution

```java
import java.util.*;

class Solution {
    public String redact(String text, String[] banned) {
        Set<String> ban = new HashSet<>();
        for (String w : banned) ban.add(w.toLowerCase(Locale.ROOT));
        StringBuilder out = new StringBuilder(text.length());
        int i = 0, n = text.length();
        while (i < n) {
            if (!isWordChar(text.charAt(i))) {
                out.append(text.charAt(i++));
                continue;
            }
            int j = i;
            while (j < n && isWordChar(text.charAt(j))) j++;
            String word = text.substring(i, j);
            out.append(ban.contains(word.toLowerCase(Locale.ROOT)) ? "REDACTED" : word);
            i = j;
        }
        return out.toString();
    }

    private static boolean isWordChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }
}
```

## Complexity

- **Time:** O(L + B), where L is the text length and B the total length of the banned words. Each character is visited once and hashed once.
- **Space:** O(B) for the set, O(L) for the output.

## Edge cases

- Empty text or empty banned list: output equals input.
- Word at the very start or end of the text.
- Mixed case (`CAT`, `Cat`).
- Separators that look like word parts: `CAT-cat` is two words, `don't` is `don` + `t`.
- Banned word that is a prefix or suffix of another word: not replaced.

## Follow-ups

- **The file is too large for memory.** Read with a `BufferedReader` (or a `Reader` into a `char[]` buffer of, say, 64 KB) and write to a `BufferedWriter`. Process each chunk with the same tokeniser, but if the chunk ends in the middle of a word, do not emit that word. Keep it as a carry-over prefix and prepend it to the next chunk. At end of file, flush the carry. Memory stays O(chunk + longest word + B). Reading line by line also works when words never span lines, but a single huge line breaks that assumption, so fixed-size chunks with carry-over are safer.
- **Huge banned list:** if it does not fit in memory, use a Bloom filter as a fast "definitely not banned" check backed by an on-disk lookup, or shard the file by hash.
- **Banned phrases (multi-word) or substrings:** use Aho-Corasick over the banned patterns for a single linear pass.
- **Parallelism:** split the file at separator boundaries and process pieces on several threads, then concatenate in order.

See [C3 · Hidden time costs](../academy/lessons/C3.md) for why string concatenation in a loop is quadratic and `StringBuilder` is not.

Practise it in the app: Run / Submit on this page.
