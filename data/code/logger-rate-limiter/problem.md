Messages arrive in a stream, each with a timestamp in seconds. A message should be printed only if the same message has not been **printed** in the last 10 seconds: if a message is printed at time `t`, identical messages are suppressed until time `t + 10` (a copy arriving at exactly `t + 10` is printed). Suppressed messages do not reset the clock. Implement the class `Logger`:

- `Logger()` creates the logger.
- `boolean shouldPrintMessage(int timestamp, String message)` returns `true` if `message` should be printed at `timestamp` (and records it as printed), otherwise `false`.

Timestamps arrive in non-decreasing order; several messages may share a timestamp.

**Input format**: two lines — the list of operation names, then the list of argument lists (the first operation is always the constructor). The output is the list of return values, with `null` for the constructor.

**Example 1**
Input:
["Logger","shouldPrintMessage","shouldPrintMessage","shouldPrintMessage","shouldPrintMessage","shouldPrintMessage","shouldPrintMessage"]
[[],[1,"foo"],[2,"bar"],[3,"foo"],[8,"bar"],[10,"foo"],[11,"foo"]]
Output: [null,true,true,false,false,false,true]
Why: "foo" printed at 1 blocks "foo" until 11; "bar" printed at 2 blocks "bar" until 12.

**Example 2**
Input:
["Logger","shouldPrintMessage","shouldPrintMessage","shouldPrintMessage"]
[[],[0,"a"],[0,"a"],[0,"b"]]
Output: [null,true,false,true]

**Constraints**
- 0 ≤ timestamp ≤ 10⁹, non-decreasing across calls
- 1 ≤ message.length ≤ 30, lowercase English letters
- at most 10⁴ calls

**Notes**: write your code in the class `Logger` (keep that name). Interview follow-up worth thinking about (not tested here): suppress a message if a duplicate appears within 10 seconds in the past **or the future** — which needs buffering each message for 10 seconds before deciding.
