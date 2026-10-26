"""Builds the ```anim blocks (step-by-step animations) and writes them into answers and lessons.

Each generator runs the real algorithm in Python and records a frame per meaningful step, so the
animation matches what the Java code does. Run from the repo root:  python tools/anim/build.py
The block goes into a "## Watch it" section (replaced on every run): after "## Picture it" when
the file has one, otherwise before the first "## " heading, unless a target names its own anchor.
"""
import json
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parents[2]


def frame(**kw):
    return {k: v for k, v in kw.items() if v is not None}


# ── DSA ──────────────────────────────────────────────────────────────────────────────────────

def two_sum():
    nums, target = [3, 8, 4, 11, 6], 10
    code = [
        'int[] twoSum(int[] nums, int target) {',
        '  Map<Integer, Integer> seen = new HashMap<>();   // value -> index',
        '  for (int i = 0; i < nums.length; i++) {',
        '    int need = target - nums[i];',
        '    if (seen.containsKey(need))',
        '      return new int[]{seen.get(need), i};',
        '    seen.put(nums[i], i);',
        '  }',
        '  return new int[0];',
        '}',
    ]
    seen, frames = {}, []
    arr = lambda **k: dict(label='nums', values=nums, **k)
    ents = lambda: [[str(k), v] for k, v in seen.items()]
    frames.append(frame(note=f'Goal: two numbers that add up to {target}. One pass, remembering every value we have seen.', line=2,
                        array=arr(), map=dict(label='seen (value → index)', entries=ents()), vars={'target': target}))
    for i, x in enumerate(nums):
        need = target - x
        frames.append(frame(note=f'i = {i}: nums[i] = {x}, so we need {target} − {x} = {need}. Is {need} in the map?', line=4,
                            array=arr(pointers={'i': i}, marks={str(i): 'cur'}), map=dict(label='seen (value → index)', entries=ents()),
                            vars={'target': target, 'i': i, 'need': need}))
        if need in seen:
            j = seen[need]
            frames.append(frame(note=f'Yes: {need} was at index {j}. Answer [{j}, {i}] in O(n), one pass, no nested loop.', line=6,
                                array=arr(pointers={'i': i}, marks={str(j): 'hit', str(i): 'hit'}),
                                map=dict(label='seen (value → index)', entries=ents(), hit=str(need)), vars={'target': target, 'i': i, 'need': need}))
            break
        seen[x] = i
        frames.append(frame(note=f'No. Remember {x} → {i} so a later number can pair with it.', line=7,
                            array=arr(pointers={'i': i}, marks={str(i): 'done'}),
                            map=dict(label='seen (value → index)', entries=ents(), hit=str(x)), vars={'target': target, 'i': i, 'need': need}))
    return dict(title='Two Sum: one pass with a hash map', code=code, frames=frames)


def container_with_most_water():
    h = [1, 8, 6, 2, 5, 4, 8, 3, 7]
    code = [
        'int maxArea(int[] h) {',
        '  int l = 0, r = h.length - 1, best = 0;',
        '  while (l < r) {',
        '    int area = Math.min(h[l], h[r]) * (r - l);',
        '    best = Math.max(best, area);',
        '    if (h[l] < h[r]) l++;',
        '    else r--;',
        '  }',
        '  return best;',
        '}',
    ]
    l, r, best, frames = 0, len(h) - 1, 0, []
    frames.append(frame(note='Start with the widest container: one pointer at each end.', line=2,
                        array=dict(label='height', values=h, pointers={'l': l, 'r': r}, range=[l, r]), vars={'best': 0}))
    while l < r:
        area = min(h[l], h[r]) * (r - l)
        nb = max(best, area)
        frames.append(frame(note=f'Area = min({h[l]}, {h[r]}) × ({r} − {l}) = {area}.' + (f' New best {nb}.' if nb > best else f' Best stays {best}.'),
                            line=5, array=dict(label='height', values=h, pointers={'l': l, 'r': r}, range=[l, r],
                                               marks={str(l): 'cur', str(r): 'cur'}), vars={'l': l, 'r': r, 'area': area, 'best': nb}))
        best = nb
        if h[l] < h[r]:
            note, line = f'h[l] = {h[l]} is the shorter wall: it caps every narrower container, so move l inward.', 6
            l += 1
        else:
            note, line = f'h[r] = {h[r]} is the shorter (or equal) wall: move r inward.', 7
            r -= 1
        frames.append(frame(note=note, line=line, array=dict(label='height', values=h, pointers={'l': l, 'r': r}, range=[l, r]),
                            vars={'l': l, 'r': r, 'best': best}))
    frames.append(frame(note=f'The pointers met. Best area = {best}, in O(n): each step discards one wall for good.', line=9,
                        array=dict(label='height', values=h, pointers={'l': l}), vars={'best': best}))
    return dict(title='Container With Most Water: two pointers', code=code, frames=frames)


