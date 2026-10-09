**Short answer:** `Comparable` defines a class's natural order inside the class itself, through `compareTo(T other)`, so there is one order. `Comparator` is a separate object with `compare(a, b)`, so you can have many orders (by salary, by name, by department) without touching the class. Since Java 8, you build comparators fluently with `Comparator.comparing(...).thenComparing(...).reversed()`.

## Explanation

| | Comparable | Comparator |
|---|---|---|
| Package | `java.lang` | `java.util` |
| Method | `int compareTo(T o)` | `int compare(T a, T b)` |
| Where | Inside the class | Outside, any number of them |
| Orders | One natural order | Many |
| Used by | `Collections.sort(list)`, `TreeMap`, `TreeSet` by default | `list.sort(cmp)`, `new TreeMap<>(cmp)`, `stream.sorted(cmp)` |

Both return a negative number, zero or a positive number for less than, equal, greater than. `String`, `Integer`, `LocalDate` and enums already implement `Comparable`.

Keep `compareTo` consistent with `equals` when you can. A `TreeSet` treats `compareTo == 0` as a duplicate, so two different employees with the same salary would collapse into one if the comparator only looks at salary.

## Example

```java
public record Employee(int id, String name, String dept, double salary)
        implements Comparable<Employee> {
    @Override public int compareTo(Employee o) {
        return Integer.compare(id, o.id);        // natural order: by id
    }
}

List<Employee> emps = new ArrayList<>(...);

Collections.sort(emps);                           // by id (Comparable)

emps.sort(Comparator.comparing(Employee::name)); // by name

// Department ascending, then salary descending, then name
emps.sort(Comparator.comparing(Employee::dept)
        .thenComparing(Employee::salary, Comparator.reverseOrder())
        .thenComparing(Employee::name));

// Highest salary first
emps.sort(Comparator.comparingDouble(Employee::salary).reversed());

// Nulls last when name may be null
emps.sort(Comparator.comparing(Employee::name,
        Comparator.nullsLast(Comparator.naturalOrder())));
```

Old style, before Java 8:

```java
Collections.sort(emps, new Comparator<Employee>() {
    public int compare(Employee a, Employee b) {
        return Double.compare(a.salary(), b.salary());
    }
});
```

## Pitfalls and follow-ups

- **`reversed()` reverses everything before it.** `comparing(dept).thenComparing(name).reversed()` reverses both dept and name. To reverse only one key, pass `Comparator.reverseOrder()` to that `thenComparing`, as above.
- **Don't subtract to compare:** `return a.age - b.age` can overflow for large or negative values. Use `Integer.compare`, `Long.compare`, `Double.compare`.
- **Primitive keys:** `comparingInt`, `comparingLong`, `comparingDouble` avoid boxing.
- **Is sorting stable?** `List.sort` and `Collections.sort` on objects use TimSort, which is stable: equal elements keep their order. That is why sorting by name first and then by department also works, though `thenComparing` is clearer.
- **Which do you implement in an entity?** Usually neither in the entity; pass a `Comparator` where you need a specific order, or sort in SQL with `ORDER BY` / `Sort`.
