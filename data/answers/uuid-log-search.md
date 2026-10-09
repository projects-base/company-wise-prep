**Short answer:** For each line, look for the query case-insensitively with `String.regionMatches(true, …)`. Accept a hit only when it stands alone: the character before and the character after must not be a letter, digit or hyphen. Return the indices of matching lines in order. This is O(total log size × 36) and needs no regex. Interviewers are checking that you clarify the matching rules and handle the boundaries cleanly.

## Approach

- **Clarify first.** Is the match case-sensitive? Does the UUID have to be a whole token, or is a substring enough? Is the log one string, or a list of lines? Do we return line numbers or the lines themselves? The rules here are case-insensitive, whole-token, line indices.
- **Naive.** `line.toLowerCase().contains(query.toLowerCase())`. It allocates a lowercased copy of every line, and it accepts a UUID glued to other characters (`...4000x`), which is a false positive.
- **Better.** Scan each start position `p` with `regionMatches(true, p, query, 0, 36)`. That compares without allocating. Then check the boundary characters. Once a line matches, stop scanning it (`break`), so each line is reported once.
- **Regex alternative.** `Pattern.compile("(?<![A-Za-z0-9-])" + Pattern.quote(query) + "(?![A-Za-z0-9-])", Pattern.CASE_INSENSITIVE)`, compiled **once** outside the loop. It is concise, but harder to explain if the interviewer asks how it works.

## Solution

```java
import java.util.*;

class Solution {
    public List<Integer> searchLogs(String[] logs, String query) {
        List<Integer> out = new ArrayList<>();
        int m = query.length();
        for (int i = 0; i < logs.length; i++) {
            String line = logs[i];
            for (int p = 0; p + m <= line.length(); p++) {
                if (!line.regionMatches(true, p, query, 0, m)) continue;
                boolean leftOk = p == 0 || !isWordChar(line.charAt(p - 1));
                boolean rightOk = p + m == line.length() || !isWordChar(line.charAt(p + m));
                if (leftOk && rightOk) {
                    out.add(i);
                    break;            // one hit per line is enough
                }
            }
        }
        return out;
    }

    private boolean isWordChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-';
    }
}
```

## Complexity

- **Time:** O(total characters × 36) in the worst case. In practice `regionMatches` fails on the first character almost every time, so it is close to linear.
- **Space:** O(1) beyond the output list.

## Edge cases

- UUID at the very start or very end of a line.
- Mixed case on either side (query in upper case, log in lower case).
- UUID glued to a letter, a digit or a hyphen: not a match.
- The same UUID twice in one line: the line is reported once.
- Empty lines and lines shorter than 36 characters.
- A false hit followed by a real one in the same line: the scan continues after a rejected hit.

## Variations

- **A real log file:** stream it with `Files.lines(path)` (or a `BufferedReader`) instead of loading everything. Keep the line number in a counter.
- **Many queries over the same log:** parse every UUID out of each line once (regex `[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}`), normalise it to lower case, and build a `Map<String, List<Integer>>` inverted index. Each query then costs O(1).
- **Distributed logs:** that is what a log platform's index does (Elasticsearch, Loki labels). Mention it, then come back to the code.
- **Validate the query:** `UUID.fromString` is lenient about some malformed input, so use the regex above if strict validation matters.

Practise it in the app: Run / Submit on this page.