def longest_substring():
    s = 'abcabcbb'
    code = [
        'int lengthOfLongestSubstring(String s) {',
        '  Map<Character, Integer> last = new HashMap<>();',
        '  int best = 0, left = 0;',
        '  for (int right = 0; right < s.length(); right++) {',
        '    char c = s.charAt(right);',
        '    if (last.containsKey(c) && last.get(c) >= left)',
        '      left = last.get(c) + 1;          // jump past the repeat',
        '    last.put(c, right);',
        '    best = Math.max(best, right - left + 1);',
        '  }',
        '  return best;',
        '}',
    ]
    vals = list(s)
    last, best, left, frames = {}, 0, 0, []
    ents = lambda: [[k, v] for k, v in last.items()]
    frames.append(frame(note='A window [left, right] with no repeated letter. right grows one step at a time.', line=3,
                        array=dict(label='s', values=vals), map=dict(label='last index of each char', entries=[]), vars={'best': 0}))
    for right, c in enumerate(s):
        if c in last and last[c] >= left:
            old = left
            left = last[c] + 1
            frames.append(frame(note=f"'{c}' repeats (last seen at {last[c]}), so left jumps from {old} to {left}.", line=7,
                                array=dict(label='s', values=vals, pointers={'left': left, 'right': right}, range=[left, right], marks={str(right): 'bad'}),
                                map=dict(label='last index of each char', entries=ents(), hit=c), vars={'left': left, 'right': right, 'best': best}))
        last[c] = right
        nb = max(best, right - left + 1)
        frames.append(frame(note=f"Window '{s[left:right + 1]}' has length {right - left + 1}." + (f' New best {nb}.' if nb > best else ''),
                            line=9, array=dict(label='s', values=vals, pointers={'left': left, 'right': right}, range=[left, right], marks={str(right): 'cur'}),
                            map=dict(label='last index of each char', entries=ents(), hit=c), vars={'left': left, 'right': right, 'best': nb}))
        best = nb
    frames.append(frame(note=f'Done: longest window without repeats = {best}. Each index enters once and left only moves forward: O(n).', line=11,
                        array=dict(label='s', values=vals), vars={'best': best}))
    return dict(title='Longest substring without repeating characters: sliding window', code=code, frames=frames)


def subarray_sum():
    nums, k = [1, 2, 1, -1, 3, 1], 3
    code = [
        'int subarraySum(int[] nums, int k) {',
        '  Map<Integer, Integer> count = new HashMap<>();   // prefix sum -> times seen',
        '  count.put(0, 1);',
        '  int sum = 0, ans = 0;',
        '  for (int x : nums) {',
        '    sum += x;',
        '    ans += count.getOrDefault(sum - k, 0);',
        '    count.merge(sum, 1, Integer::sum);',
        '  }',
        '  return ans;',
        '}',
    ]
    count, s, ans, frames = {0: 1}, 0, 0, []
    ents = lambda: [[str(a), b] for a, b in count.items()]
    frames.append(frame(note=f'A subarray (i, j] sums to k when prefix[j] − prefix[i] = {k}. Seed the map with prefix 0 seen once.', line=3,
                        array=dict(label='nums', values=nums), map=dict(label='prefix sum → count', entries=ents()), vars={'k': k, 'sum': 0, 'ans': 0}))
    for i, x in enumerate(nums):
        s += x
        add = count.get(s - k, 0)
        ans += add
        frames.append(frame(note=f'sum = {s}. Need an earlier prefix of {s} − {k} = {s - k}: seen {add} time(s), so ans += {add}.', line=7,
                            array=dict(label='nums', values=nums, pointers={'j': i}, range=[0, i]),
                            map=dict(label='prefix sum → count', entries=ents(), hit=str(s - k) if add else None), vars={'k': k, 'sum': s, 'ans': ans}))
        count[s] = count.get(s, 0) + 1
        frames.append(frame(note=f'Record prefix {s} (now seen {count[s]} time(s)).', line=8,
                            array=dict(label='nums', values=nums, pointers={'j': i}, range=[0, i]),
                            map=dict(label='prefix sum → count', entries=ents(), hit=str(s)), vars={'k': k, 'sum': s, 'ans': ans}))
    frames.append(frame(note=f'{ans} subarrays sum to {k}. One pass, O(n), and it works with negative numbers (a sliding window would not).', line=10,
                        array=dict(label='nums', values=nums), map=dict(label='prefix sum → count', entries=ents()), vars={'ans': ans}))
    return dict(title='Subarray sum equals K: prefix sums + hash map', code=code, frames=frames)


def rotated_search():
    a, t = [4, 5, 6, 7, 0, 1, 2], 0
    code = [
        'int search(int[] a, int t) {',
        '  int lo = 0, hi = a.length - 1;',
        '  while (lo <= hi) {',
        '    int mid = (lo + hi) >>> 1;',
        '    if (a[mid] == t) return mid;',
        '    if (a[lo] <= a[mid]) {                 // left half is sorted',
        '      if (a[lo] <= t && t < a[mid]) hi = mid - 1;',
        '      else lo = mid + 1;',
        '    } else {                               // right half is sorted',
        '      if (a[mid] < t && t <= a[hi]) lo = mid + 1;',
        '      else hi = mid - 1;',
        '    }',
        '  }',
        '  return -1;',
        '}',
    ]
    lo, hi, frames = 0, len(a) - 1, []
    A = lambda **k: dict(label='a (rotated)', values=a, **k)
    frames.append(frame(note=f'Find {t}. One half around mid is always sorted; check whether the target lies inside that half.', line=2,
                        array=A(pointers={'lo': lo, 'hi': hi}, range=[lo, hi]), vars={'t': t}))
    while lo <= hi:
        mid = (lo + hi) // 2
        if a[mid] == t:
            frames.append(frame(note=f'a[{mid}] = {t}. Found in O(log n).', line=5,
                                array=A(pointers={'lo': lo, 'mid': mid, 'hi': hi}, range=[lo, hi], marks={str(mid): 'hit'}), vars={'lo': lo, 'mid': mid, 'hi': hi}))
            break
        frames.append(frame(note=f'mid = {mid}, a[mid] = {a[mid]} ≠ {t}.', line=4,
                            array=A(pointers={'lo': lo, 'mid': mid, 'hi': hi}, range=[lo, hi], marks={str(mid): 'cur'}), vars={'lo': lo, 'mid': mid, 'hi': hi}))
        if a[lo] <= a[mid]:
            if a[lo] <= t < a[mid]:
                hi, note, line = mid - 1, f'Left half [{a[lo]}..{a[mid]}] is sorted and holds {t}: drop the right half.', 7
            else:
                lo, note, line = mid + 1, f'Left half [{a[lo]}..{a[mid]}] is sorted but {t} is not in it: drop the left half.', 8
        else:
            if a[mid] < t <= a[hi]:
                lo, note, line = mid + 1, f'Right half [{a[mid]}..{a[hi]}] is sorted and holds {t}: drop the left half.', 10
            else:
                hi, note, line = mid - 1, f'Right half [{a[mid]}..{a[hi]}] is sorted but {t} is not in it: drop the right half.', 11
        frames.append(frame(note=note, line=line, array=A(pointers={'lo': lo, 'hi': hi}, range=[lo, hi] if lo <= hi else None),
                            vars={'lo': lo, 'hi': hi}))
    return dict(title='Search in a rotated sorted array: binary search on the sorted half', code=code, frames=frames)


