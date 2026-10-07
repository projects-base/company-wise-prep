You are building a small templating library. Given a map `vars` from variable names to values, and a string `template`, return the template with every variable reference replaced by that variable's value. Values may contain references too, so they must be expanded the same way.

The rules, applied left to right:

- `%%` is an escaped percent sign and becomes a single literal `%`.
- `%NAME%`, where `NAME` is one or more characters none of which is `%`, is a **reference**. It is replaced by the **fully expanded** value of `NAME`. The expanded text is inserted as-is — it is not scanned again.
- Any other character is copied unchanged.

Return `null` (the expansion fails) if any of these happens while expanding:

- a reference names a variable that is not in `vars`;
- a `%` opens a reference that is never closed (no later `%`);
- expanding a variable requires expanding that same variable again (a cycle such as `A → %B%`, `B → %A%`).

Only variables that are actually reached matter: an unused variable that is missing, malformed or cyclic does not cause a failure.

**Example 1**
Input: vars = [["NAME","Ada"],["GREETING","Hello, %NAME%!"]], template = "%GREETING% You are 100%% ready."
Output: "Hello, Ada! You are 100% ready."

**Example 2**
Input: vars = [["A","x%B%"],["B","y%A%"]], template = "start %A%"
Output: null
Why: A needs B, which needs A again — a cycle.

**Example 3**
Input: vars = [["HOST","example.com"]], template = "https://%HOST%/%PATH%"
Output: null
Why: PATH is not defined.

**Constraints**
- 0 ≤ number of variables ≤ 10⁴; names are non-empty, unique and contain no `%`
- 0 ≤ template.length, value lengths ≤ 10⁴
- if the expansion succeeds, the result and every expanded value along the way have at most 10⁵ characters

**Notes**: in the examples `vars` is shown as a list of `[name, value]` pairs; your method receives a `Map<String, String>`. A variable can be referenced many times — the hidden tests include deeply shared references where re-expanding the same variable over and over takes exponential time, so remember each variable's expansion once it is computed.
