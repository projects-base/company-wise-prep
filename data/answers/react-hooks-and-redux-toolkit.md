**Short answer:** Hooks give function components state (`useState`), side effects (`useEffect`), memoisation (`useMemo`, `useCallback`), mutable refs (`useRef`) and shared values (`useContext`). Context is enough for low-frequency global values like theme, current user or locale. Redux Toolkit fits large, frequently changing shared state with many writers: it gives one store, slices with reducers written in "mutating" style (Immer), `useSelector` to read only the slice a component needs, and DevTools for time-travel debugging.

## Explanation

| Hook | What it does |
|---|---|
| `useState` | State that triggers a re-render when set. Use the updater form `setN(n => n + 1)` when the next value depends on the previous. |
| `useEffect` | Sync with outside systems (fetch, subscriptions, timers) after render; return a cleanup. |
| `useMemo` | Cache a computed **value** between renders until its dependencies change. |
| `useCallback` | Cache a **function** identity. `useCallback(fn, deps)` is `useMemo(() => fn, deps)`. |
| `useRef` | A mutable box (`ref.current`) that survives renders without causing one; also holds DOM nodes. |
| `useContext` | Read the nearest Provider's value; re-renders when that value changes. |
| `useReducer` | Local state with a reducer, for complex transitions. |

**Context vs Redux Toolkit:** every consumer of a context re-renders when the provider's value changes, and there is no selector to subscribe to just part of it. That is fine for rarely changing values. Redux's `useSelector` re-renders a component only when its selected value changes, and RTK adds structure (slices, middleware, RTK Query for server data caching) for bigger apps.

## Example

```tsx
// store.ts
import { configureStore, createSlice, PayloadAction } from "@reduxjs/toolkit";

const cartSlice = createSlice({
  name: "cart",
  initialState: { items: [] as { id: string; qty: number }[] },
  reducers: {
    added(state, action: PayloadAction<string>) {
      const item = state.items.find((i) => i.id === action.payload);
      if (item) item.qty++;                    // Immer makes this immutable under the hood
      else state.items.push({ id: action.payload, qty: 1 });
    },
    removed(state, action: PayloadAction<string>) {
      state.items = state.items.filter((i) => i.id !== action.payload);
    },
  },
});
export const { added, removed } = cartSlice.actions;
export const store = configureStore({ reducer: { cart: cartSlice.reducer } });
export type RootState = ReturnType<typeof store.getState>;

// main.tsx: <Provider store={store}><App /></Provider>

// CartBadge.tsx
import { useSelector, useDispatch } from "react-redux";
function CartBadge() {
  const count = useSelector((s: RootState) =>
    s.cart.items.reduce((n, i) => n + i.qty, 0));
  const dispatch = useDispatch();
  return <button onClick={() => dispatch(added("sku-1"))}>Cart ({count})</button>;
}
```

## Pitfalls and follow-ups

- **useMemo vs useCallback vs React.memo:** `useMemo` caches a value, `useCallback` caches a function, `React.memo(Component)` skips re-rendering a component when its props are shallow-equal. `useCallback` alone does nothing useful; it pays off when the function is passed to a `React.memo` child or used as an effect dependency. Don't memoise everything; measure first. (The React Compiler, stable in 2025, can add this memoisation automatically.)
- **Why keys matter:** during reconciliation React matches list children by `key`. Stable unique keys (an id) let React keep each item's state and DOM node. Using the array index breaks when items are inserted, removed or reordered: state sticks to the wrong row.
- **Virtual DOM and reconciliation:** each render produces a tree of React elements. React diffs it against the previous tree, assuming different element types mean different subtrees and using keys for lists, then applies the minimal DOM changes.
- **Rules of hooks:** call them at the top level of a component or custom hook, never in loops, conditions or after an early return, because React tracks hooks by call order.
- **Never mutate state directly** outside RTK reducers: `state.items.push(x); setItems(state.items)` won't re-render because the reference didn't change.
- **Server data:** prefer RTK Query or TanStack Query over hand-written fetch-in-effect plus Redux; they handle caching, deduping and refetching.