def valid_parentheses():
    s = '([{}])(]'
    code = [
        'boolean isValid(String s) {',
        '  Deque<Character> stack = new ArrayDeque<>();',
        '  for (char c : s.toCharArray()) {',
        "    if (c == '(' || c == '[' || c == '{') stack.push(c);",
        '    else {',
        '      if (stack.isEmpty()) return false;',
        '      char open = stack.pop();',
        '      if (!matches(open, c)) return false;',
        '    }',
        '  }',
        '  return stack.isEmpty();',
        '}',
    ]
    pairs = {')': '(', ']': '[', '}': '{'}
    st, frames = [], []
    frames.append(frame(note='Every closer must match the most recent unmatched opener: that is a stack.', line=2,
                        array=dict(label='s', values=list(s)), stack=dict(items=[])))
    for i, c in enumerate(s):
        if c in '([{':
            st.append(c)
            frames.append(frame(note=f"'{c}' opens: push it.", line=4, array=dict(label='s', values=list(s), pointers={'c': i}, marks={str(i): 'cur'}),
                                stack=dict(items=list(st))))
        else:
            top = st.pop() if st else None
            ok = top == pairs[c]
            frames.append(frame(note=f"'{c}' closes: pop '{top}'. " + ('They match, keep going.' if ok else f"'{top}' does not match '{c}': return false."),
                                line=8 if not ok else 7, array=dict(label='s', values=list(s), pointers={'c': i}, marks={str(i): 'hit' if ok else 'bad'}),
                                stack=dict(items=list(st))))
            if not ok:
                break
    return dict(title='Valid parentheses: a stack of open brackets', code=code, frames=frames)


def kadane():
    nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4]
    code = [
        'int maxSubArray(int[] nums) {',
        '  int cur = nums[0], best = nums[0];',
        '  for (int i = 1; i < nums.length; i++) {',
        '    cur = Math.max(nums[i], cur + nums[i]);   // extend or restart',
        '    best = Math.max(best, cur);',
        '  }',
        '  return best;',
        '}',
    ]
    cur = best = nums[0]
    start, bs, be, frames = 0, 0, 0, []
    frames.append(frame(note='cur = best sum of a subarray ending here. Start with the first element.', line=2,
                        array=dict(label='nums', values=nums, pointers={'i': 0}, range=[0, 0]), vars={'cur': cur, 'best': best}))
    for i in range(1, len(nums)):
        x = nums[i]
        if x > cur + x:
            note = f'{x} alone beats {cur} + {x} = {cur + x}: restart the subarray at i = {i}.'
            cur, start = x, i
        else:
            note = f'Extend: {cur} + {x} = {cur + x}.'
            cur = cur + x
        if cur > best:
            best, bs, be = cur, start, i
            note += f' New best {best}.'
        frames.append(frame(note=note, line=4, array=dict(label='nums', values=nums, pointers={'i': i}, range=[start, i]), vars={'cur': cur, 'best': best}))
    frames.append(frame(note=f'Best subarray is indices {bs}..{be} with sum {best}. O(n), O(1) space.', line=7,
                        array=dict(label='nums', values=nums, range=[bs, be], marks={str(j): 'hit' for j in range(bs, be + 1)}), vars={'best': best}))
    return dict(title="Maximum subarray: Kadane's algorithm", code=code, frames=frames)


