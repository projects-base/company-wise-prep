**Short answer:** Describe a situation where the problem or the requirements were unclear, then show the method you used to make progress anyway: separate what was known from what was unknown, write down assumptions, get quick answers from the right people, and build a small first version to test the riskiest assumption. Finish with the outcome and what you would repeat.

## What they are checking

Google lists comfort with ambiguity as part of Googleyness.

- **Bias to action:** you did not wait for perfect information.
- **Structure:** you turned a vague goal into concrete next steps.
- **Judgement:** you knew which unknowns mattered and which did not.
- **Communication:** you made your assumptions visible so others could correct them.

## Structure

STAR + Reflection. In the Action part, make the method explicit:

1. **Restate the goal** as an outcome with a measure ("p99 under X", "report by month-end").
2. **List knowns, unknowns and assumptions.**
3. **Reduce the biggest unknown first:** ask the owner, read the code or data, run a spike.
4. **Ship a thin slice** and get feedback.
5. **Adjust** as answers come in.

Stories that fit: an inherited service with no docs and no owner, a vague ask like "make it faster", a migration whose real consumers were unknown, a bug only seen in production.

## Template

> "At [company], I was asked to [vague ask]. Nobody could tell me [the key unknown, e.g. who used the old endpoint / what 'fast enough' meant], and [extra complication, e.g. the original author had left].
>
> I wrote a short doc listing what we knew, what we didn't, and my assumptions, like [assumption]. To test the riskiest one, I [action, e.g. added logging to see which clients called the endpoint, or ran a one-day spike]. I shared the doc with [stakeholders] and asked them to correct anything wrong. [Person] pointed out [correction], which changed [plan].
>
> I then built [thin first version] and [validated it how].
>
> The result was [outcome, ideally measured]. What I'd repeat is [reflection, e.g. writing assumptions down early; people correct a written assumption much faster than they answer an open question]."

## Mistakes to avoid

- A story that was not really ambiguous (just hard or busy).
- Waiting for someone to clarify everything before acting.
- Acting with no check-ins and delivering the wrong thing.
- No outcome, or no reflection.
