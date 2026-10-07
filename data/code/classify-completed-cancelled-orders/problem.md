An online shop logs order status events in chronological order: event `i` says that order `orderIds[i]` reported status `statuses[i]`. A status is one of `"placed"`, `"processed"`, `"shipped"`, `"delivered"` and `"cancelled"`.

Each order follows this lifecycle:

```
(new) --placed--> PLACED --processed--> PROCESSED --shipped--> SHIPPED --delivered--> DELIVERED
                    |                       |                     |
                    +----------cancelled----+---------------------+--> CANCELLED
```

- An order with no events yet is *new*. `"cancelled"` is allowed only from PLACED, PROCESSED or SHIPPED.
- Process the events in order. An event that is **not** a valid transition from the order's current state is ignored — for example a duplicate, a skipped stage (`"shipped"` straight after `"placed"`), any event before `"placed"`, or anything after the order is DELIVERED or CANCELLED.

Return two lists: `[completed, cancelled]`, where `completed` holds the ids of orders that end in DELIVERED and `cancelled` the ids of orders that end in CANCELLED, each in increasing order. Orders still in progress appear in neither list.

**Example 1**
Input: orderIds = [1,2,1,1,2,3,1,3], statuses = ["placed","placed","processed","shipped","cancelled","processed","delivered","placed"]
Output: [[1],[2]]
Why: order 1 goes through all four stages; order 2 is cancelled after being placed; order 3's "processed" arrives before "placed", so it is ignored and order 3 is only PLACED.

**Example 2**
Input: orderIds = [5,5,5], statuses = ["placed","shipped","delivered"]
Output: [[],[]]
Why: "shipped" skips "processed", so it is ignored, and then "delivered" is not valid from PLACED either.

**Constraints**
- 1 ≤ orderIds.length = statuses.length ≤ 10⁴
- 1 ≤ orderIds[i] ≤ 10⁹
- every status is one of the five strings above

**Notes**: each of the two lists may be returned in any order — the checker sorts them. Interviewers mainly look for a clean model here, such as an enum of states with the allowed transitions in one place.