def islands():
    g = [list('11000'), list('11010'), list('00100'), list('00011')]
    code = [
        'int numIslands(char[][] g) {',
        '  int count = 0;',
        '  for (int r = 0; r < g.length; r++)',
        '    for (int c = 0; c < g[0].length; c++)',
        "      if (g[r][c] == '1') { count++; sink(g, r, c); }",
        '  return count;',
        '}',
        'void sink(char[][] g, int r, int c) {',
        "  if (r < 0 || c < 0 || r >= g.length || c >= g[0].length || g[r][c] != '1') return;",
        "  g[r][c] = '0';                          // visited",
        '  sink(g, r + 1, c); sink(g, r - 1, c); sink(g, r, c + 1); sink(g, r, c - 1);',
        '}',
    ]
    view = [row[:] for row in g]
    frames, count, labels = [], 0, 'ABCDEFG'
    snap = lambda marks=None: dict(cells=[row[:] for row in view], marks=marks)
    frames.append(frame(note="Scan every cell. Each time we meet unvisited land ('1'), it is a new island: count it and sink all of it.",
                        line=3, grid=snap(), vars={'count': 0}))

    def sink(r, c, lab):
        if r < 0 or c < 0 or r >= len(g) or c >= len(g[0]) or g[r][c] != '1':
            return
        g[r][c] = '0'
        view[r][c] = lab
        frames.append(frame(note=f'Sink ({r},{c}) and spread to its 4 neighbours.', line=10, grid=snap({f'{r},{c}': 'cur'}), vars={'count': count, 'r': r, 'c': c}))
        sink(r + 1, c, lab); sink(r - 1, c, lab); sink(r, c + 1, lab); sink(r, c - 1, lab)

    for r in range(len(g)):
        for c in range(len(g[0])):
            if g[r][c] == '1':
                count += 1
                frames.append(frame(note=f'Land at ({r},{c}) not visited yet: island #{count}.', line=5, grid=snap({f'{r},{c}': 'hit'}),
                                    vars={'count': count, 'r': r, 'c': c}))
                sink(r, c, labels[count - 1])
    frames.append(frame(note=f'Scan finished: {count} islands. Every cell is visited at most twice: O(rows × cols).', line=6, grid=snap(), vars={'count': count}))
    return dict(title='Number of islands: scan + flood fill (DFS)', code=code, colors={'1': 'c1', '0': 'c2', 'A': 'c3', 'B': 'c3', 'C': 'c3', 'D': 'c3'}, frames=frames)


def rotting_oranges():
    g = [[2, 1, 1, 0], [1, 1, 0, 0], [0, 1, 1, 1], [0, 0, 1, 1]]
    code = [
        'int orangesRotting(int[][] g) {',
        '  Deque<int[]> q = new ArrayDeque<>(); int fresh = 0;',
        '  // every rotten orange starts in the queue; count the fresh ones',
        '  int minutes = 0;',
        '  while (!q.isEmpty() && fresh > 0) {',
        '    for (int n = q.size(); n > 0; n--) {       // one BFS level = one minute',
        '      int[] p = q.poll();',
        '      for (int[] d : DIRS) {',
        '        int r = p[0] + d[0], c = p[1] + d[1];',
        '        if (inside(r, c) && g[r][c] == 1) { g[r][c] = 2; fresh--; q.add(new int[]{r, c}); }',
        '      }',
        '    }',
        '    minutes++;',
        '  }',
        '  return fresh == 0 ? minutes : -1;',
        '}',
    ]
    R, C = len(g), len(g[0])
    q = [(r, c) for r in range(R) for c in range(C) if g[r][c] == 2]
    fresh = sum(row.count(1) for row in g)
    cells = lambda: [row[:] for row in g]
    fmt = lambda qq: [f'({r},{c})' for r, c in qq]
    frames = [frame(note='Multi-source BFS: all rotten oranges (2) start in the queue at minute 0.', line=3,
                    grid=dict(cells=cells(), marks={f'{r},{c}': 'cur' for r, c in q}), stack=dict(kind='queue', items=fmt(q)), vars={'minutes': 0, 'fresh': fresh})]
    minutes = 0
    while q and fresh:
        nq = []
        for r, c in q:
            for dr, dc in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                rr, cc = r + dr, c + dc
                if 0 <= rr < R and 0 <= cc < C and g[rr][cc] == 1:
                    g[rr][cc] = 2
                    fresh -= 1
                    nq.append((rr, cc))
        minutes += 1
        q = nq
        frames.append(frame(note=f'Minute {minutes}: the rot spreads one step from every orange in the queue; {len(nq)} more rotted, {fresh} fresh left.',
                            line=13, grid=dict(cells=cells(), marks={f'{r},{c}': 'cur' for r, c in nq}), stack=dict(kind='queue', items=fmt(nq)),
                            vars={'minutes': minutes, 'fresh': fresh}))
    frames.append(frame(note=f'No fresh oranges left after {minutes} minutes. (If any fresh one were unreachable, the answer would be −1.)', line=15,
                        grid=dict(cells=cells()), vars={'minutes': minutes, 'fresh': fresh}))
    return dict(title='Rotting oranges: multi-source BFS, one level per minute', code=code, colors={'2': 'c4', '1': 'c3', '0': 'c5'}, frames=frames)


