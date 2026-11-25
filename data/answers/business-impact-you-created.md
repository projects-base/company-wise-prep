**Short answer:** Lead with one number and who felt it: "I cut [process] from [X] to [Y], which saved [users] [amount]". Then explain the baseline, what *you* did, how you measured it, and how sure you are.

Have two more impacts ready in one line each, from different kinds (money, time, risk). A modest number you can defend beats a big one you cannot.

## What they are checking

- **Business sense:** you know why the work mattered.
- **Measurement:** there was a baseline, and you checked after.
- **Your share:** what came from you and what from the team.
- **Honesty:** you separate measured from estimated.

## Structure

1. **Headline:** result first, one sentence.
2. **Baseline:** how it was, with a number and its source.
3. **Action:** the two or three things you did and decided.
4. **Measurement:** how you know (dashboard, log query, ticket count, bill), over what period.
5. **Business meaning:** translate into money, hours, customers or risk.
6. **Caveats:** who else contributed, what was estimated.

Turn technical results into business ones (fill in with your own data):

| Technical change | Number to find | Business meaning |
|---|---|---|
| Faster API or job | p95, or run time before and after | conversion, users waiting less, a deadline met |
| Fewer incidents | pages per month, minutes of downtime | revenue protected, on-call hours |
| Automation | runs per week × minutes saved | hours per year |
| Lower resource use | instances, storage, the monthly bill | cost per year |
| Better tests or pipeline | lead time, failed releases | features sooner, fewer rollbacks |
| Data correctness | wrong records per month | refunds, support tickets, compliance |

**Simple maths to show:** [minutes saved per run] × [runs per week] × 52 ÷ 60 = [hours per year].

**No exact number?** Say so, then give a range and how you reached it: "about [low] to [high], based on [sample]".

## Template

> "The impact I am most proud of is [result with a number] for [users / team].
>
> Before, [baseline], which I got from [source]. It hurt because [business reason].
>
> I found that [cause], proposed [solution], and personally built [your part]. [Colleague / team] did [their part].
>
> Over [period], [metric] went from [before] to [after]. In business terms that is [money / hours / customers], calculated as [simple maths].
>
> To be clear, [the caveat: what is estimated, or what else changed in that time].
>
> Two others, briefly: [cost impact in one line], and [quality or risk impact in one line]."

## Mistakes to avoid

- "We improved performance a lot." No baseline, no number.
- A percentage alone: "80% faster" of 10 ms or of 10 minutes?
- Claiming the whole team's result.
- A number you cannot explain when asked "how did you measure that?".
- Listing activities (migrated, refactored) instead of outcomes.
