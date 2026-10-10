**Short answer:** Split the path on `/` and walk the parts with a stack (a deque). Skip empty parts and `.`; on `..` pop if the stack is not empty; push anything else, including names like `...`. Then join the stack from bottom to top with `/`, returning `/` if it is empty. Because the interview asked for production-ready code, also show a few unit tests.

## Picture it

Path `/a/./b/../../c/` splits into `"", "a", ".", "b", "..", "..", "c"` (Java drops the trailing empty string).

| step | part | action | stack (bottom → top) |
|---|---|---|---|
| 1 | `""` | skip empty | `[]` |
| 2 | `a` | push | `[a]` |
| 3 | `.` | skip | `[a]` |
| 4 | `b` | push | `[a, b]` |
| 5 | `..` | pop `b` | `[a]` |
| 6 | `..` | pop `a` | `[]` |
| 7 | `c` | push | `[c]` |

Join bottom to top with `/`: `"/c"`.

**The picture in one sentence:** the canonical path is a stack of directory names where `..` means pop and `.` or empty means do nothing.

## Approach

- **Key insight:** a canonical path is just the list of directory names left after applying `..` as "remove the last one". That is a stack.
- Splitting on `/` turns runs of slashes into empty strings, which you skip, so `//` needs no special handling.
- Use `ArrayDeque` with `addLast` / `pollLast` so you can iterate it in insertion order at the end. `java.util.Stack` iterates bottom-to-top too, but it is synchronised and legacy.
- Only exactly `.` and `..` are special. `...` and `....` are normal names.

## Solution

```java
import java.util.*;

class Solution {
    public String simplifyPath(String path) {
        Deque<String> stack = new ArrayDeque<>();
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                if (!stack.isEmpty()) stack.pollLast();   // ".." at root stays at root
            } else {
                stack.addLast(part);
            }
        }
        if (stack.isEmpty()) return "/";
        StringBuilder sb = new StringBuilder();
        for (String dir : stack) sb.append('/').append(dir);
        return sb.toString();
    }
}
```

Tests worth writing (JUnit 5):

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SolutionTest {
    @ParameterizedTest
    @CsvSource({
        "/home/,            /home",
        "/home//foo/,       /home/foo",
        "/a/./b/../../c/,   /c",
        "/../,              /",
        "/,                 /",
        "/.../a/../b,       /.../b",
        "/a/b/c/../../..,   /"
    })
    void simplifies(String in, String expected) {
        assertEquals(expected, new Solution().simplifyPath(in));
    }
}
```

`@CsvSource` trims surrounding whitespace from unquoted values by default, so the alignment spaces are harmless.

## Complexity

- **Time:** O(n) in the path length (split, one pass, join).
- **Space:** O(n) for the parts and the stack.

## Edge cases

- Root only, or `..` above the root: `/`.
- Trailing slash: dropped.
- Names of three or more dots are ordinary directories.
- Multiple consecutive slashes.

## Variations

- **Relative paths or a current working directory:** start the stack with the cwd's parts, then apply the same rules.
- **`cd` command simulation:** the same stack, kept across calls.
- In real code, `java.nio.file.Path.of(p).normalize()` does this, but note it does not touch the file system and it keeps a leading `..` on relative paths.

Practise it in the app: Run / Submit on this page.