def course_schedule():
    n = 6
    pre = [[1, 0], [2, 0], [3, 1], [3, 2], [4, 3], [5, 3], [5, 4]]
    code = [
        'int[] findOrder(int n, int[][] pre) {',
        '  List<List<Integer>> next = adjacency(n, pre);   // prereq -> courses it unlocks',
        '  int[] indeg = new int[n];',
        '  for (int[] p : pre) indeg[p[0]]++;',
        '  Deque<Integer> q = new ArrayDeque<>();',
        '  for (int i = 0; i < n; i++) if (indeg[i] == 0) q.add(i);',
        '  List<Integer> order = new ArrayList<>();',
        '  while (!q.isEmpty()) {',
        '    int c = q.poll(); order.add(c);',
        '    for (int d : next.get(c)) if (--indeg[d] == 0) q.add(d);',
        '  }',
        '  return order.size() == n ? toArray(order) : new int[0];   // a cycle leaves courses out',
        '}',
    ]
    nxt = {i: [] for i in range(n)}
    indeg = [0] * n
    for a, b in pre:
        nxt[b].append(a)
        indeg[a] += 1
    edges = [[str(k), ', '.join(map(str, v)) or '—'] for k, v in nxt.items()]
    q = [i for i in range(n) if indeg[i] == 0]
    order, frames = [], []
    frames.append(frame(note='Count prerequisites (indegree) per course. Courses with none can be taken now.', line=6,
                        array=dict(label='indegree per course', values=indeg[:], marks={str(i): 'hit' for i in q}),
                        map=dict(label='unlocks', entries=edges), stack=dict(kind='queue', items=q[:]), vars={'order': '[]'}))
    while q:
        c = q.pop(0)
        order.append(c)
        freed = []
        for d in nxt[c]:
            indeg[d] -= 1
            if indeg[d] == 0:
                q.append(d)
                freed.append(d)
        frames.append(frame(note=f'Take course {c}. Its dependents lose one prerequisite' + (f'; {", ".join(map(str, freed))} now ready.' if freed else '.'),
                            line=10, array=dict(label='indegree per course', values=indeg[:], marks={**{str(x): 'done' for x in order}, **{str(x): 'hit' for x in freed}}),
                            map=dict(label='unlocks', entries=edges, hit=str(c)), stack=dict(kind='queue', items=q[:]), vars={'order': str(order)}))
    frames.append(frame(note=f'All {n} courses ordered: {order}. With a cycle, some indegree never reaches 0 and the order comes up short.', line=12,
                        array=dict(label='indegree per course', values=indeg[:]), vars={'order': str(order)}))
    return dict(title="Course schedule II: topological sort (Kahn's algorithm)", code=code, frames=frames)


def decode_ways():
    s = '12261'
    code = [
        'int numDecodings(String s) {',
        '  int n = s.length(); int[] dp = new int[n + 1];   // dp[i] = ways to decode the first i digits',
        "  dp[0] = 1; dp[1] = s.charAt(0) == '0' ? 0 : 1;",
        '  for (int i = 2; i <= n; i++) {',
        "    int one = s.charAt(i - 1) - '0', two = Integer.parseInt(s.substring(i - 2, i));",
        '    if (one >= 1) dp[i] += dp[i - 1];',
        '    if (two >= 10 && two <= 26) dp[i] += dp[i - 2];',
        '  }',
        '  return dp[n];',
        '}',
    ]
    n = len(s)
    dp = [0] * (n + 1)
    dp[0], dp[1] = 1, (0 if s[0] == '0' else 1)
    frames = [frame(note=f'dp[i] = ways to decode the first i digits of "{s}". dp[0] = 1 (empty), dp[1] = 1.', line=3,
                    array=dict(label='dp', values=dp[:], marks={'0': 'done', '1': 'done'}), vars={'s': s})]
    for i in range(2, n + 1):
        one, two = int(s[i - 1]), int(s[i - 2:i])
        parts = []
        if one >= 1:
            dp[i] += dp[i - 1]
            parts.append(f"'{one}' alone → dp[{i - 1}] = {dp[i - 1]}")
        if 10 <= two <= 26:
            dp[i] += dp[i - 2]
            parts.append(f"'{two}' as a pair → dp[{i - 2}] = {dp[i - 2]}")
        marks = {str(i): 'cur'}
        if one >= 1: marks[str(i - 1)] = 'hit'
        if 10 <= two <= 26: marks[str(i - 2)] = 'hit'
        frames.append(frame(note=f'i = {i}: ' + '; '.join(parts) + f'. dp[{i}] = {dp[i]}.', line=7,
                            array=dict(label='dp', values=dp[:], pointers={'i': i}, marks=marks), vars={'s': s, 'one': one, 'two': two}))
    frames.append(frame(note=f'"{s}" decodes {dp[n]} ways. Only the last two dp values are needed, so O(1) space is possible.', line=9,
                        array=dict(label='dp', values=dp[:], marks={str(n): 'hit'}), vars={'answer': dp[n]}))
    return dict(title='Decode ways: 1-D dynamic programming', code=code, frames=frames)


# ── Java ─────────────────────────────────────────────────────────────────────────────────────

def java_hash(s):
    h = 0
    for ch in s:
        h = (31 * h + ord(ch)) & 0xFFFFFFFF
    return h - (1 << 32) if h >= 1 << 31 else h


