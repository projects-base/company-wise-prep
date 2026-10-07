An alien language uses lowercase English letters, but in an unknown alphabetical order. You are given a list of `words` that is claimed to be sorted in that alien order (sorted the way a dictionary is: compare letter by letter, and a word comes before any longer word it is a prefix of). Return a string containing every distinct letter that appears in `words`, each exactly once, arranged in an order consistent with the list. If no alphabet could produce this sorted list, return `""`.

**Example 1**
Input: words = ["wrt","wrf","er","ett","rftt"]
Output: "wertf" — the judge prints "valid order" for any correct alphabet
Why: "wrt" < "wrf" gives t before f; "wrf" < "er" gives w before e; "er" < "ett" gives r before t; "ett" < "rftt" gives e before r.

**Example 2**
Input: words = ["z","x"]
Output: "zx"

**Example 3**
Input: words = ["z","x","z"]
Output: ""
Why: z would have to come both before and after x.

**Constraints**
- 1 ≤ words.length ≤ 3000
- 1 ≤ words[i].length ≤ 100
- words[i] consists of lowercase English letters

**Notes**:
- If more than one order fits, return any of them. Letters that are never compared can go anywhere.
- A list where a word is followed by its own proper prefix (e.g. `["abc","ab"]`) is invalid — return `""`.
- The judge checks your answer: it prints `"valid order"` for any correct alphabet, `""` for an empty answer, and `"invalid order: ..."` otherwise.
