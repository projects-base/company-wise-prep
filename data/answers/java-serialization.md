**Short answer:** Serialization turns an object graph into bytes (`ObjectOutputStream.writeObject`) and deserialization rebuilds it (`ObjectInputStream.readObject`). A class opts in by implementing the marker interface `Serializable`. `serialVersionUID` is the class's version stamp: if the stream's value doesn't match the local class, deserialization fails with `InvalidClassException`. `transient` fields are skipped and come back as default values. In modern services, Java serialization is avoided for data exchange because deserializing untrusted bytes is a known security hole; use JSON (Jackson) or Protobuf/Avro instead.

## Explanation

**What gets written:** the class descriptor (name, `serialVersionUID`, field names and types), then field values, recursively following references. Shared references and cycles are handled: each object is written once and later references point back to it.

**Rules**
- Every non-transient, non-static field's type must itself be serializable, or you get `NotSerializableException`.
- `static` fields belong to the class, not the object, so they are never written.
- `transient` excludes a field: passwords, caches, derived values, non-serializable handles (threads, sockets, loggers).
- **Superclass that is not `Serializable`:** its fields aren't written, and on deserialization its **no-arg constructor runs** (it must exist). For the serializable classes in the hierarchy, no constructor runs; the object is created and the fields are filled directly.
- **Records** (Java 16+) deserialize differently: through the canonical constructor, so your validation runs. A good reason to prefer records for serialized data.

**`serialVersionUID`**
- If you don't declare one, the JVM computes one from the class's structure (name, fields, methods). Almost any change, even adding a method, changes it and breaks old streams.
- Declare it explicitly: `private static final long serialVersionUID = 1L;`. Then **compatible** changes (adding a field, which comes back as default) still deserialize. Incompatible ones (changing a field's type, changing the class hierarchy) still fail or need custom code.

**Customising**
- `private void writeObject(ObjectOutputStream)` / `readObject(ObjectInputStream)`: call `defaultWriteObject()` / `defaultReadObject()`, then add or validate data.
- `readResolve()`: replace the deserialized object, used to keep singletons single.
- `writeReplace()`: write a proxy instead (the *serialization proxy pattern*).
- `Externalizable`: you write everything yourself; it needs a public no-arg constructor.

**Security**
- `readObject` can instantiate any serializable class on the classpath and run its `readObject` logic. Chains of such classes ("gadget chains") have led to remote code execution in real systems.
- Defence: never deserialize untrusted input with `ObjectInputStream`. If you must, use **deserialization filters**: `ObjectInputFilter` (Java 9, JEP 290) and the JVM-wide filter factory (Java 17, JEP 415) to allow-list classes and limit depth and size.

## Example

```java
public class Session implements Serializable {
    @Serial private static final long serialVersionUID = 1L;   // @Serial: Java 14+, compiler checks

    private final String userId;
    private final Instant loginAt;
    private transient String accessToken;      // not written; null after reading
    private transient int cachedHash;          // recomputed lazily

    public Session(String userId, Instant loginAt, String token) {
        this.userId = userId; this.loginAt = loginAt; this.accessToken = token;
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (userId == null) throw new InvalidObjectException("userId required");   // validate
    }
}

// Write
try (var out = new ObjectOutputStream(Files.newOutputStream(Path.of("session.bin")))) {
    out.writeObject(new Session("u42", Instant.now(), "secret"));
}

// Read, with an allow-list filter
try (var in = new ObjectInputStream(Files.newInputStream(Path.of("session.bin")))) {
    in.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
        "com.example.Session;java.time.*;java.lang.*;!*"));
    Session s = (Session) in.readObject();     // accessToken is null
}
```

Keeping a singleton single:

```java
public final class Registry implements Serializable {
    public static final Registry INSTANCE = new Registry();
    private Registry() {}
    @Serial private Object readResolve() { return INSTANCE; }
}
// Simpler: an enum singleton is serialization-safe by design.
```

## Pitfalls and follow-ups

- **Where is Java serialization still used?** Legacy RMI, some caches and session replication (for example HTTP sessions in clustered servlet containers), and some older messaging. New Spring Boot services send JSON over REST and Avro/Protobuf over Kafka.
- **Why are `transient` fields `null`/`0` after reading?** No constructor or initializer runs for serializable classes; fields get JVM defaults unless `readObject` restores them.
- **Is `serialVersionUID` required?** No, but without it any recompilation with structural changes breaks compatibility. Declare it.
- **Serializable lambdas** are possible but fragile; avoid them.
- **`Serializable` vs `Externalizable`:** automatic vs fully manual; `Externalizable` can be faster and smaller but you own versioning.
- **Deep copy via serialization?** It works but is slow and fragile; prefer copy constructors.
- **JSON with Jackson:** uses getters/fields or record components, and `@JsonIgnore` plays the role of `transient`. Schema evolution is handled by ignoring unknown fields (Spring Boot configures Jackson that way by default).

Related: [A3 · Class loading and delegation](../academy/lessons/A3.md) (deserialization resolves classes through a class loader).
