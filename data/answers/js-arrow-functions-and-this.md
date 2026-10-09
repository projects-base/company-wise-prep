**Short answer:** An arrow function is a shorter function syntax, `(a, b) => a + b`, but the real difference is that it has no own `this`: it uses the `this` of the surrounding scope (lexical `this`). A regular function's `this` depends on how it is called. Arrow functions also have no `arguments` object, no `prototype`, and can't be used with `new`. Use arrows for callbacks; use regular functions or method syntax for object methods and constructors.

## Explanation

| | Regular function | Arrow function |
|---|---|---|
| `this` | Set by the call: `obj.f()` → `obj`; plain `f()` → `undefined` in strict mode (global object otherwise); `new` → new object; `call/apply/bind` → explicit | Lexical, from the enclosing scope; `call/apply/bind` can't change it |
| `arguments` | Yes | No (use rest params `...args`) |
| `new` | Can be a constructor | `TypeError` |
| `prototype` | Yes | No |
| Hoisting | Declarations are hoisted | It's an expression in a `const`, so TDZ rules |
| Implicit return | No | Yes for a single expression; wrap an object literal in `()` |

## Example

```js
const timer = {
  seconds: 0,
  startBroken() {
    setInterval(function () { this.seconds++; }, 1000);  // `this` is not timer
  },
  start() {
    setInterval(() => { this.seconds++; }, 1000);        // arrow uses start()'s this = timer
  },
};

const obj = {
  name: "A",
  regular() { return this.name; },     // "A" when called as obj.regular()
  arrow: () => this?.name,             // `this` of the module/outer scope, not obj
};

const sum = (...nums) => nums.reduce((a, b) => a + b, 0);
const toPoint = (x, y) => ({ x, y });  // parentheses needed to return an object

function Person(name) { this.name = name; }
new Person("A");                       // ok
const P = (name) => { this.name = name; };
// new P("A");                         // TypeError: P is not a constructor
```

React class components used arrows (`handleClick = () => {...}`) to avoid `.bind(this)`; with function components and hooks there is no `this` at all.

## Pitfalls and follow-ups

- **Don't use arrows as object methods or prototype methods** if they need `this` to be the object.
- **Event listeners:** with a regular function, `this` is the element (`e.currentTarget`); with an arrow it isn't. Prefer `e.currentTarget` either way.
- **Losing `this`:** `const f = obj.regular; f()` gives `undefined` for `this` in strict mode. Fix with `obj.regular.bind(obj)` or an arrow wrapper.

**Java vs JavaScript (asked together):**

| | Java | JavaScript |
|---|---|---|
| Typing | Static, checked at compile time | Dynamic (TypeScript adds static types) |
| Execution | Compiled to bytecode, run by the JVM with JIT | Interpreted/JIT-compiled by an engine (V8) in browsers or Node.js |
| OOP | Class-based | Prototype-based (`class` is syntax over prototypes) |
| Concurrency | Multi-threaded (platform and virtual threads) | Single-threaded event loop with async callbacks/promises; Web Workers for parallelism |
| Functions | Lambdas implement functional interfaces | Functions are first-class objects |

Despite the name, they are unrelated languages.
