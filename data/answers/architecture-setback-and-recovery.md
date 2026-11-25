**Short answer:** Describe one real architecture in about a minute: the problem, the main parts, and *your* part. Then tell one or two setbacks in order: what went wrong, how you noticed, the options you weighed, what you did, and the number that shows it recovered.

Finish with what you would design differently. The setback and your decisions are the answer; the diagram is only the context.

## What they are checking

- **You understand the whole system**, not only your tickets.
- **Decisions:** why this shape, and what you gave up.
- **Calm under trouble:** detect, limit the damage, find the cause, prevent.
- **Honesty:** what you got wrong, without blaming others.
- **Level:** for a senior role, the scope should be a system or a cross-team change.

## Structure

CARL: Context, Action, Result, Learning, with the architecture as the context.

1. **Context (45 s):** users, load, 4 or 5 boxes, the hard requirement. Draw it if you can.
2. **Your part:** what you owned and decided.
3. **Setback:** expected vs actual, impact in numbers, how it was detected.
4. **Action:** stop the damage, options with trade-offs, choice, rollout.
5. **Result:** before and after.
6. **Learning:** one design rule you use now.

Setbacks that suit a backend story (use only one that happened to you):
- Two systems drifted apart because of a dual write.
- A synchronous call chain turned one slow service into an outage.
- A table outgrew its queries, or a migration locked it.
- A consumer processed messages twice after a rebalance.

## Template

> "I worked on [system], used by [users], handling [load]. The main parts were [component A], [component B] and [data store]; the hard requirement was [latency / correctness / volume].
>
> I owned [part], and I chose [design choice] over [alternative] because [reason].
>
> The setback came when [event]. We saw [symptom] through [alert / report], and it affected [impact with a number].
>
> First I [mitigation] to stop the damage. The root cause was [cause], which I confirmed by [evidence].
>
> I considered [option 1], [option 2] and [option 3]. I chose [option] because [trade-off], and rolled it out [flag / canary / phases].
>
> After that [metric] went from [before] to [after]. There was a second setback: [smaller problem], which we handled by [action].
>
> Looking back, I would [design change], because [lesson]. Now I always [habit]."

Expect these questions:
- *Why not [other technology]?* One honest reason, and when you would choose it.
- *What was your part exactly?* Say "I" for your decisions and "we" for the team's.

## Mistakes to avoid

- Ten minutes of diagrams and no setback.
- A setback that is really a complaint about another team.
- "We fixed it" with no options, no number and no "I".
- Sharing confidential details; describe the shape, not the secrets.
