**Short answer:** Answer in layers, from the moment you write code to production: clear design and readable code (small methods, good names, SOLID where it helps), tests at the right levels (unit, integration with real dependencies, a few end-to-end), code review on every pull request, automated gates in CI (build, tests, coverage, static analysis, dependency scans), and feedback from production (logs, metrics, alerts, post-incident fixes). Then give one concrete example of a practice you introduced or a bug it caught.

## What they are checking

- You have a **system**, not just "I test my code".
- You know **Java tooling**: JUnit 5, Mockito, Testcontainers, JaCoCo, SonarQube, SpotBugs/Checkstyle.
- You treat **review** as a two-way practice: you review well and accept feedback.
- You balance quality with delivery (no gold-plating, no shortcuts that bite later).
- Senior signal: you raised the bar for the **team**, not only yourself.

## Structure

Walk the layers quickly, then one STAR example.

1. **Before coding:** understand the requirement and edge cases; agree the design for anything non-trivial.
2. **While coding:** readable code, immutability, validation at boundaries, consistent error handling, no duplicated logic.
3. **Testing:** unit tests for business logic, `@WebMvcTest`/`@DataJpaTest` slices, integration tests with Testcontainers for PostgreSQL or Kafka, contract tests between services.
4. **Review:** small PRs, a checklist (correctness, edge cases, transactions, N+1 queries, security, logging), respectful comments.
5. **Automation:** CI blocks the merge on failing tests, quality gate or vulnerable dependencies.
6. **Production:** monitoring and alerts; every incident ends with a test that would have caught it.

## Template

> "I think of quality in layers.
>
> When I write code I **[e.g. keep business logic in small, testable services, validate input at the controller, and make error handling consistent]**. I write **[unit tests with JUnit 5 and Mockito, integration tests with Testcontainers]** so that **[e.g. repository queries are tested against real PostgreSQL]**.
>
> Every change goes through a pull request. When I review, I look for **[correctness, edge cases, transaction boundaries, performance like N+1 queries]**. Our CI pipeline runs **[tests, JaCoCo coverage, SonarQube gate, dependency scan]**, and the merge is blocked if any fail.
>
> One example: on **[project]**, we had **[problem, e.g. repeated production bugs in X / no integration tests]**. I **[action, e.g. introduced Testcontainers-based tests for the repository layer / added a review checklist / set up a Sonar quality gate]**. As a result, **[outcome, e.g. escaped defects in that module dropped from N to M / review time went down]**.
>
> And after an incident, I add the test that would have caught it, so the same bug can't come back."

## Mistakes to avoid

- "I test thoroughly" with no specifics.
- Treating coverage percentage as the goal (100% coverage with weak assertions proves little).
- Saying review is someone else's job, or describing review as gatekeeping.
- Listing tools you have not actually used.
- No example, or an example where quality slowed the team without benefit.

Go deeper: [D8 · Production readiness: Actuator, metrics, testing](../academy/lessons/D8.md), [E1 · SOLID, by violation and refactor](../academy/lessons/E1.md).
