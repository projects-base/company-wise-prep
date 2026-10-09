**Short answer:** I meet the deadline by default, but I take on debt deliberately, not by accident. I separate debt that blocks or endangers this release (fix now) from debt that only slows us later (record it with its cost, and schedule it). Any shortcut gets a ticket, an owner, and a plan to pay it back, agreed with the product owner. Then I give an example.

## What they are checking

- **Pragmatism:** you don't gold-plate and miss a business deadline.
- **Engineering judgement:** you won't ship something unsafe just to hit a date.
- **Communication:** debt is made visible and agreed, not hidden.
- **Follow-through:** you actually paid some debt back.

## Structure

Give your **rule of thumb**, then a **story**.

Rule of thumb:
1. **Must fix now:** anything that risks correctness, security, data loss, or makes this feature untestable.
2. **Contain:** if you must cut a corner, isolate it behind a clean interface so it is cheap to replace later.
3. **Record:** a ticket that states the shortcut, its risk, and the cost of leaving it.
4. **Negotiate:** a slice of each sprint (many teams use a fixed share) for debt, or pay it off when you next touch that code.
5. **Pay back:** follow up after the deadline.

## Template

> "On [project], we had [deadline, e.g. a client go-live in two weeks]. While building [feature], I found [debt, e.g. the order service had no tests around the pricing logic I needed to change, and the code was duplicated in three places].
>
> I split it: [critical part, e.g. adding tests around the pricing path I was changing] was non-negotiable because a pricing bug would hit customers. [Non-critical part, e.g. removing the duplication] could wait, so I [contained it, e.g. put my change behind one new method the others could later call] and raised a ticket explaining the risk.
>
> I told [PO / lead] about the trade-off, and we agreed to [plan, e.g. take the ticket in the sprint after go-live].
>
> We shipped on [date] with [result], and [follow-up, e.g. the clean-up landed two sprints later and cut a later change from days to hours]."

## Mistakes to avoid

- "I always refactor first" (ignores the business) or "deadline always wins" (ignores risk).
- Hidden debt: TODO comments nobody tracks.
- No conversation with product or the lead.
- A story where the debt was never paid back and you don't mention it.
