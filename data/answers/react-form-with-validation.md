**Short answer:** Keep the form values in state (a controlled form), compute errors from the values with a pure `validate` function, show an error only after the field was touched or a submit was attempted, and disable the submit button while there are errors or a request is in flight. On submit, call `preventDefault`, validate again, then send. In real projects I would reach for React Hook Form with a Zod schema, but in an interview I'd write it by hand like this.

## Explanation

- **Controlled inputs:** `value={values.email}` plus `onChange` makes React state the single source of truth.
- **Errors are derived, not stored:** `const errors = validate(values)` runs on every render. No effect, no out-of-sync state.
- **Touched tracking:** don't shout "required" before the user typed. Mark a field touched on blur; on submit, mark all.
- **Accessibility:** a `<label htmlFor>` for each input, `aria-invalid`, and `aria-describedby` pointing at the error text.

## Example

```tsx
import { useState, type ChangeEvent, type FocusEvent, type FormEvent } from "react";

type Values = { name: string; email: string; age: string };
type Errors = Partial<Record<keyof Values, string>>;

function validate(v: Values): Errors {
  const e: Errors = {};
  if (!v.name.trim()) e.name = "Name is required";
  if (!/^\S+@\S+\.\S+$/.test(v.email)) e.email = "Enter a valid email";
  const age = Number(v.age);
  if (!Number.isInteger(age) || age < 18) e.age = "Must be 18 or older";
  return e;
}

export function SignupForm({ onSubmit }: { onSubmit: (v: Values) => Promise<void> }) {
  const [values, setValues] = useState<Values>({ name: "", email: "", age: "" });
  const [touched, setTouched] = useState<Partial<Record<keyof Values, boolean>>>({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const errors = validate(values);
  const isValid = Object.keys(errors).length === 0;

  const handleChange = (e: ChangeEvent<HTMLInputElement>) =>
    setValues((v) => ({ ...v, [e.target.name]: e.target.value }));
  const handleBlur = (e: FocusEvent<HTMLInputElement>) =>
    setTouched((t) => ({ ...t, [e.target.name]: true }));

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setTouched({ name: true, email: true, age: true });
    if (!isValid) return;
    setSubmitting(true);
    setServerError(null);
    try {
      await onSubmit(values);
    } catch (err) {
      setServerError((err as Error).message);
    } finally {
      setSubmitting(false);
    }
  }

  const field = (name: keyof Values, label: string, type = "text") => (
    <div>
      <label htmlFor={name}>{label}</label>
      <input id={name} name={name} type={type} value={values[name]}
             onChange={handleChange} onBlur={handleBlur}
             aria-invalid={!!(touched[name] && errors[name])}
             aria-describedby={`${name}-error`} />
      {touched[name] && errors[name] && <span id={`${name}-error`} role="alert">{errors[name]}</span>}
    </div>
  );

  return (
    <form onSubmit={handleSubmit} noValidate>
      {field("name", "Name")}
      {field("email", "Email", "email")}
      {field("age", "Age", "number")}
      {serverError && <p role="alert">{serverError}</p>}
      <button type="submit" disabled={!isValid || submitting}>
        {submitting ? "Saving…" : "Sign up"}
      </button>
    </form>
  );
}
```

**Follow-up: fetch and render a table with loading and error states**

```tsx
function UsersTable() {
  const [state, setState] = useState<
    { status: "loading" } | { status: "error"; msg: string } | { status: "ok"; users: User[] }
  >({ status: "loading" });

  useEffect(() => {
    const ctrl = new AbortController();
    fetch("/api/users", { signal: ctrl.signal })
      .then((r) => (r.ok ? r.json() : Promise.reject(new Error(`HTTP ${r.status}`))))
      .then((users) => setState({ status: "ok", users }))
      .catch((e) => e.name !== "AbortError" && setState({ status: "error", msg: e.message }));
    return () => ctrl.abort();
  }, []);

  if (state.status === "loading") return <p>Loading…</p>;
  if (state.status === "error") return <p role="alert">Failed: {state.msg}</p>;
  if (state.users.length === 0) return <p>No users</p>;
  return (
    <table>
      <thead><tr><th>Name</th><th>Email</th></tr></thead>
      <tbody>
        {state.users.map((u) => <tr key={u.id}><td>{u.name}</td><td>{u.email}</td></tr>)}
      </tbody>
    </table>
  );
}
```

**Follow-up: debounce a search input**

```tsx
function useDebounced<T>(value: T, delay = 300): T {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(id);        // each keystroke cancels the previous timer
  }, [value, delay]);
  return debounced;
}

function Search() {
  const [q, setQ] = useState("");
  const query = useDebounced(q, 300);
  useEffect(() => {
    if (!query) return;
    const ctrl = new AbortController();
    fetch(`/api/search?q=${encodeURIComponent(query)}`, { signal: ctrl.signal }) /* ...set results */;
    return () => ctrl.abort();
  }, [query]);
  return <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search" />;
}
```

## Pitfalls and follow-ups

- **Number inputs give strings.** Convert before validating.
- **Validate on the server too.** Client validation is for user experience, not security.
- **Double submit:** disabling the button while `submitting` prevents it.
- **Uncontrolled alternative:** read values with `new FormData(e.currentTarget)` on submit; fewer re-renders, and it is what React 19's form actions (`<form action={fn}>`, `useActionState`) build on.
- **Why abort in the effect?** It prevents a slow, stale response from overwriting a newer one and avoids updates after unmount.
