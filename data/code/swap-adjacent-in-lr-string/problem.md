A row of cells is described by a string over `L`, `R` and `X`, where `X` is an empty cell, `L` is a piece that can only slide left and `R` is a piece that can only slide right. One move is either `"XL" → "LX"` (an `L` steps left into an empty cell) or `"RX" → "XR"` (an `R` steps right into an empty cell). Pieces never jump over each other. Given `start` and `result` of the same length, return `true` if `result` can be reached from `start` by a sequence of moves.

**Example 1**
Input: start = "RXXLRXRXL", result = "XRLXXRRLX"
Output: true
Why: RXXLRXRXL → XRXLRXRXL → XRLXRXRXL → XRLXXRRXL → XRLXXRRLX.

**Example 2**
Input: start = "X", result = "L"
Output: false
Why: pieces are never created or destroyed.

**Example 3**
Input: start = "XXRXXLXXXX", result = "XXXXRXXLXX"
Output: false
Why: the `L` would have to move right.

**Constraints**
- 1 ≤ start.length ≤ 10⁴
- start.length == result.length
- both strings contain only `L`, `R` and `X`
