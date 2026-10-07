You want to turn `beginWord` into `endWord` by changing **one letter at a time**. Every intermediate word, and `endWord` itself, must appear in `wordList`; `beginWord` does not need to. Return the number of words in the shortest such chain, counting both `beginWord` and `endWord` (so this is the number of one-letter changes plus one). Return `0` if no chain exists.

**Example 1**
Input: beginWord = "hit", endWord = "cog", wordList = ["hot","dot","dog","lot","log","cog"]
Output: 5
Why: hit → hot → dot → dog → cog uses 5 words (4 changes).

**Example 2**
Input: beginWord = "hit", endWord = "cog", wordList = ["hot","dot","dog","lot","log"]
Output: 0
Why: "cog" is not in the list, so it can never be reached.

**Example 3**
Input: beginWord = "a", endWord = "c", wordList = ["a","b","c"]
Output: 2

**Constraints**
- 1 ≤ beginWord.length ≤ 10, and every word has the same length
- 1 ≤ wordList.length ≤ 5·10⁴
- all words are lowercase English letters; words in `wordList` are distinct
- beginWord ≠ endWord

**Notes**: comparing every pair of words is too slow on the hidden tests; generate neighbours by changing each position to each letter instead.
