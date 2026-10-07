You are given `freq`, where `freq[i]` is how many times symbol `i` occurs in a message. Build a **Huffman code**: assign each symbol a binary codeword (a string of `'0'` and `'1'` characters) so that

- the code is **prefix-free** — no codeword is a prefix of another codeword (so a bit stream can be decoded unambiguously), and
- the total encoded length `Σ freq[i] · codes[i].length()` is as **small as possible**.

Return `codes`, where `codes[i]` is the codeword of symbol `i`. Every codeword must be non-empty, so if there is only one symbol, give it a one-bit codeword.

Many different codes are optimal (you can swap `0`/`1` at any branch, and ties can be broken either way), so any optimal prefix-free code is accepted. The checker verifies your code and **prints its total encoded length**, which must equal the optimum — or prints an error message if the code is not valid.

**Example 1**
Input: freq = [5,9,12,13,16,45]
Output: 224
Why: one optimal code is 45 → "0", 13 → "101", 12 → "100", 16 → "111", 5 → "1100", 9 → "1101": 45·1 + (12 + 13 + 16)·3 + (5 + 9)·4 = 224.

**Example 2**
Input: freq = [1,1,1,1]
Output: 8
Why: four codewords of length 2.

**Example 3**
Input: freq = [7]
Output: 7
Why: a single symbol still needs a one-bit codeword such as "0".

**Constraints**
- 1 ≤ freq.length ≤ 10⁴
- 1 ≤ freq[i] ≤ 10⁹

**Notes**: repeatedly scanning for the two smallest weights is O(n²); a priority queue makes it O(n log n).
