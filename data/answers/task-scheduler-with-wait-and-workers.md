**Short answer:** Make `Task` a small future: it holds the work, a state, and the result or error, and `await()` blocks on a condition until the state is terminal (a `while` loop around `wait()`, with `notifyAll()` when the task finishes). Step two is a hand-rolled fixed pool: three worker threads loop on `BlockingQueue.take()`, run tasks as they arrive, and stop on a poison pill. One Java detail to say early: you cannot declare your own no-arg `wait()` because `Object.wait()` is `final`, so the blocking method is called `await()`.

## Requirements

- `Task.schedule(work)` (the prompt's `task()`) starts the work and returns at once.
- `task.await()` blocks the caller until the work finishes, then returns its result or rethrows its failure. Many threads may await the same task.
- Extension: exactly 3 workers; tasks keep arriving; each runs on whichever worker is free, in arrival order.
- Clean shutdown: stop accepting, finish queued work, stop workers.
- Likely follow-ups under time pressure: timeout on await, cancellation, wait for all, bounded queue, priorities.

## Classes

- `Task<T>`: the work (`Callable<T>`), `State` (`PENDING`, `RUNNING`, `SUCCEEDED`, `FAILED`, `CANCELLED`), result, error. `run()` (called by a worker), `await()`, `await(timeout)`, `cancel()`.
- `WorkerPool`: a `BlockingQueue<Task<?>>`, N worker threads, `submit(Callable)`, `close()`.
- In production you would use `ExecutorService` and `CompletableFuture`; the interview wants to see you build them.

## Patterns used

- **Producer-consumer**: submitters produce tasks, workers consume them through a blocking queue.
- **Future / promise**: `Task` is a minimal `Future`.
- **Guarded suspension**: `await` waits in a loop until the guard (`isDone`) is true, which handles spurious wakeups.
- **Poison pill** for shutdown, so workers exit without being interrupted mid-task.

## Code

```java
public final class Task<T> {
    public enum State { PENDING, RUNNING, SUCCEEDED, FAILED, CANCELLED }

    private final Callable<T> work;
    private State state = State.PENDING;    // all fields guarded by "this"
    private T result;
    private Throwable error;

    Task(Callable<T> work) { this.work = work; }

    /** Step 1: one thread per task. */
    public static <T> Task<T> schedule(Callable<T> work) {
        Task<T> t = new Task<>(work);
        Thread.ofPlatform().start(t::run);
        return t;
    }

    /** Called by exactly one worker. */
    void run() {
        synchronized (this) {
            if (state != State.PENDING) return;        // cancelled before it started
            state = State.RUNNING;
        }
        T value = null; Throwable failure = null;
        try { value = work.call(); }                    // run the work without holding the lock
        catch (Throwable e) { failure = e; }
        synchronized (this) {
            if (failure == null) { result = value; state = State.SUCCEEDED; }
            else { error = failure; state = State.FAILED; }
            notifyAll();                                // wake every waiter
        }
    }

    public synchronized T await() throws InterruptedException, ExecutionException {
        while (!isDone()) wait();                       // loop: spurious wakeups are allowed
        return outcome();
    }

    public synchronized T await(Duration timeout)
            throws InterruptedException, ExecutionException, TimeoutException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (!isDone()) {
            long left = deadline - System.nanoTime();
            if (left <= 0) throw new TimeoutException();
            TimeUnit.NANOSECONDS.timedWait(this, left);
        }
        return outcome();
    }

    /** Only a task that has not started can be cancelled in this version. */
    public synchronized boolean cancel() {
        if (state != State.PENDING) return false;
        state = State.CANCELLED;
        notifyAll();
        return true;
    }

    public synchronized boolean isDone() {
        return state == State.SUCCEEDED || state == State.FAILED || state == State.CANCELLED;
    }

    private T outcome() throws ExecutionException {
        return switch (state) {
            case SUCCEEDED -> result;
            case FAILED -> throw new ExecutionException(error);
            case CANCELLED -> throw new CancellationException();
            default -> throw new IllegalStateException(state.name());
        };
    }
}

/** Step 2: fixed pool, tasks arrive continuously. */
public final class WorkerPool implements AutoCloseable {
    private static final Task<Void> POISON = new Task<>(() -> null);

    private final BlockingQueue<Task<?>> queue = new LinkedBlockingQueue<>();
    private final List<Thread> workers = new ArrayList<>();
    private boolean shutdown;                            // guarded by "this"

    public WorkerPool(int size) {
        for (int i = 0; i < size; i++) {
            workers.add(Thread.ofPlatform().name("worker-" + i).start(this::workLoop));
        }
    }

    private void workLoop() {
        try {
            while (true) {
                Task<?> t = queue.take();
                if (t == POISON) return;
                t.run();                                 // run() never throws: failures go into the task
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();          // asked to stop now
        }
    }

    /** Check and enqueue under one lock, so no task lands behind the poison pills. */
    public synchronized <T> Task<T> submit(Callable<T> work) {
        if (shutdown) throw new RejectedExecutionException("pool is shut down");
        Task<T> t = new Task<>(work);
        queue.add(t);
        return t;
    }

    /** Graceful: queued tasks finish, then each worker takes one pill and exits. */
    @Override
    public void close() throws InterruptedException {
        synchronized (this) {
            if (shutdown) return;
            shutdown = true;
            for (int i = 0; i < workers.size(); i++) queue.add(POISON);
        }
        for (Thread w : workers) w.join();
    }
}
```

Usage: `try (var pool = new WorkerPool(3)) { var a = pool.submit(() -> fetch(1)); ... a.await(); }`. Tasks run in FIFO order across 3 workers; `await` callers block only on their own task.

Correctness notes to say out loud: `wait`/`notifyAll` must be called while holding the task's monitor, or Java throws `IllegalMonitorStateException`. The `while` loop (not `if`) guards against spurious wakeups. `notifyAll` (not `notify`) because several threads may await the same task. The work runs outside the lock, so `await` and `cancel` are never blocked by a slow task. The pool's `submit` and `close` share a lock, so a task cannot be enqueued after the pills, where no worker would ever pick it up.

## Extensions

- **Wait for all:** `awaitAll(List<Task<?>>)` simply awaits each in turn; total wait is the slowest task. Or a shared `CountDownLatch` counted down in `run()`.
- **Bounded queue / back-pressure:** use `ArrayBlockingQueue(capacity)` and `put` in `submit` (blocks producers when full), or `offer` and reject. Do not call a blocking `put` while holding the pool lock, since `close` needs it. Instead do `put` outside the lock (the pills too, since `add` throws on a full queue), and after the workers have exited, `close` drains the queue and cancels anything left behind the pills, so no awaiter blocks forever.
- **Cancelling a running task:** keep the worker `Thread` in the task and `interrupt()` it; the work must check interruption. That is cooperative cancellation, same as `Future.cancel(true)`.
- **Priorities:** `PriorityBlockingQueue` with a comparator on priority then a sequence number for FIFO among equals. The poison pill needs the lowest priority.
- **Shutdown now:** set the flag, `interrupt()` all workers, drain the queue and cancel the drained tasks.
- **Callbacks instead of blocking:** `task.onComplete(Consumer)`: keep a list of callbacks under the same lock and run them after completion. That is the road to `CompletableFuture`.
- **Java 21:** for I/O-bound tasks, a virtual thread per task (`Executors.newVirtualThreadPerTaskExecutor()`) replaces the fixed pool; keep a fixed pool or a `Semaphore` when the limit of 3 protects a scarce resource.

Deeper reading: [B1 · Threads: lifecycle, creation, interruption](../academy/lessons/B1.md), [B4 · Executors and sizing thread pools](../academy/lessons/B4.md), [B7 · Classic problems](../academy/lessons/B7.md), [L3 · Thread lifecycle](../academy/lessons/L3.md).