def hashmap_put():
    code = [
        'V put(K key, V value) {',
        '  int h = key.hashCode();',
        '  h = h ^ (h >>> 16);                     // mix high bits into low bits',
        '  int i = h & (table.length - 1);         // bucket index (length is a power of 2)',
        '  if (table[i] == null) table[i] = new Node<>(h, key, value);',
        '  else appendOrReplace(table[i], key, value);   // equals() walk; treeify at 8',
        '  if (++size > threshold) resize();       // threshold = 0.75 × capacity',
        '  return null;',
        '}',
    ]
    cap, size, table, frames = 4, 0, None, []
    table = [[] for _ in range(cap)]
    spread = lambda h: (h ^ ((h & 0xFFFFFFFF) >> 16))
    show = lambda: ['·' if not b else ' → '.join(b) for b in table]
    frames.append(frame(note='An empty HashMap with capacity 4 (real default: 16) and load factor 0.75, so it resizes after 3 entries.', line=1,
                        array=dict(label='table (buckets)', values=show()), vars={'size': 0, 'capacity': cap, 'threshold': int(cap * 0.75)}))
    for key in ['cat', 'Aa', 'BB', 'dog']:
        h = java_hash(key)
        sp = spread(h)
        i = sp & (cap - 1)
        collide = bool(table[i])
        table[i].append(key)
        size += 1
        frames.append(frame(note=f'put("{key}"): hashCode = {h}, spread, index = hash & {cap - 1} = {i}.' +
                            (f' Bucket {i} is taken: compare with equals(), different key, so chain it.' + (' ("Aa" and "BB" have the same hashCode!)' if key == 'BB' else '') if collide else ' Empty bucket: store the node.'),
                            line=6 if collide else 5, array=dict(label='table (buckets)', values=show(), pointers={'i': i}, marks={str(i): 'bad' if collide else 'hit'}),
                            vars={'key': key, 'hashCode': h, 'index': i, 'size': size, 'capacity': cap, 'threshold': int(cap * 0.75)}))
        if size > cap * 0.75:
            entries = [k for b in table for k in b]
            cap *= 2
            table = [[] for _ in range(cap)]
            for k in entries:
                table[spread(java_hash(k)) & (cap - 1)].append(k)
            frames.append(frame(note=f'size {size} > threshold {int(cap // 2 * 0.75)}: resize to {cap} buckets and re-place every node (index = hash & {cap - 1}). Each node stays or moves by exactly the old capacity.',
                                line=7, array=dict(label='table (buckets)', values=show()), vars={'size': size, 'capacity': cap, 'threshold': int(cap * 0.75)}))
    frames.append(frame(note='get(key) repeats the same steps: hash, index, then walk the bucket with equals(). That is why equals and hashCode must agree.', line=4,
                        array=dict(label='table (buckets)', values=show()), vars={'size': size, 'capacity': cap}))
    return dict(title='HashMap.put: hashing, collisions and resize (simulated with real Java hashCodes)', code=code, frames=frames)


# ── Spring and system design (stage flows) ──────────────────────────────────────────────────

def stages_spec(title, stages, steps, code=None):
    frames = []
    for st in steps:
        f = dict(stage=st['stage'], note=st['note'])
        if 'vars' in st: f['vars'] = st['vars']
        if 'line' in st: f['line'] = st['line']
        frames.append(f)
    spec = dict(title=title, stages=stages, frames=frames)
    if code: spec['code'] = code
    return spec


def bean_lifecycle():
    code = [
        '@Component',
        'class OddsCache implements ApplicationContextAware {',
        '  private final OddsClient client;',
        '  OddsCache(OddsClient client) { this.client = client; }',
        '  public void setApplicationContext(ApplicationContext ctx) { }',
        '  @PostConstruct void warmUp() { client.loadAll(); }',
        '  @Transactional public void refresh() { /* ... */ }',
        '  @PreDestroy void close() { client.close(); }',
        '}',
    ]
    stages = ['Instantiate', 'Inject dependencies', 'Aware callbacks', 'BeanPostProcessor: before init', 'Init (@PostConstruct)',
              'BeanPostProcessor: after init → proxy', 'In use', 'Destroy (@PreDestroy)']
    steps = [
        dict(stage=0, line=4, note='The container calls the constructor. With constructor injection, OddsClient is passed in right here.', vars={'bean': 'raw object', 'proxy': False}),
        dict(stage=1, line=3, note='Field and setter injection happen now, after the constructor. (So a field-injected dependency is still null inside the constructor.)', vars={'bean': 'wired', 'proxy': False}),
        dict(stage=2, line=5, note='Aware callbacks hand the bean container objects it asked for: its name, the BeanFactory, the ApplicationContext.', vars={'bean': 'wired', 'proxy': False}),
        dict(stage=3, note='Every BeanPostProcessor gets postProcessBeforeInitialization: e.g. the one that handles @PostConstruct and other annotations.', vars={'bean': 'wired', 'proxy': False}),
        dict(stage=4, line=6, note='Init hooks in order: @PostConstruct, then afterPropertiesSet(), then a custom initMethod. All dependencies are ready, so warm the cache here.', vars={'bean': 'initialised', 'proxy': False}),
        dict(stage=5, line=7, note='postProcessAfterInitialization: refresh() is @Transactional, so the auto-proxy creator wraps the bean in a CGLIB proxy. Other beans get the proxy, not the raw object.', vars={'bean': 'initialised', 'proxy': True}),
        dict(stage=6, note='The bean serves calls. A call to refresh() goes through the proxy, which opens the transaction. (A call from inside the class would skip it.)', vars={'bean': 'in use', 'proxy': True}),
        dict(stage=7, line=8, note='context.close() on shutdown: @PreDestroy, then destroy(), then a custom destroyMethod. Prototype beans never get this step.', vars={'bean': 'destroyed', 'proxy': True}),
    ]
    return stages_spec('Spring bean lifecycle, step by step', stages, steps, code)


