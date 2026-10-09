**Short answer:** Model `Machine` with many `Sensor`s. Each sensor has a type and a `Threshold` rule. Readings flow into a `MonitoringService` that checks the sensor's rule. If the reading is out of range, the `AlertService` raises an `Alert`, at most one open alert per sensor, and notifies listeners. An alert is a small state machine: OPEN → ACKNOWLEDGED → RESOLVED, with IGNORED as another exit. The allowed transitions live in one place, the status enum, so an invalid move is rejected.

## Requirements

- Machines have sensors (temperature, pressure, and later others). Each sensor has min/max thresholds, configurable per sensor.
- A reading outside the range raises an alert with machine, sensor, value, threshold and time.
- No alert storm: while a sensor already has an open (not resolved or ignored) alert, further bad readings update it, for example by counting occurrences and keeping the latest value. They do not create new alerts.
- Users list alerts (filter by machine, status) and move them: OPEN → ACKNOWLEDGED, OPEN/ACKNOWLEDGED → RESOLVED or IGNORED. RESOLVED and IGNORED are terminal.
- Readings arrive concurrently from many sensors.

## Classes

- `Machine` (id, name, sensors). `Sensor` (id, machineId, `SensorType`, `ThresholdRule`).
- `ThresholdRule`: an interface `Optional<String> violation(double value)`. `RangeRule(min, max)` is the first implementation.
- `Reading`: a record of (sensorId, value, timestamp).
- `Alert`: id, sensor, first and last value, count, status, history of `StatusChange(from, to, user, time)`.
- `AlertStatus`: an enum that owns the transition rules.
- `AlertService`: raises alerts, deduplicates them, changes status, and queries.
- `AlertListener`: notification hooks (email, dashboard push).
- `AlertRepository`: storage. In-memory maps for the interview.

## Patterns used

- **State (enum-based state machine)**: the transitions are data in `AlertStatus.canMoveTo`, not `if` chains spread across services. If states get behaviour (for example, re-notify when an ACKNOWLEDGED alert is older than an SLA), move to state classes.
- **Strategy**: `ThresholdRule`. Rate-of-change or "N bad readings in M seconds" rules plug in without touching the service (OCP).
- **Observer**: `AlertListener`s are told about raised or changed alerts. The service does not know about email or SMS.
- **Repository**: storage behind an interface (DIP).

## Code

```java
public enum AlertStatus {
    OPEN, ACKNOWLEDGED, RESOLVED, IGNORED;

    public boolean canMoveTo(AlertStatus next) {
        return switch (this) {
            case OPEN -> next == ACKNOWLEDGED || next == RESOLVED || next == IGNORED;
            case ACKNOWLEDGED -> next == RESOLVED || next == IGNORED;
            case RESOLVED, IGNORED -> false;
        };
    }
    public boolean isOpen() { return this == OPEN || this == ACKNOWLEDGED; }
}

public interface ThresholdRule { Optional<String> violation(double value); }

public record RangeRule(double min, double max) implements ThresholdRule {
    public Optional<String> violation(double v) {
        if (v < min) return Optional.of(v + " < min " + min);
        if (v > max) return Optional.of(v + " > max " + max);
        return Optional.empty();
    }
}

public record Reading(String sensorId, double value, Instant at) {}
public record StatusChange(AlertStatus from, AlertStatus to, String user, Instant at) {}

public final class Alert {
    private final String id;
    private final String sensorId;
    private AlertStatus status = AlertStatus.OPEN;
    private double lastValue;
    private int occurrences = 1;
    private final List<StatusChange> history = new ArrayList<>();

    Alert(String id, String sensorId, double value) { this.id = id; this.sensorId = sensorId; this.lastValue = value; }

    synchronized void recordRepeat(double value) { lastValue = value; occurrences++; }

    synchronized void moveTo(AlertStatus next, String user, Instant at) {
        if (!status.canMoveTo(next)) {
            throw new IllegalStateException(status + " -> " + next + " not allowed");
        }
        history.add(new StatusChange(status, next, user, at));
        status = next;
    }
    public synchronized AlertStatus status() { return status; }
    public String id() { return id; }
    public String sensorId() { return sensorId; }
}

public final class AlertService {
    private final Map<String, Sensor> sensors;                        // sensorId -> sensor
    private final ConcurrentMap<String, Alert> openBySensor = new ConcurrentHashMap<>();
    private final Map<String, Alert> byId = new ConcurrentHashMap<>();
    private final List<AlertListener> listeners;
    private final Clock clock;

    // constructor omitted

    public void onReading(Reading r) {
        Sensor sensor = sensors.get(r.sensorId());
        if (sensor == null || sensor.rule().violation(r.value()).isEmpty()) return;

        // compute() runs atomically per key: two bad readings cannot both create an alert
        boolean[] created = {false};
        Alert alert = openBySensor.compute(r.sensorId(), (id, existing) -> {
            if (existing != null && existing.status().isOpen()) {
                existing.recordRepeat(r.value());
                return existing;
            }
            created[0] = true;
            Alert a = new Alert(UUID.randomUUID().toString(), id, r.value());
            byId.put(a.id(), a);
            return a;
        });
        if (created[0]) listeners.forEach(l -> l.onRaised(alert));   // notify outside compute()
    }

    public void transition(String alertId, AlertStatus next, String user) {
        Alert a = Optional.ofNullable(byId.get(alertId))
                .orElseThrow(() -> new NoSuchElementException(alertId));
        a.moveTo(next, user, clock.instant());
        if (!next.isOpen()) openBySensor.remove(a.sensorId(), a);   // free the slot for a new alert
        listeners.forEach(l -> l.onStatusChanged(a, next));
    }
}
```

## Extensions

- **Concurrency:** `compute` makes "check for an open alert, else create one" atomic per sensor. Keep slow work, such as listener calls, out of the lambda. `remove(key, value)` only removes this exact alert, so it cannot drop a newer one.
- **Hysteresis and debounce:** raise only after N consecutive bad readings, and auto-resolve after M good ones. This avoids flapping around the threshold. It is a new `ThresholdRule` or a decorator around one.
- **Severity and escalation:** WARNING and CRITICAL bands. If an alert stays OPEN past an SLA, a scheduled job escalates it (Observer again).
- **Scale (the HLD angle):** sensors publish to Kafka, partitioned by sensorId so one sensor's readings stay ordered. Stateless evaluators consume them, and alerts go in PostgreSQL with a partial unique index on `(sensor_id) WHERE status IN ('OPEN','ACKNOWLEDGED')`. That index enforces the one-open-alert rule in the database.
- **Audit:** the `history` list answers who acknowledged what, and when.

Related: [E4 · Behavioural patterns](../academy/lessons/E4.md), [E6 · LLD case studies](../academy/lessons/E6.md), [B3 · Atomics and concurrent collections](../academy/lessons/B3.md).
