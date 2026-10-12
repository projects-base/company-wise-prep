**Short answer:** Cross-team dependencies come in a few types: one team needs another's API or feature, shared code or infrastructure, a shared environment or data, or a release that must ship together. You overcome them by finding them early (in refinement and planning), making them visible (linked tickets, a dependency board), agreeing contracts so teams can work in parallel (API specs, mocks, contract tests), and talking often. For metrics: velocity, burndown, cycle time and lead time, throughput and escaped defects are signals for the team to improve, not targets or a way to compare teams.

## What they are checking

- You have worked in a multi-team agile setup and know where it breaks.
- You act early and practically rather than waiting or blaming.
- You understand metrics as tools for conversation, and their limits.
- (This was asked of a delivery manager; an engineer should answer from the engineer's seat: what *you* did to unblock work.)

## Structure

1. **Types of dependency** (one line each).
2. **How you handle them:** before, during and if it slips.
3. **A short example** from your experience (STAR).
4. **Metrics:** what each tells you, and how you read it.

**Practices worth naming:**
- Identify early: dependency questions in refinement; PI planning or a dependency board in SAFe/large setups.
- Decouple: agree the API contract first (OpenAPI), build against a mock or WireMock stub, verify with contract tests (Pact, Spring Cloud Contract); feature flags so code ships before the other side is ready.
- Coordinate: a named owner on each side, a scrum-of-scrums or a short sync, linked Jira tickets with due dates.
- Escalate with options, not complaints, when a dependency threatens a date.

**Reading metrics:**
- *Velocity:* for the team's own forecasting; it varies with estimates, so never compare teams.
- *Burndown:* flat then a cliff means work is too big or blocked.
- *Cycle time / lead time:* rising cycle time often points to waiting (reviews, dependencies, environments).
- *Throughput, WIP, escaped defects, deployment frequency, change-failure rate:* flow and quality.
- Look at trends over several sprints and ask "why", not single numbers.

## Template

> "The common ones I've seen are **[e.g. waiting on another team's API, a shared library change, a shared test environment]**.
>
> For example, on **[project]**, our **[feature]** depended on **[team]** delivering **[API/change]**. **[Situation: what made it risky, e.g. their sprint priorities were different]**. I **[actions, e.g. agreed the request and response contract with their lead in week one, built against a WireMock stub, added a contract test, and joined their stand-up twice a week]**. When **[slip, if any]**, I **[how you raised it, with options]**.
>
> The result was **[outcome, e.g. we integrated in two days instead of a sprint]**.
>
> On metrics, I treat **[velocity / cycle time]** as a team signal. When **[metric pattern, e.g. our cycle time went up]**, we found **[cause, e.g. PRs waiting a day for review]** and **[fix]**."

## Mistakes to avoid

- Blaming the other team.
- Only "we escalated to the manager".
- Using velocity as a productivity score or comparing teams by it.
- Listing metrics without saying what you would do with them.
- A theory-only answer with no example.
