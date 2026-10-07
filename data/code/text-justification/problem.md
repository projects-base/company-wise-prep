Lay out `words` as text where every line is exactly `maxWidth` characters wide. Fill lines greedily: put as many words on a line as fit with at least one space between neighbouring words. Then pad each line with extra spaces:

- On a line with several words (other than the last line), spread the spaces between the words as evenly as possible. When they cannot be split evenly, the gaps further left get one more space than the gaps further right.
- A line with a single word is left-justified: the word, then spaces up to `maxWidth`.
- The **last line** is left-justified: words separated by single spaces, then spaces up to `maxWidth`.

Return the lines in order.

**Example 1**
Input: words = ["This","is","an","example","of","text","justification."], maxWidth = 16
Output: ["This    is    an","example  of text","justification.  "]

**Example 2**
Input: words = ["What","must","be","acknowledgment","shall","be"], maxWidth = 16
Output: ["What   must   be","acknowledgment  ","shall be        "]
Why: "acknowledgment" is alone on its line, so it is left-justified; the last line is left-justified too.

**Example 3**
Input: words = ["Science","is","what","we","understand","well","enough","to","explain","to","a","computer.","Art","is","everything","else","we","do"], maxWidth = 20
Output: ["Science  is  what we","understand      well","enough to explain to","a  computer.  Art is","everything  else  we","do                  "]

**Constraints**
- 1 ≤ words.length ≤ 300
- 1 ≤ words[i].length ≤ maxWidth ≤ 100
- words contain no spaces
