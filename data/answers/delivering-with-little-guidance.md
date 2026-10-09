**Short answer:** Tell a story where the goal was clear but the path was not. Show how you created your own structure: clarified the outcome and success metric, broke the work into small slices, prioritised by impact and risk, and rolled out safely behind a flag with a way to measure the result. Expect the interviewer to dig into each of those three areas, so know your numbers.

## What they are checking

- **Ownership:** you moved forward without waiting for a spec.
- **Judgement in prioritisation:** how you chose what to do first and what to drop.
- **Safe delivery:** feature flags, canary or staged rollout, rollback plan.
- **Measurement:** how you knew it worked. The A/B testing follow-up checks whether you understand experiments, not just shipping.

## Structure

STAR + Reflection, with three prepared deep-dive answers.

- **Prioritisation:** impact vs effort, riskiest unknown first, what you cut and who agreed.
- **Rollout:** flag, internal users, a small percentage, then ramp; what metrics gated each step; how you would roll back.
- **A/B testing:** the hypothesis, the primary metric, guardrail metrics (errors, latency), random assignment by a stable key such as user ID, running long enough for a meaningful sample, and not stopping the moment the graph looks good.

If you have never run a formal A/B test, say so and explain how you validated impact instead (before/after metrics, a pilot customer), then describe how you would run one.

## Template

> "At [company], I was asked to [goal, e.g. reduce failed payment retries / build an export feature] with [the gap, e.g. no spec, the PM was on leave, no one owned the legacy module].
>
> I started by [clarifying, e.g. writing a one-page doc with the problem, success metric and open questions, and getting 15 minutes with the stakeholder]. I split the work into [slices] and did [riskiest slice] first because [reason]. I chose not to do [scope cut] because [reason].
>
> For rollout, I put it behind [flag], enabled it for [internal users / N% of traffic], and watched [metrics]. [If applicable: we ran an A/B test on X with metric Y.]
>
> The result was [measurable outcome]. Next time I would [reflection, e.g. agree the success metric in writing before writing code]."

## Mistakes to avoid

- Choosing a story where you actually had lots of guidance.
- "I just figured it out" with no visible method.
- Claiming an A/B test you cannot explain (sample, metric, duration). Be honest.
- No rollback plan in the rollout story.
- Going silent with stakeholders while working alone. Show regular check-ins.
