**Short answer:** Most stream drills are one pipeline: a source (`list.stream()`), a few intermediate steps (`filter`, `map`, `sorted`), and one terminal step (`collect`, `count`, `findFirst`). The heavy lifting is usually in `Collectors`: `groupingBy` with a downstream collector (`counting()`, `maxBy`, `mapping`) answers almost every "per group" question. Say the pipeline out loud before typing it.

## Explanation

Three collectors cover most interview drills:

- `groupingBy(classifier)` builds a `Map<K, List<T>>`.
- `groupingBy(classifier, downstream)` replaces the list with something else: `counting()`, `summingDouble(...)`, `maxBy(...)`, `mapping(...)`.
- `groupingBy(classifier, mapFactory, downstream)` lets you pick the map type. Pass `LinkedHashMap::new` when the order of first appearance matters, `TreeMap::new` when you want sorted keys.

`partitioningBy(predicate)` is the two-bucket version (`true` / `false`). `toMap(k, v, merge)` needs a merge function if keys can repeat, otherwise it throws `IllegalStateException`.

Since Java 16, `stream.toList()` returns an unmodifiable list and is shorter than `collect(Collectors.toList())`.

## Example

Assume `record Employee(String name, String dept, double salary) {}`.

```java
List<Integer> nums = List.of(5, 2, 8, 2, 9, 5, 1);

// 1. Even numbers
List<Integer> evens = nums.stream().filter(n -> n % 2 == 0).toList();

// 2. Group and count (employees per department)
Map<String, Long> perDept = emps.stream()
        .collect(Collectors.groupingBy(Employee::dept, Collectors.counting()));

// 3. Duplicates
Set<Integer> seen = new HashSet<>();
Set<Integer> dups = nums.stream().filter(n -> !seen.add(n)).collect(Collectors.toSet());
// pure version, no side effects:
Set<Integer> dups2 = nums.stream()
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream().filter(e -> e.getValue() > 1)
        .map(Map.Entry::getKey).collect(Collectors.toSet());

// 4. Sort employees by salary desc, then name
List<Employee> sorted = emps.stream()
        .sorted(Comparator.comparingDouble(Employee::salary).reversed()
                .thenComparing(Employee::name))
        .toList();

// 5. Highest-paid employee per department
Map<String, Optional<Employee>> topPaid = emps.stream()
        .collect(Collectors.groupingBy(Employee::dept,
                Collectors.maxBy(Comparator.comparingDouble(Employee::salary))));
// max salary value per department
Map<String, Double> maxSal = emps.stream()
        .collect(Collectors.toMap(Employee::dept, Employee::salary, Math::max));
```

Follow-ups:

```java
String s = "programming";

// Character frequency
Map<Character, Long> freq = s.chars().mapToObj(c -> (char) c)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

// First non-repeated character (LinkedHashMap keeps insertion order)
Optional<Character> firstUnique = s.chars().mapToObj(c -> (char) c)
        .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()))
        .entrySet().stream().filter(e -> e.getValue() == 1)
        .map(Map.Entry::getKey).findFirst();          // 'p'

// Second highest number (distinct, so duplicates of the max don't count)
Optional<Integer> second = nums.stream().distinct()
        .sorted(Comparator.reverseOrder()).skip(1).findFirst();   // 8
```

## Pitfalls and follow-ups

- **Why `distinct()` before second-highest?** Without it, `[9, 9, 8]` returns 9.
- **`maxBy` returns `Optional`.** Wrap with `collectingAndThen(maxBy(...), Optional::get)` if you want the bare value; the group is never empty, so `get` is safe there.
- **The `seen.add` trick is stateful.** It works on a sequential stream but breaks on `parallelStream()`. Mention the pure `groupingBy` version.
- **`toMap` on duplicate keys** throws `IllegalStateException` unless you give a merge function.
- **`String.chars()` returns an `IntStream`,** so you need `mapToObj(c -> (char) c)` to box to `Character`.
- **Is `parallelStream()` faster?** Only for large, CPU-bound, easily splittable data. It uses the common ForkJoinPool, so avoid it for blocking I/O.

Deeper: [H1 · Java idioms for coding interviews](../academy/lessons/H1.md), [A7 · Java 8 → 21](../academy/lessons/A7.md).
