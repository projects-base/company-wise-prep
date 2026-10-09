**Short answer:** Class components extend `React.Component`, keep state in `this.state`, and use lifecycle methods. Function components are plain functions that return JSX and use hooks (`useState`, `useEffect`) for state and side effects. They are less code, have no `this` confusion, and let you share logic through custom hooks, so all new React code uses them. `useEffect` covers mount, update and unmount: the dependency array controls when it runs, and the returned cleanup function runs before the next effect and on unmount.

## Explanation

| | Class | Function + hooks |
|---|---|---|
| State | `this.state`, `this.setState` (merges) | `useState` (replaces) |
| Side effects | `componentDidMount/DidUpdate/WillUnmount` | `useEffect` |
| `this` binding | Needed for handlers | None |
| Logic reuse | HOCs, render props | Custom hooks |
| Error boundaries | Only classes can be one | Not yet possible with hooks |

**Lifecycle mapping with `useEffect`:**

- `componentDidMount` → `useEffect(fn, [])`: runs once after the first render.
- `componentDidUpdate` → `useEffect(fn, [a, b])`: runs after the first render and whenever `a` or `b` changes. No array means after every render.
- `componentWillUnmount` → return a cleanup function from the effect.

Think of an effect as "keep this external thing in sync with these values" rather than as lifecycle events. When a dependency changes, React runs the old cleanup, then the new effect.

## Example

```tsx
import { useEffect, useState } from "react";

function UserProfile({ userId }: { userId: string }) {
  const [user, setUser] = useState<User | null>(null);
  const [error, setError] = useState<string | null>(null);

  // didMount + didUpdate(userId) + willUnmount
  useEffect(() => {
    const controller = new AbortController();
    fetch(`/api/users/${userId}`, { signal: controller.signal })
      .then((r) => (r.ok ? r.json() : Promise.reject(new Error(`HTTP ${r.status}`))))
      .then(setUser)
      .catch((e) => { if (e.name !== "AbortError") setError(e.message); });
    return () => controller.abort();          // cleanup: cancel stale request
  }, [userId]);

  // mount-only subscription with cleanup
  useEffect(() => {
    const onResize = () => console.log(window.innerWidth);
    window.addEventListener("resize", onResize);
    return () => window.removeEventListener("resize", onResize);
  }, []);

  if (error) return <p role="alert">{error}</p>;
  return user ? <h2>{user.name}</h2> : <p>Loading…</p>;
}
```

The class version needs the fetch in both `componentDidMount` and `componentDidUpdate` (with a `prevProps.userId !== this.props.userId` check) and the cleanup in `componentWillUnmount`. The hook keeps all three together.

## Pitfalls and follow-ups

- **Dependency array:** list every prop, state value or function from the component that the effect reads. Missing ones give stale values; the `react-hooks/exhaustive-deps` lint rule catches them. Objects or functions created during render change every render, so memoise them or move them inside the effect.
- **StrictMode double-invoke:** in development only, React 18+ mounts, unmounts and remounts each component once, running effect → cleanup → effect. It exposes missing cleanups. Production runs effects once. Don't "fix" it with a ref flag; write a proper cleanup.
- **`useEffect` vs `useLayoutEffect`:** `useEffect` runs after the browser paints, so it doesn't block the screen; use it for fetching and subscriptions. `useLayoutEffect` runs synchronously after DOM changes but before paint; use it only to measure layout and adjust before the user sees a flicker (tooltip positions).
- **Fetch race conditions:** without the abort or an `ignore` flag, a slow old response can overwrite a newer one.
- **You might not need an effect:** derived values (filtered lists, totals) should be computed during render, not copied into state by an effect. For data fetching in real apps, a library like TanStack Query or a framework loader handles caching and races.
- **`setState` in class vs `useState`:** class `setState` merges objects; the hook setter replaces the value, so spread when updating objects: `setForm(f => ({ ...f, name }))`.
