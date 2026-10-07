A firewall has a list of address **patterns**. Each pattern has four dot-separated parts, like an IPv4 address; each part is either a number from `0` to `255` or the wildcard `*`, which matches any number in that position. A pattern with no `*` matches exactly one address. For example, `192.124.168.*` matches `192.124.168.0` through `192.124.168.255`, and `10.*.*.1` matches `10.7.200.1`.

For each address in `queries`, decide whether it matches **at least one** pattern. Return a boolean array with one answer per query.

**Example 1**
Input: patterns = ["192.124.168.*","10.0.0.1"], queries = ["192.124.168.77","10.0.0.1","10.0.0.2","192.124.169.77"]
Output: [true,true,false,false]

**Example 2**
Input: patterns = ["*.*.*.*"], queries = ["0.0.0.0","255.255.255.255"]
Output: [true,true]

**Example 3**
Input: patterns = ["1.*.3.*","*.2.*.4"], queries = ["1.9.3.9","9.2.9.4","1.2.9.9"]
Output: [true,true,false]
Why: "1.2.9.9" fails the first pattern in part 3 and the second pattern in part 1.

**Constraints**
- 1 ≤ patterns.length, queries.length ≤ 5000
- every number is written in plain decimal without leading zeros (`0` is written as `0`)
- every query is a full address of four numbers (no wildcards)

**Notes**: comparing every query with every pattern is O(P · Q). Can you answer each query in time that does not depend on the number of patterns?
