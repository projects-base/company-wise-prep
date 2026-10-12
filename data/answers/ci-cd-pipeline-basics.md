**Short answer:** CI (continuous integration) means every push is automatically built and tested, so the main branch is always in a working state. CD means the same tested artifact is then promoted through environments automatically: *continuous delivery* stops for a manual approval before production, *continuous deployment* goes all the way. A typical Java pipeline is: checkout → build and unit tests → static analysis and security scans → package a Docker image → integration tests → deploy to dev/QA → approval → production, with a rollback plan.

## Explanation

**Why it exists.** Merging rarely and deploying by hand makes every release big, risky and slow to debug. A pipeline makes integration and release small, repeatable and boring.

**The stages, and what each protects:**

1. **Trigger:** a push or a pull request. PR builds give feedback before merging.
2. **Build and unit tests:** `mvn verify` or `gradle build`. Fast tests first, so feedback comes in minutes.
3. **Quality gates:** code coverage (JaCoCo), static analysis (SonarQube, SpotBugs, Checkstyle), dependency vulnerability scan (OWASP Dependency-Check, Snyk). Fail the build if a gate fails.
4. **Package once:** a JAR, then a Docker image tagged with the commit SHA, pushed to a registry. The *same* artifact goes to every environment; only configuration changes.
5. **Integration tests:** against real dependencies, often with Testcontainers (PostgreSQL, Kafka) or a deployed test environment. Contract tests if other teams call you.
6. **Deploy:** to dev/QA automatically, then staging, then production. On Kubernetes, the pipeline updates the image tag (directly with Helm/kubectl or via GitOps tools like Argo CD).
7. **Release strategy:** rolling update, blue-green (switch traffic between two environments) or canary (send a small share of traffic first and watch metrics).
8. **Verify and roll back:** smoke tests and health checks after deploy; roll back to the previous image tag if error rates rise.

**Tools.** Jenkins, GitHub Actions, GitLab CI, Azure DevOps, CircleCI all express the same idea as pipeline-as-code in the repository.

## Example

A GitHub Actions pipeline for a Spring Boot service:

```yaml
name: ci
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
      - run: mvn -B verify              # compile, unit + integration tests, JaCoCo
      - run: docker build -t registry.example.com/orders:${{ github.sha }} .
      - run: docker push registry.example.com/orders:${{ github.sha }}
  deploy-dev:
    needs: build
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - run: echo "deploy orders:${{ github.sha }} to dev"   # helm upgrade / GitOps commit
```

When describing your own pipeline in the interview, walk the same stages with your real tools: **[your CI tool]**, **[your test and quality gates]**, **[how you deploy: VMs, Kubernetes, cloud service]**, **[how you roll back]**.

## Pitfalls and follow-ups

- **Delivery vs deployment?** Delivery: always releasable, a human presses the button. Deployment: every green build goes to production.
- **How do you handle database migrations?** Flyway or Liquibase runs versioned scripts at startup or as a pipeline step. Make changes backward compatible (expand, then contract) so old and new versions can run together during a rolling deploy.
- **Secrets?** Never in the repository. Use the CI tool's secret store or a vault, injected at runtime.
- **Flaky tests?** Quarantine and fix them; a pipeline people ignore is worse than none.
- **Slow pipeline?** Cache dependencies, run test suites in parallel, keep unit tests fast and move slow ones to a later stage.
- **Feature flags** decouple deploy from release: ship code dark, turn it on later, turn it off without a redeploy.
- **Why build once?** Rebuilding per environment can produce a different binary than the one you tested.

Go deeper: [D8 · Production readiness: Actuator, metrics, testing](../academy/lessons/D8.md), [D10 · Shipping a feature: the playbook](../academy/lessons/D10.md).
