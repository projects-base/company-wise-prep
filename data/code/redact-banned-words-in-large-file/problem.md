You are given the contents of a text file, `text`, and a list of `banned` words. Replace every banned word in the text with the exact string `REDACTED` and return the result.

Rules:
- A **word** is a maximal run of consecutive ASCII letters and digits (`A–Z`, `a–z`, `0–9`). Every other character, such as spaces, punctuation, hyphens, apostrophes or newlines, separates words and is copied unchanged.
- A word is banned if it equals a banned word **ignoring case**.
- Only whole words are replaced. With "cat" banned, "cats" and "concat" stay as they are.

**Example 1**
Input: text = "The cat sat on the mat.", banned = ["cat","mat"]
Output: "The REDACTED sat on the REDACTED."

**Example 2**
Input: text = "Classic CAT-cat; cats!", banned = ["cat"]
Output: "Classic REDACTED-REDACTED; cats!"
Why: "CAT" matches ignoring case, and the hyphen separates the two words. "Classic" and "cats" are different words.

**Example 3**
Input: text = "nothing to hide", banned = []
Output: "nothing to hide"

**Constraints**
- 0 ≤ text.length ≤ 2·10⁵; text contains printable ASCII characters and newlines
- 0 ≤ banned.length ≤ 10⁴; each banned word is a non-empty string of letters and digits

**Notes**: checking every word against every banned word is slow. Put the banned words, lowercased, in a hash set. Build the output with a `StringBuilder`, not repeated string concatenation. Follow-up (discussion only): if the file is too large for memory, stream it in chunks and carry a partial word across each chunk boundary, because a word may be cut in two.
