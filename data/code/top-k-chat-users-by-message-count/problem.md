A chat log is a list of messages, where `messages[i] = [user, text]` means `user` sent the message `text`. Given the log and an integer `k`, return the `k` users who sent the most messages, from most to fewest. Users with the same number of messages are ordered by name, alphabetically ascending (plain string comparison). If fewer than `k` distinct users appear, return all of them in that order.

**Example 1**
Input: messages = [["ann","hi"],["bob","hello"],["ann","how are you"],["cat","yo"],["bob","fine"],["ann","great"]], k = 2
Output: ["ann","bob"]
Why: ann sent 3 messages, bob 2, cat 1.

**Example 2**
Input: messages = [["zed","a"],["amy","b"],["kim","c"]], k = 2
Output: ["amy","kim"]
Why: everyone sent one message, so the tie is broken by name.

**Example 3**
Input: messages = [["solo","only me"]], k = 5
Output: ["solo"]

**Constraints**
- 1 ≤ messages.length ≤ 10⁵
- 1 ≤ k ≤ 10⁵
- user names are non-empty strings of lowercase letters and digits; text may be any string (it is not used)

**Notes**: aim for O(m + u log k) with a heap of size k (m messages, u distinct users); sorting all users, O(u log u), is also accepted.
