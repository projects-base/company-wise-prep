**Short answer:** In an escalation I first stop the bleeding: confirm the impact, mitigate (rollback, feature flag, failover) before hunting for the root cause, and set a regular update rhythm with the customer-facing owner. After it is stable, I run a blameless root-cause review and track the fixes to closure. For conflict inside the team, I move the argument from people to data and agree on a decision owner.

## What they are checking

- **Customer obsession:** you think about the customer's impact first, not your code.
- **Calm under pressure:** a clear order of actions, not heroics.
- **Communication:** who you update, how often, and in what words.
- **Ownership after the fire:** RCA, prevention, follow-through.
- **Conflict resolution:** the second half of the question. Have a short answer ready.

## Structure

Give your **general approach** in 4-5 steps, then one **real story** (STAR + Reflection).

Approach:
1. **Assess:** who is affected, how many, since when. Check dashboards, logs, recent deploys.
2. **Mitigate first:** roll back, toggle a feature flag, scale out, or apply a data fix. Restoring service beats a perfect diagnosis.
3. **Communicate:** one owner talks to the customer or support. Updates on a fixed cadence (for example every 30 minutes), even if the update is "still investigating".
4. **Fix and verify:** root cause, a proper fix, tests, and confirmation with the customer.
5. **Prevent:** blameless post-mortem, action items with owners and dates, better alerts.

Team conflict: listen to both sides privately, restate the shared goal, compare options on agreed criteria (risk, time, customer impact), and if still stuck, agree who decides and commit.

## Template

> "At [company], [customer / internal team] reported [symptom] on [system], affecting [scope, e.g. N% of requests / a key client's month-end run]. I was [your role, e.g. on-call / owner of the service].
>
> I first [how you confirmed impact]. Since [recent change] was the likely trigger, I [mitigation] within [time], which restored [service]. I kept [stakeholder] updated every [interval].
>
> The root cause was [cause]. We fixed it by [fix] and added [test / alert / guardrail].
>
> During the incident, [teammate] and I disagreed on [e.g. hotfix vs rollback]. I [how you resolved it, e.g. we listed the risk of each in two minutes and picked rollback because it was reversible].
>
> The result was [outcome, e.g. customer kept / no repeat incident in N months]. What I learned was [reflection]."

## Mistakes to avoid

- Starting with debugging details and never mentioning the customer.
- Root-causing in production for an hour while users are down.
- "I fixed it alone overnight" heroics with no communication.
- Blaming another team in the post-mortem.
- Forgetting the conflict-resolution part of the question.
