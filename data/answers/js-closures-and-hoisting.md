**Short answer:** A closure is a function that remembers the variables of the scope where it was created, even after that scope has returned. Hoisting means declarations are registered before code runs: `var` is hoisted and initialised to `undefined`, function declarations are hoisted whole, and `let`/`const` are hoisted but stay uninitialised until their line runs. Accessing them earlier throws a `ReferenceError`; that window is the temporal dead zone (TDZ). Prefer `const`, use `let` when you reassign, avoid `var`.

## Explanation

**var vs let vs const**

| | `var` | `let` | `const` |
|---|---|---|---|
| Scope | Function | Block `{}` | Block |
| Hoisted | Yes, as `undefined` | Yes, but in TDZ | Yes, but in TDZ |
| Redeclare in same scope | Allowed | Error | Error |
| Reassign | Yes | Yes | No (but objects stay mutable) |
| Top level creates `window` property | Yes | No | No |

**Closures in practice:** private state (counters, memo caches), function factories, event handlers and callbacks that read component variables (every React hook callback is a closure).

## Example

```js
console.log(a);   // undefined   (var hoisted)
var a = 1;
console.log(b);   // ReferenceError: Cannot access 'b' before initialization (TDZ)
let b = 2;
greet();          // works: function declarations are hoisted with their body
function greet() { console.log("hi"); }

function makeCounter() {
  let count = 0;                  // private: only reachable through the closure
  return { inc: () => ++count, get: () => count };
}
const c = makeCounter();
c.inc(); c.inc();
c.get();          // 2

const user = { name: "A" };
user.name = "B";  // fine: const blocks reassignment, not mutation
```

**Follow-up: the loop + setTimeout bug**

```js
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 0);   // 3 3 3: one shared `i`, already 3 when callbacks run
}

// Fix 1: let creates a new binding per iteration
for (let i = 0; i < 3; i++) setTimeout(() => console.log(i), 0);   // 0 1 2

// Fix 2 (pre-ES6): capture with an IIFE
for (var i = 0; i < 3; i++) ((j) => setTimeout(() => console.log(j), 0))(i);
```

**Follow-up: event loop, microtasks vs macrotasks**

JavaScript runs one call stack. When it empties, the event loop first drains **all microtasks** (promise `.then/.catch/.finally` callbacks, code after `await`, `queueMicrotask`), then takes **one macrotask** (`setTimeout`, `setInterval`, I/O, UI events), then drains microtasks again, and so on.

```js
console.log("1");
setTimeout(() => console.log("2"), 0);
Promise.resolve().then(() => console.log("3"));
(async () => { console.log("4"); await null; console.log("5"); })();
console.log("6");
// 1 4 6 3 5 2
```

`async` functions run synchronously until the first `await`; the rest is resumed as a microtask. `async/await` is syntax over promises: `await p` pauses the function, not the thread.

## Pitfalls and follow-ups

- **Stale closures in React:** an effect or `setInterval` callback captures the state value from the render it was created in. Fix with the updater form `setCount(c => c + 1)`, a correct dependency array, or a ref.
- **Memory:** a closure keeps its captured variables alive; a long-lived listener that closes over a large object keeps it from being garbage-collected until you remove the listener.
- **Function expressions are not hoisted with their body:** `const f = () => {}` follows `const` rules (TDZ).
- **`typeof` on a TDZ variable** still throws, unlike on an undeclared one.
- **Error handling with async/await:** wrap in `try/catch`; an unhandled rejected promise surfaces as an `unhandledrejection` event.
