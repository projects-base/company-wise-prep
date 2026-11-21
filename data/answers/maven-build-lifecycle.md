**Short answer:** Maven has three lifecycles: `clean`, `default` and `site`. Each is an ordered list of phases, and running a phase runs every earlier phase of that lifecycle first. The main default phases are validate, compile, test, package, verify, install and deploy. Phases do no work themselves: plugin goals bound to them do, and the packaging type (jar, war) decides the default bindings. So `mvn clean install` deletes `target/`, then compiles, runs unit tests, builds the jar, runs the verify checks and copies the jar into `~/.m2`.

## Explanation

- **Lifecycle → phase → goal.** A lifecycle is the order, a phase is a hook in that order, and a goal (`plugin:goal`) is the actual work. For a jar project: `compile` → `compiler:compile`, `test` → `surefire:test`, `package` → `jar:jar`, `install` → `install:install`, `deploy` → `deploy:deploy`.
- **Phases chain; goals don't.** `mvn package` runs validate through package. `mvn dependency:tree` runs only that goal, with no lifecycle.
- **Adding work.** Bind a plugin goal to a phase with an `<execution>`. Spring Boot's parent POM binds `spring-boot-maven-plugin:repackage` to `package`, which turns the plain jar into the executable fat jar.
- **Tests.** Surefire runs unit tests (`*Test`, `*Tests`) in `test` and fails fast. Failsafe runs `*IT` tests in `integration-test`, lets `post-integration-test` clean up, and fails the build in `verify`. That is why pipelines run `mvn verify`.
- **Install vs deploy.** `install` copies the artifact into the local repository (`~/.m2/repository`) for other builds on this machine. `deploy` uploads it to a remote repository such as Nexus or Artifactory for other machines and teams.

## Example

```xml
<!-- Pin one version for the whole build; this adds no dependency by itself -->
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.fasterxml.jackson</groupId>
      <artifactId>jackson-bom</artifactId>
      <version>2.19.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>                           <!-- runtime: code uses java.sql interfaces only -->
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

```text
mvn clean install        clean lifecycle up to clean, then default up to install
mvn verify               unit + integration tests, nothing published
mvn -pl app -am package  build module "app" and the modules it depends on
mvn dependency:tree      who brought in which version
```

## Pitfalls and follow-ups

- **Version conflicts:** two libraries need different versions of a third. Maven keeps one: the **nearest** in the dependency tree wins, and at equal depth the **first declared** wins. The symptom is a runtime `NoSuchMethodError` although everything compiled. Diagnose with `mvn dependency:tree`, then fix with `dependencyManagement`, a BOM or an `<exclusion>`.
- **Scopes:** `compile` (default, everywhere), `provided` (compile and test, supplied at runtime by the server or JDK, e.g. the servlet API in a war), `runtime` (not for compiling, e.g. a JDBC driver), `test` (JUnit, Mockito), `import` (only for BOMs inside `dependencyManagement`).
- **`-DskipTests` vs `-Dmaven.test.skip=true`:** the first compiles tests but does not run them; the second skips compiling them too. Neither belongs in a pipeline that publishes artifacts.
- **`mvn integration-test` in CI:** it stops before cleanup and before `verify`, so failed integration tests may not fail the build.
- **Multi-module:** the reactor builds modules in dependency order, not listing order. Inheritance (`<parent>`) shares configuration; aggregation (`<modules>`) builds them together.
- **SNAPSHOT vs release:** a SNAPSHOT can change and is re-checked (`-U` forces it); a release never changes once published. The Maven Wrapper (`mvnw`) pins the Maven version for everyone.

Further reading: [JAVA-FUND-9 · Maven in depth](../academy/lessons/J9.md), [BE-STORY-7 · Annotations, Java config & Maven](../academy/lessons/S6.md).
