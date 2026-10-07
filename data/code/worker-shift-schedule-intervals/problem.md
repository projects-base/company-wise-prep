Shift `i` says that worker `names[i]` is on duty during the half-open time interval `[times[i][0], times[i][1])`. Build the schedule: split the timeline into maximal intervals during which the **set of workers on duty stays the same and is not empty**, and return them in chronological order.

- A worker can have several shifts, and they may overlap or touch. The worker is on duty whenever at least one of their shifts covers the moment, and is listed once.
- Times when nobody is on duty are left out.
- Two consecutive intervals in the answer either have different sets of workers or are separated by a gap with nobody on duty. Adjacent pieces with the same set are merged.

Return each interval as a `Segment(start, end, workers)`. The names inside a segment may be in any order; the judge sorts them before printing `[[start,end,[names…]],…]`.

```java
class Segment {          // provided by the judge, do not redeclare it
    int start, end;
    List<String> workers;
    Segment(int start, int end, List<String> workers) { ... }
}
```

**Example 1**
Input: names = ["Abby","Ben","Carla","Dan"], times = [[10,100],[50,70],[60,120],[150,300]]
Output: [[10,50,["Abby"]],[50,60,["Abby","Ben"]],[60,70,["Abby","Ben","Carla"]],[70,100,["Abby","Carla"]],[100,120,["Carla"]],[150,300,["Dan"]]]

**Example 2**
Input: names = ["Ann","Ann","Bo"], times = [[0,5],[5,9],[3,7]]
Output: [[0,3,["Ann"]],[3,7,["Ann","Bo"]],[7,9,["Ann"]]]
Why: Ann's two shifts touch at 5, so nothing changes there.

**Example 3**
Input: names = ["Zoe"], times = [[1,2]]
Output: [[1,2,["Zoe"]]]

**Constraints**
- 1 ≤ names.length == times.length ≤ 2000
- 0 ≤ times[i][0] < times[i][1] ≤ 10⁹
- names are non-empty strings of English letters
