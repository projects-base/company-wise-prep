**Short answer:** Walk the array and look each name up in the dictionary; if it is there, put it in a `LinkedHashMap`. That keeps the array's order, skips names the restaurant does not sell and drops repeats, in O(n) expected time.

**Assumptions (the report gives one sentence, so say these first):**
- The dictionary maps dish name to price in minor units (pence or paise). The array lists the dishes wanted on the menu.
- The menu follows the **array's order**. Dictionary order is usually not meaningful.
- A name missing from the dictionary is skipped. A repeated name appears once.

## Approach

- **Brute force:** for every dictionary entry, scan the array to see if it is wanted. O(d · n), and the result comes out in dictionary order.
- **Key insight:** drive the loop from the array and use the dictionary for what it is good at, an O(1) lookup by key.
- **Optimal:** one pass over the array. `LinkedHashMap` remembers insertion order, and `putIfAbsent` ignores repeats.

Example: prices `{Pizza: 900, Soup: 450, Salad: 600}`, wanted `[Salad, Sushi, Pizza, Salad]`.

| name | in dictionary? | menu after |
|---|---|---|
| Salad | yes, 600 | {Salad=600} |
| Sushi | no | unchanged |
| Pizza | yes, 900 | {Salad=600, Pizza=900} |
| Salad | yes, already on the menu | unchanged |

## Solution

```java
import java.util.*;

class Solution {
    public <K, V> Map<K, V> buildMenu(Map<K, V> dishes, List<K> wanted) {
        Map<K, V> menu = new LinkedHashMap<>();      // keeps the order of `wanted`
        for (K name : wanted) {
            V value = dishes.get(name);
            if (value != null) menu.putIfAbsent(name, value);
        }
        return menu;
    }
}
```

One `get` is enough. Calling `containsKey` and then `get` does the lookup twice.

## Complexity

- **Time:** O(n) expected for `n` wanted names, whatever the dictionary size.
- **Space:** O(m) for the `m` dishes that match.

## Edge cases

- Empty dictionary or empty array: empty menu.
- Nothing matches: empty menu, not `null`.
- Keys differ in case or spaces (`"pizza "` vs `"Pizza"`): normalise both sides, or the lookup misses.
- A dictionary that stores `null` values: this code treats them as absent.
- `Map.of(...)` throws `NullPointerException` on `get(null)`, so reject null names before the loop.

## Variations

- **Keep dictionary order instead:** put the array in a `HashSet` and filter the entries:

```java
import java.util.*;

class DictionaryOrder {
    <K, V> Map<K, V> buildMenu(Map<K, V> dishes, List<K> wanted) {
        Map<K, V> menu = new LinkedHashMap<>(dishes);   // copy, so the input is untouched
        menu.keySet().retainAll(new HashSet<>(wanted));
        return menu;
    }
}
```

- **The array is an exclude list** (for example, sold-out items): use `removeAll` instead of `retainAll`.
- **Report unknown names:** collect misses in a second list, so a typo in the menu config is visible.
- **The dictionary is category → dishes:** filter each list, then drop categories that end up empty.
- **Stream version:** `Collectors.toMap` needs a merge function for repeats and `LinkedHashMap::new` for order, so the loop reads better.

Related: [J6 · Collections internals](../academy/lessons/J6.md).
