# Code challenges — LeetCode-style "Run" and "Submit"

A question in the bank becomes runnable when `data/code/<question-slug>/` exists:

```
data/code/
  _lib/IO.java                  shared input parsing / canonical output, TreeNode, ListNode
  <slug>/
    problem.md                  the full statement, in our own words (never copied from LeetCode)
    Solution.java               the starter the learner edits (class Solution, method stubs)
    Main.java                   hidden harness: reads ONE test from stdin, calls Solution, prints
    reference/Solution.java     a correct solution — proves the tests; shown only on request
    tests.yaml                  the test cases
```

The app compiles `IO.java + Main.java + <learner's Solution.java>` and runs **each test in its own
JVM** (time and memory limited), feeding `input` on stdin and comparing stdout with `expected`.

## Input format — one argument per line, JSON-like

```
[2,7,11,15]        int[]            → in.nextIntArray()
9                  int              → in.nextInt()
"abc"              String           → in.nextString()
[["1","0"],["0","1"]]  char[][]     → in.nextCharMatrix()
[[1,2],[3]]        int[][] / lists  → in.nextIntMatrix() / in.nextIntListList()
[3,9,20,null,null,15,7]  tree       → in.nextTree()
[1,2,3]            linked list      → in.nextLinkedList()
```

Design problems (LRU Cache etc.) use LeetCode's two-line form — operations, then arguments —
read with `in.nextList()` twice; the harness prints the list of results (`null` for void calls).

## Output — always `IO.print(result)`

Canonical: no spaces, `[1,2]`, strings quoted, doubles to 5 decimals, trees in level order with
trailing nulls trimmed. **If the problem accepts any order, the harness sorts before printing**
so `expected` stays a single exact string. The runner compares after trimming trailing
whitespace on each line and trailing blank lines — nothing fuzzier.

## tests.yaml

```yaml
method: twoSum          # informational, shown in the UI
tests:
  - name: Example 1
    input: |
      [2,7,11,15]
      9
    expected: |
      [0,1]
  - name: Large input
    hidden: true        # used by Submit only
    input: |
      ...
    expected: |
      ...
```

Rules:
- 2–3 visible examples (from the problem's classic examples) + 4–8 hidden tests covering edge
  cases (empty / single element / duplicates / negatives / max constraints where cheap).
- **Every expected output is produced by running the reference solution**, never typed by hand.
  `python -I tools/check_challenge.py <slug>` does both: runs the reference on every test, and
  checks the starter compiles against the harness. It can also `--write-expected` to fill
  `expected` from the reference.
- Large inputs: keep each test file under ~200 KB; generate them with a script, not by hand.

## Starter (`Solution.java`)

The exact LeetCode signature, a one-line comment with the problem statement's key constraint,
and a body that compiles and returns a default (`return new int[0];`, `return 0;`, `return null;`).
No imports beyond `java.util.*`. Design problems put LeetCode's class (`LRUCache`, `MinStack`, …)
in `Solution.java` instead of `class Solution` — the runner compiles whatever classes the file holds.

Problems with many valid answers (topological orders, any palindrome of max length) may have a
harness that **validates** the learner's answer and prints a fixed verdict string instead of the answer.

## problem.md

Written in our own words — never paste a site's problem text. Sections:

```markdown
<2–5 sentence statement of the task>

**Example 1**
Input: nums = [2,7,11,15], target = 9
Output: [0,1]
Why: nums[0] + nums[1] = 9.

**Constraints**
- 2 ≤ nums.length ≤ 10⁵
- exactly one valid answer exists

**Notes** (optional): any-order rules, what to return when there is no answer.
```

For custom problems that come only from interview reports (no LeetCode equivalent), pin the
problem down precisely here — inputs, outputs, edge-case behaviour — since the report is vague.
