You are given an absolute Unix-style file path (it always starts with `/`). Convert it to its canonical form using these rules:

- `.` means the current directory and can be dropped.
- `..` means the parent directory: it removes the previous directory name. Going up from the root `/` keeps you at the root.
- Any run of consecutive slashes (`//`, `///`, …) counts as a single `/`.
- Any other name — including names made only of dots such as `...` or `....` — is an ordinary directory name.

The canonical path starts with a single `/`, separates names with exactly one `/`, has no trailing `/` (unless it is just the root `/`), and contains no `.` or `..` parts. Return it.

**Example 1**
Input: path = "/home/"
Output: "/home"

**Example 2**
Input: path = "/home//foo/"
Output: "/home/foo"

**Example 3**
Input: path = "/a/./b/../../c/"
Output: "/c"

**Constraints**
- 1 ≤ path.length ≤ 3000
- path contains English letters, digits, `.`, `/` and `_`
- path is a valid absolute path (starts with `/`)

**Notes**: `"/../"` simplifies to `"/"`, and `"/.../a/../b"` simplifies to `"/.../b"`.
