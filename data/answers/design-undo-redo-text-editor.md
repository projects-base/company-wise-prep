**Short answer:** Use the Command pattern: every edit is an object that can `apply` and `revert` itself, storing only the delta (position plus inserted or deleted text), not a copy of the document. Keep two deques, `undo` and `redo`. A new edit pushes onto `undo` and clears `redo`; undo moves one command from `undo` to `redo`, and redo moves it back. The history limit is enforced by dropping the oldest command from the bottom of the `undo` deque, which is O(1) with an `ArrayDeque`.

## Requirements

- Operations: `insert(pos, text)`, `delete(pos, length)`, `undo()`, `redo()`, `text()`.
- A new edit after an undo discards the redo history (standard editor behaviour).
- Configurable history limit N: at most N undoable steps; the oldest is forgotten first.
- Memory should scale with the size of the edits, not the size of the document.
- Nice to have: typing "hello" undoes as one step, not five.

## Classes

- `TextBuffer`: holds the text (`StringBuilder` here) with `insert` and `delete`.
- `EditCommand` (sealed interface): `apply(TextBuffer)` and `revert(TextBuffer)`.
  - `InsertCommand(pos, text)`: revert deletes `text.length()` chars at `pos`.
  - `DeleteCommand(pos, length)`: captures the removed text on `apply`, so revert can re-insert it.
- `History`: the two deques and the limit. Knows nothing about text.
- `Editor`: the facade the UI calls. Creates commands, runs them, records them in `History`.

## Patterns used

- **Command**: edits are objects with their own inverse. That is what makes undo cheap and generic.
- **Memento** (the alternative): snapshot the whole text per step. Simple and always correct, but O(document size) memory per step. Fine for small fields, wrong for documents. Say why you chose Command.
- Single responsibility: `History` is a reusable bounded undo/redo structure; `Editor` handles text rules.

## Code

```java
public final class TextBuffer {
    private final StringBuilder sb = new StringBuilder();
    void insert(int pos, String s) { sb.insert(pos, s); }
    String delete(int pos, int len) {
        String removed = sb.substring(pos, pos + len);
        sb.delete(pos, pos + len);
        return removed;
    }
    int length() { return sb.length(); }
    @Override public String toString() { return sb.toString(); }
}

public sealed interface EditCommand permits InsertCommand, DeleteCommand {
    void apply(TextBuffer buf);
    void revert(TextBuffer buf);
}

public record InsertCommand(int pos, String text) implements EditCommand {
    public void apply(TextBuffer buf)  { buf.insert(pos, text); }
    public void revert(TextBuffer buf) { buf.delete(pos, text.length()); }

    /** Typing coalesces: "he" at 0 followed by "llo" at 2 becomes "hello" at 0. */
    Optional<InsertCommand> mergeWith(InsertCommand next) {
        return next.pos == pos + text.length() && !next.text.contains("\n")
            ? Optional.of(new InsertCommand(pos, text + next.text))
            : Optional.empty();
    }
}

public final class DeleteCommand implements EditCommand {
    private final int pos, length;
    private String removed;                     // filled on apply
    public DeleteCommand(int pos, int length) { this.pos = pos; this.length = length; }
    public void apply(TextBuffer buf)  { removed = buf.delete(pos, length); }
    public void revert(TextBuffer buf) { buf.insert(pos, removed); }
}

public final class History {
    private final int limit;
    private final Deque<EditCommand> undo = new ArrayDeque<>();   // head = most recent
    private final Deque<EditCommand> redo = new ArrayDeque<>();

    public History(int limit) {
        if (limit < 1) throw new IllegalArgumentException("limit must be >= 1");
        this.limit = limit;
    }

    void record(EditCommand cmd) {
        if (cmd instanceof InsertCommand next && undo.peekFirst() instanceof InsertCommand prev) {
            Optional<InsertCommand> merged = prev.mergeWith(next);
            if (merged.isPresent()) { undo.removeFirst(); undo.addFirst(merged.get()); redo.clear(); return; }
        }
        undo.addFirst(cmd);
        if (undo.size() > limit) undo.removeLast();               // forget the oldest, O(1)
        redo.clear();
    }

    Optional<EditCommand> popUndo() {
        EditCommand c = undo.pollFirst();
        if (c != null) redo.addFirst(c);
        return Optional.ofNullable(c);
    }

    Optional<EditCommand> popRedo() {
        EditCommand c = redo.pollFirst();
        if (c != null) undo.addFirst(c);                          // redo stack is never larger than limit
        return Optional.ofNullable(c);
    }
}

public final class Editor {
    private final TextBuffer buf = new TextBuffer();
    private final History history;

    public Editor(int historyLimit) { this.history = new History(historyLimit); }

    public void insert(int pos, String text) { run(new InsertCommand(pos, text)); }
    public void delete(int pos, int len)     { run(new DeleteCommand(pos, len)); }

    private void run(EditCommand cmd) {
        cmd.apply(buf);                         // apply first: if it throws, history is untouched
        history.record(cmd);
    }

    public boolean undo() { return history.popUndo().map(c -> { c.revert(buf); return true; }).orElse(false); }
    public boolean redo() { return history.popRedo().map(c -> { c.apply(buf);  return true; }).orElse(false); }
    public String text()  { return buf.toString(); }
}
```

**Data structure choices** (the emphasis of the question):

- **Two stacks as `ArrayDeque`, not `java.util.Stack`.** `Stack` is synchronized and extends `Vector`. `ArrayDeque` gives O(1) push and pop at the head and O(1) removal at the tail, which is exactly what the limit needs.
- **Bounded history alternative:** a fixed-size circular array with a head index and a count. Same O(1), no allocation, good if the limit is fixed and large.
- **Deltas, not snapshots:** memory is O(total size of the last N edits).
- **Text storage:** `StringBuilder` insert and delete are O(n) because of shifting. Real editors use a gap buffer (fast local typing), a piece table or a rope (O(log n) edits). Mention it, but do not build it unless asked.

## Extensions

- **Limit by memory instead of count:** track total bytes in the deque and evict from the tail until under budget.
- **Changing the limit at runtime:** shrink by evicting from the tail until `size <= newLimit`.
- **Grouping:** a "transaction" (find-and-replace all) is a `CompositeCommand` holding a list; revert runs the children in reverse order.
- **Branching history (undo tree):** instead of clearing redo, keep a tree of states so no edit is ever lost.
- **Concurrency / collaboration:** a single editor is single-threaded (UI thread). Multi-user editing needs operational transformation or CRDTs, which is an HLD topic.

Deeper reading: [E4 · Behavioural patterns](../academy/lessons/E4.md), [E5 · The LLD interview method](../academy/lessons/E5.md).