def mvc_request():
    stages = ['Tomcat worker thread', 'Filter chain', 'DispatcherServlet', 'HandlerMapping', 'Interceptor preHandle', 'HandlerAdapter: bind arguments',
              '@RestController method', 'HttpMessageConverter → JSON', 'Interceptor afterCompletion', 'Response sent']
    steps = [
        dict(stage=0, note='Tomcat accepts the connection and hands the request to a worker thread from its pool (default max 200).', vars={'request': 'GET /bets/42', 'thread': 'http-nio-8090-exec-3'}),
        dict(stage=1, note='Servlet filters run first, for every request: Spring Security (JWT check), CORS, logging with a correlation id.', vars={'request': 'GET /bets/42', 'user': 'from JWT'}),
        dict(stage=2, note='The single front-controller servlet receives it. Spring Boot registered it at "/".', vars={'request': 'GET /bets/42'}),
        dict(stage=3, note='Which method handles GET /bets/{id}? RequestMappingHandlerMapping finds BetController.get(Long id).', vars={'handler': 'BetController.get'}),
        dict(stage=4, note='HandlerInterceptors run around the chosen handler; preHandle can stop the request (e.g. a feature flag).', vars={'handler': 'BetController.get'}),
        dict(stage=5, note='Argument resolvers bind @PathVariable id = 42 (and a @RequestBody via Jackson for POSTs); @Valid runs here.', vars={'id': 42}),
        dict(stage=6, note='Your code runs, usually calling a service bean (through its proxy if it is @Transactional).', vars={'id': 42, 'returns': 'BetDto'}),
        dict(stage=7, note='The return value is written as JSON by MappingJackson2HttpMessageConverter. Exceptions go to @ControllerAdvice instead.', vars={'status': 200, 'body': '{"id":42,...}'}),
        dict(stage=8, note='afterCompletion runs even on errors: a good place for timing and cleanup.', vars={'status': 200}),
        dict(stage=9, note='The response goes back and the thread returns to the pool. A slow downstream call holds this thread the whole time: that is why timeouts matter.', vars={'status': 200, 'thread': 'back in pool'}),
    ]
    return stages_spec('A request through Spring MVC', stages, steps)


def aop_call():
    stages = ['Caller', 'Proxy', '@Around (before proceed)', '@Before', 'Target method', '@AfterReturning', '@After', '@Around (after proceed)', 'Back to caller']
    steps = [
        dict(stage=0, note='OrderController calls betService.place(slip). It holds a reference to the proxy, not the real BetService.', vars={'call': 'place(slip)'}),
        dict(stage=1, note='The proxy (CGLIB subclass) looks up the advice chain that matches this method.', vars={'call': 'place(slip)', 'matched': '3 advices'}),
        dict(stage=2, note='@Around starts first, e.g. starts a timer, then calls pjp.proceed().', vars={'timer': 'started'}),
        dict(stage=3, note='@Before runs: an auth or validation check. Throwing here stops the call.', vars={'timer': 'started'}),
        dict(stage=4, note='The real place() method runs on the target object.', vars={'result': 'BetId(981)'}),
        dict(stage=5, note='@AfterReturning sees the result (on an exception, @AfterThrowing runs instead).', vars={'result': 'BetId(981)'}),
        dict(stage=6, note='@After always runs, like finally.', vars={'result': 'BetId(981)'}),
        dict(stage=7, note='@Around continues after proceed(): logs "place took 12 ms" and returns the result (it could also change it).', vars={'timer': '12 ms'}),
        dict(stage=8, note='The caller gets the result. Trap: if place() calls this.validate() inside BetService, that call never touches the proxy, so no advice runs.', vars={'result': 'BetId(981)'}),
    ]
    return stages_spec('A call through a Spring AOP proxy', stages, steps)


def transactional_call():
    stages = ['Call reaches the proxy', 'TransactionInterceptor reads @Transactional', 'Begin: connection, autoCommit=false, bound to thread',
              'Method runs (repositories reuse the connection)', 'Commit or roll back', 'Release the connection']
    steps = [
        dict(stage=0, note='transfer(1, 2, 500) is called on the TransferService proxy.', vars={'run': 'happy path'}),
        dict(stage=1, note='Attributes: propagation REQUIRED, default isolation, rollbackFor InsufficientFundsException.', vars={'run': 'happy path'}),
        dict(stage=2, note='JpaTransactionManager takes a pooled connection, sets autoCommit=false and binds it (and the EntityManager) to this thread.', vars={'connection': 'conn-7', 'autoCommit': False}),
        dict(stage=3, note='debit and credit change managed entities; dirty checking will flush them. Every repository call on this thread uses conn-7.', vars={'connection': 'conn-7', 'dirty': 2}),
        dict(stage=4, note='Normal return: flush, then COMMIT.', vars={'outcome': 'COMMIT'}),
        dict(stage=5, note='Unbind from the thread, return conn-7 to the pool.', vars={'connection': 'returned'}),
        dict(stage=3, note='Second run: transfer(1, 2, 5000) throws InsufficientFundsException inside the method.', vars={'run': 'failure', 'exception': 'InsufficientFundsException'}),
        dict(stage=4, note='The exception matches rollbackFor, so ROLLBACK. (A checked exception without rollbackFor would COMMIT.)', vars={'outcome': 'ROLLBACK'}),
        dict(stage=5, note='Connection released, exception rethrown to the caller.', vars={'connection': 'returned'}),
    ]
    return stages_spec('@Transactional: what the proxy does around your method', stages, steps)


def url_redirect():
    stages = ['Client GET /aZ3kQ9', 'CDN / edge', 'Load balancer', 'Redirect service', 'Redis cache', 'Database (on a miss)', '302 Location', 'Click event → Kafka']
    steps = [
        dict(stage=0, note='A browser opens https://sho.rt/aZ3kQ9. Reads outnumber writes about 100 to 1, so this path must be fast.', vars={'latency': '0 ms'}),
        dict(stage=1, note='Popular links can be cached at the edge, but we usually skip that: every click must be counted.', vars={'latency': '5 ms'}),
        dict(stage=2, note='The load balancer picks a healthy, stateless redirect instance.', vars={'latency': '6 ms'}),
        dict(stage=3, note='Decode the short code and look it up.', vars={'latency': '7 ms'}),
        dict(stage=4, note='Cache-aside: GET url:aZ3kQ9. About 90%+ of lookups hit here in ~1 ms.', vars={'latency': '8 ms', 'cache': 'hit'}),
        dict(stage=5, note='On a miss: read by primary key from the database, then SET it in Redis with a TTL.', vars={'latency': '15 ms (miss)', 'cache': 'filled'}),
        dict(stage=6, note='Reply 302 with Location: the long URL (302, not 301, so the browser does not cache it and clicks keep being counted).', vars={'status': 302}),
        dict(stage=7, note='Analytics is asynchronous: publish a click event to Kafka; the redirect never waits for it.', vars={'status': 302, 'async': 'click event'}),
    ]
    return stages_spec('URL shortener: the redirect path', stages, steps)


