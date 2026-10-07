You are given the lines of a log file and a UUID `query`. A UUID is 32 hexadecimal digits in groups of 8-4-4-4-12 separated by hyphens, for example `123e4567-e89b-12d3-a456-426614174000`. Return the 0-based indices, in increasing order, of all lines that **mention** the query.

A line mentions the query when it contains the same 36 characters as the query, **ignoring upper/lower case**, and that occurrence stands on its own: the character just before it and the character just after it (when they exist) are not a letter, a digit or a hyphen. So `id=123E4567-E89B-12D3-A456-426614174000,` mentions the UUID above, but `x123e4567-e89b-12d3-a456-426614174000` and `123e4567-e89b-12d3-a456-4266141740001` do not.

**Example 1**
Input: logs = ["10:00 start job 123e4567-e89b-12d3-a456-426614174000","10:01 job ok","10:02 retry [123E4567-E89B-12D3-A456-426614174000]"], query = "123e4567-e89b-12d3-a456-426614174000"
Output: [0,2]
Why: line 2 has the same UUID in upper case, enclosed in brackets.

**Example 2**
Input: logs = ["req=a1b2c3d4-0000-1111-2222-333344445555x","req=a1b2c3d4-0000-1111-2222-333344445555"], query = "A1B2C3D4-0000-1111-2222-333344445555"
Output: [1]
Why: in line 0 the UUID runs straight into the letter x.

**Example 3**
Input: logs = ["nothing here"], query = "00000000-0000-0000-0000-000000000000"
Output: []

**Constraints**
- 1 ≤ logs.length ≤ 10⁴, 0 ≤ logs[i].length ≤ 500
- log lines contain printable ASCII characters
- `query` is a valid UUID (any mix of upper and lower case)
