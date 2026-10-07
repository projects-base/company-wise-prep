A service writes one error code per failing request, and `words` is the list of codes taken from its log lines (for example `"E500"`, `"E404"`). Return the `k` codes that occur most often, ordered from most to least frequent. When two codes occur equally often, the one that is smaller in plain string order comes first — compared character by character by character code, exactly like Java's `String.compareTo` (so digits < uppercase < lowercase).

**Example 1**
Input: words = ["E500","E404","E500","E403","E404","E500"], k = 2
Output: ["E500","E404"]
Why: E500 appears 3 times, E404 twice, E403 once.

**Example 2**
Input: words = ["E2","E10","E2","E10","E7"], k = 2
Output: ["E10","E2"]
Why: E10 and E2 tie with 2 each; "E10" < "E2" as strings because '1' < '2'.

**Example 3**
Input: words = ["the","day","is","sunny","the","the","the","sunny","is","is"], k = 4
Output: ["the","is","sunny","day"]

**Constraints**
- 1 ≤ words.length ≤ 10⁵
- 1 ≤ words[i].length ≤ 10; codes contain only uppercase letters, lowercase letters and digits
- 1 ≤ k ≤ number of distinct codes

**Notes**: the order of the result matters. Can you do it in O(n log k)? The hidden tests include 25,000 log entries.