def payment_flow():
    stages = ['POST /payments + Idempotency-Key', 'Insert PENDING row', 'Call PSP (timeout)', 'PSP webhook', 'Mark SUCCEEDED (conditional update)', 'Outbox event', 'Reconciliation job']
    steps = [
        dict(stage=0, note='The client sends a payment with an Idempotency-Key. A retry with the same key returns the first result instead of charging twice.', vars={'status': '—'}),
        dict(stage=1, note='Write the payment as PENDING first (UNIQUE on the key), before calling anyone outside.', vars={'status': 'PENDING'}),
        dict(stage=2, note='Call the payment provider with a timeout. If we crash or time out now, the row is still PENDING, never lost.', vars={'status': 'PENDING'}),
        dict(stage=3, note='The provider confirms asynchronously via a signed webhook (it may send it more than once).', vars={'status': 'PENDING', 'webhook': 'succeeded'}),
        dict(stage=4, note="UPDATE payment SET status='SUCCEEDED' WHERE id=? AND status='PENDING'. Rowcount 0 means a duplicate webhook: ignore it.", vars={'status': 'SUCCEEDED'}),
        dict(stage=5, note='In the same transaction, insert PaymentSucceeded into the outbox; a relay publishes it to Kafka for orders and notifications.', vars={'status': 'SUCCEEDED', 'event': 'queued'}),
        dict(stage=6, note='Every few minutes, PENDING rows older than N minutes are checked against the provider: fix them, or refund if money moved but the order failed.', vars={'status': 'consistent'}),
    ]
    return stages_spec('Payment flow: PENDING first, webhook, outbox, reconciliation', stages, steps)


# ── Targets ──────────────────────────────────────────────────────────────────────────────────

TARGETS = [
    ('data/answers/two-sum.md', two_sum, None),
    ('data/answers/container-with-most-water.md', container_with_most_water, None),
    ('data/answers/longest-substring-without-repeating-characters.md', longest_substring, None),
    ('data/answers/subarray-sum-equals-k.md', subarray_sum, None),
    ('data/answers/search-in-rotated-sorted-array.md', rotated_search, None),
    ('data/answers/valid-parentheses.md', valid_parentheses, None),
    ('data/answers/maximum-subarray.md', kadane, None),
    ('data/answers/number-of-islands.md', islands, None),
    ('data/answers/rotting-oranges.md', rotting_oranges, None),
    ('data/answers/course-schedule-ii.md', course_schedule, None),
    ('data/answers/decode-ways.md', decode_ways, None),
    ('data/answers/hashmap-internals.md', hashmap_put, None),
    ('data/answers/spring-applicationcontext-and-bean-lifecycle.md', bean_lifecycle, None),
    ('data/academy/lessons/L4.md', bean_lifecycle, 'Steps 1–2: instantiate, then populate'),
    ('data/answers/servlets-and-dispatcherservlet.md', mvc_request, None),
    ('data/answers/spring-aop-and-advice-types.md', aop_call, None),
    ('data/answers/spring-transactional-management.md', transactional_call, None),
    ('data/answers/design-url-shortener.md', url_redirect, None),
    ('data/answers/design-payment-system.md', payment_flow, None),
]


def block(spec):
    frames = spec.pop('frames')
    head = json.dumps(spec, ensure_ascii=False, separators=(',', ':'))[:-1]
    body = ',\n'.join(json.dumps(f, ensure_ascii=False, separators=(',', ':')) for f in frames)
    return '```anim\n' + head + ',"frames":[\n' + body + '\n]}\n```'


def place(text, section, anchor):
    # Remove a previous "## Watch it" section.
    text = re.sub(r'\n## Watch it\n.*?(?=\n## |\Z)', '\n', text, flags=re.S)
    lines = text.split('\n')
    heads = [i for i, l in enumerate(lines) if l.startswith('## ')]
    if anchor:
        at = next(i for i in heads if lines[i][3:].strip() == anchor)
    else:
        pic = next((i for i in heads if lines[i][3:].strip() == 'Picture it'), None)
        after = [i for i in heads if pic is not None and i > pic]
        at = after[0] if after else (heads[0] if pic is None and heads else len(lines))
    out = lines[:at] + section.rstrip('\n').split('\n') + [''] + lines[at:]
    return re.sub(r'\n{3,}', '\n\n', '\n'.join(out))


def main():
    for path, gen, anchor in TARGETS:
        p = ROOT / path
        spec = gen()
        n = len(spec['frames'])
        section = ('## Watch it\n\nPress **Play**, or step with **→**. The highlighted code line is the one that just ran.\n\n'
                   if 'code' in spec else '## Watch it\n\nPress **Play**, or step with **→**.\n\n') + block(spec) + '\n'
        text = p.read_text(encoding='utf-8')
        p.write_text(place(text, section, anchor), encoding='utf-8', newline='\n')
        print(f'{path}: {n} frames')


if __name__ == '__main__':
    main()
