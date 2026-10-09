**Short answer:** The exact problem was never published. The pattern behind it is this: a trie matches **prefixes**, so when the question is about **suffixes** ("which words end with X?", "does the text seen so far end with a dictionary word?"), insert every string **reversed**. A suffix of a word is a prefix of the reversed word, so one walk down the reversed trie answers the question in O(length of the query), with no need to compare against every word.

**Assumed problem (state this to the interviewer):** implement `addWord(word)` and `countEndingWith(suffix)`, which returns how many added words end with `suffix`. Then show the stream version: after each incoming character, report whether some dictionary word ends at the current position (LeetCode 1032, "Stream of Characters"). Both are classic reverse-trie problems.

## Approach

- **Brute force.** For each query, call `endsWith` on every word: O(N · L) per query. For the stream, check every word against the tail of the text after each character: O(N · L) per character.
- **Key insight.** Reverse the strings. `word.endsWith(suffix)` is the same as `reverse(word).startsWith(reverse(suffix))`. In a trie of reversed words, the words ending with `suffix` are exactly the words passing through the node reached by walking `suffix` from its **last** character to its first. If each node stores a `count` of words passing through it, the query is one walk.
- **Stream version.** Keep the most recent characters (at most the longest word length). After each new character, walk the reversed trie from the newest character backwards. Stop at the first `isWord` node (match found) or at a missing child (no match). Each step costs O(longest word), not O(dictionary).

## Solution

```java
import java.util.*;

/** Words stored reversed, so suffix questions become prefix walks. */
class SuffixTrie {
    private static final class Node {
        final Node[] next = new Node[26];
        int count;       // words whose reversed form passes through this node
        boolean isWord;  // a word ends here (read backwards)
    }

    private final Node root = new Node();

    public void addWord(String word) {
        Node n = root;
        n.count++;
        for (int i = word.length() - 1; i >= 0; i--) {   // insert reversed
            int c = word.charAt(i) - 'a';
            if (n.next[c] == null) n.next[c] = new Node();
            n = n.next[c];
            n.count++;
        }
        n.isWord = true;
    }

    /** How many added words end with suffix. */
    public int countEndingWith(String suffix) {
        Node n = root;
        for (int i = suffix.length() - 1; i >= 0 && n != null; i--) {
            n = n.next[suffix.charAt(i) - 'a'];
        }
        return n == null ? 0 : n.count;
    }

    /** True if some added word is a suffix of text[0..end] (used by the stream checker). */
    boolean someWordEndsAt(CharSequence text, int end) {
        Node n = root;
        for (int i = end; i >= 0; i--) {
            n = n.next[text.charAt(i) - 'a'];
            if (n == null) return false;
            if (n.isWord) return true;
        }
        return false;
    }
}

/** LeetCode 1032 style: query(c) returns true if a dictionary word ends at the newest letter. */
class StreamChecker {
    private final SuffixTrie trie = new SuffixTrie();
    private final StringBuilder recent = new StringBuilder();
    private final int maxLen;

    public StreamChecker(String[] words) {
        int m = 0;
        for (String w : words) { trie.addWord(w); m = Math.max(m, w.length()); }
        maxLen = m;
    }

    public boolean query(char letter) {
        recent.append(letter);
        if (recent.length() > 2 * maxLen) {               // keep memory bounded
            recent.delete(0, recent.length() - maxLen);
        }
        return trie.someWordEndsAt(recent, recent.length() - 1);
    }
}
```

The buffer trims only when it reaches twice the longest word. That keeps trimming amortised O(1) per character, while always keeping at least `maxLen` recent characters.

## Complexity

- **addWord:** O(L) time. Total space O(sum of word lengths × 26) references.
- **countEndingWith:** O(|suffix|).
- **Stream query:** O(maxLen) worst case per character, independent of the number of words. Buffer memory is O(maxLen).

## Edge cases

- Empty suffix: the walk stays at the root and returns the total word count. Decide with the interviewer whether that is wanted.
- Duplicate words: counted twice by `count`. Use a set first if words must be unique.
- One word being a suffix of another ("ab" and "cab"): both pass through the same nodes. `isWord` marks the shorter one.
- Characters outside `a–z`: switch `next` to a `HashMap<Character, Node>`.

## Variations

- **Short Encoding of Words (LC 820):** insert reversed words. The encoding length is the sum of (depth + 1) over the leaves, because words that are suffixes of others disappear.
- **Prefix and suffix together (LC 745, "Prefix and Suffix Search"):** insert `suffix + '{' + word` for every suffix, or keep a forward and a reversed trie and intersect the results.
- **Many patterns in a long text:** Aho-Corasick builds failure links over a forward trie and matches all patterns in one pass, O(text + matches).
- **Longest common suffix of a group:** walk the reversed trie while a node has a single child.
