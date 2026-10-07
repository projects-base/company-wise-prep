You are given a list of words `products` and a string `searchWord` that a user types one character at a time. After each typed character, suggest up to three words from `products` that start with the prefix typed so far. If more than three words match, suggest the three lexicographically smallest. Return a list with one entry per typed character, each entry being that step's suggestions in sorted order (an empty list when nothing matches).

**Example 1**
Input: products = ["mobile","mouse","moneypot","monitor","mousepad"], searchWord = "mouse"
Output: [["mobile","moneypot","monitor"],["mobile","moneypot","monitor"],["mouse","mousepad"],["mouse","mousepad"],["mouse","mousepad"]]
Why: after "m" and "mo" all five match and the three smallest are shown; from "mou" on only "mouse" and "mousepad" match.

**Example 2**
Input: products = ["havana"], searchWord = "tatiana"
Output: [[],[],[],[],[],[],[]]

**Constraints**
- 1 ≤ products.length ≤ 2 · 10⁴, 1 ≤ products[i].length ≤ 3000, total characters in `products` ≤ 2 · 10⁵
- 1 ≤ searchWord.length ≤ 1000
- all strings contain only lowercase English letters; `products` has no duplicates

**Notes**: the interview asked for this as a small class — `addWord(word)` and `getWords(prefix)` returning the three smallest matches — built on a trie or a sorted list with binary search. Follow-up (not tested here): let the pattern contain `.` meaning "any one letter" and still return the three smallest matches efficiently.
