"""Generates data/campaigns/backend-2027-01/campaign.yaml: the backend plan as fixed sessions.

Run from the repo root: python tools/backend-plan/generate.py . (edit the tables or guide.md, then rerun).

Restarted on Mon 23 Nov 2026. Weekends belong to the Masai AI-ML course, so the backend work sits on weekdays:
60 minutes of core a day (two lessons, a lesson and a DSA problem, or one AI Build milestone on a Monday or
Wednesday), one 60-minute design on Sunday, and a Saturday catch-up slot. The plan runs as many weeks as the
lessons and milestones need. Every day has tiers: core sessions (the schedule is built on these alone), stretch
sessions (optional, they never put you behind) and one floor session (the minimum that keeps a bad day alive).
The Academy's "Zero to Pro" path runs in order, except that the system design kit, interview technique, the
glossary and the AI lessons move up so they are done before and while the January applications run. Each AI
Build milestone (docs/AI-PROJECT.md) is scheduled on the first Monday or Wednesday after its prerequisites."""
import datetime as dt
import glob
import os
import re
import sys

import yaml

ROOT = sys.argv[1]
OUT = os.path.join(ROOT, 'data', 'campaigns', 'backend-2027-01', 'campaign.yaml')

bank = {}
for f in glob.glob(os.path.join(ROOT, 'data', 'questions', '*', '*.yaml')):
    q = yaml.safe_load(open(f, encoding='utf-8'))
    bank[q['slug']] = q
cur = yaml.safe_load(open(os.path.join(ROOT, 'data', 'academy', 'curriculum.yaml'), encoding='utf-8'))
mods = {m['id']: m for t in cur['tracks'] for m in t['modules']}
# Display code: the track's code plus the position in the track (JAVA-JVM-3), as the app shows it.
CODE = {m['id']: f"{t.get('code', t['id'])}-{i + 1}" for t in cur['tracks'] for i, m in enumerate(t['modules'])}
PATH = list(next(p['modules'] for p in cur['paths'] if p['id'] == 'zero-to-pro'))
# Building blocks, the design method, the interview kit, interview technique, the glossary and the AI lessons have
# no later prerequisites, so they move up to right after testing (SPRING-BOOT-8): the kit is done by mid-January,
# when the applications start, and the AI lessons follow so the AI Build project can start.
EARLY = ['F1', 'F3', 'H4', 'H2', 'R1', 'G1', 'G2', 'G7', 'G3', 'G4', 'G5', 'G6']
for m in EARLY:
    PATH.remove(m)
at = PATH.index(next(m for m in PATH if CODE[m] == 'SPRING-BOOT-8')) + 1
PATH[at:at] = EARLY
BUILD = list(next(p['modules'] for p in cur['paths'] if p['id'] == 'ai-build'))

# Lessons pinned to a week: each DSA pattern lesson opens the week whose DSA problems need it, and the behavioural
# lesson and the system design kit land before the 17 Jan applications checkpoint. A pinned lesson becomes the second lesson of that week.
PINNED = {'T1': 1, 'T2': 3, 'T3': 4, 'T4': 5, 'H6': 7, 'F1': 7, 'F3': 8, 'H4': 8, 'H2': 8, 'T5': 9, 'T6': 10}

# One queue: lessons in path order, each milestone right after the last of its prerequisites.
QUEUE = [('L', m) for m in PATH if m not in PINNED]
for b in BUILD:
    after = max(i for i, (_, m) in enumerate(QUEUE) if m in mods[b]['prerequisites'])
    QUEUE.insert(after + 1, ('P', b))


def Q(slug):
    assert slug in bank, f'missing question {slug}'
    return slug


KICKOFF = dt.date(2026, 11, 22)         # Sunday: a 20-minute kickoff
WEEK1_MONDAY = dt.date(2026, 11, 23)
APPLY_WEEK = 8                          # Sun 17 Jan: the checkpoint that starts the applications
MASAI_BACKLOG_WEEKS = 3                 # a weekday evening for the Masai backlog, until it is clear
LONG_WORDS = 2400                       # prose words above which a lesson is "long" (see lesson())
MOCK_WEEKS = {9: 'MOCK_LLD', 10: 'MOCK_HLD', 13: 'MOCK_LLD', 14: 'MOCK_HLD'}  # finals take the last two Sundays

# Weeks 1-12: one DSA pattern a week. Three problems are core (Tue, Thu, Fri), two are stretch (Mon, Wed).
DSA = [
    ('Arrays, hashing, two pointers', ['two-sum', '3sum', 'longest-consecutive-sequence'], ['container-with-most-water', 'group-anagrams']),
    ('Sliding window, prefix sums', ['longest-substring-without-repeating-characters', 'subarray-sum-equals-k', 'longest-repeating-character-replacement'],
     ['permutation-in-string', 'max-consecutive-ones-iii']),
    ('Stacks, binary search', ['valid-parentheses', 'search-in-rotated-sorted-array', 'koko-eating-bananas'], ['min-stack', 'basic-calculator-ii']),
    ('Linked lists, intervals', ['reverse-linked-list-ii', 'merge-intervals', 'meeting-rooms-ii'], ['linked-list-cycle-ii', 'find-the-duplicate-number']),
    ('Trees', ['invert-binary-tree', 'binary-tree-zigzag-level-order-traversal', 'kth-smallest-element-in-a-bst'],
     ['maximum-width-of-binary-tree', 'amount-of-time-for-binary-tree-to-be-infected']),
    ('Heaps, top-K', ['top-k-frequent-elements', 'implement-min-heap', 'meeting-rooms-iii'], ['top-k-frequent-words', 'single-threaded-cpu']),
    ('Graphs: BFS and DFS', ['number-of-islands', 'rotting-oranges', 'pacific-atlantic-water-flow'], ['max-area-of-island', 'find-the-safest-path-in-a-grid']),
    ('Graphs: topological sort, union-find, shortest paths', ['course-schedule-ii', 'dijkstra-shortest-path', 'cheapest-flights-within-k-stops'],
     ['is-graph-bipartite', 'the-earliest-moment-when-everyone-become-friends']),
    ('Backtracking, tries', ['generate-parentheses', 'implement-trie-ii-prefix-tree', 'search-suggestions-system'],
     ['letter-combinations-of-a-phone-number', 'restore-ip-addresses']),
    ('Dynamic programming 1', ['best-time-to-buy-and-sell-stock', 'decode-ways', 'partition-equal-subset-sum'], ['maximum-subarray', 'delete-and-earn']),
    ('Dynamic programming 2, greedy', ['edit-distance', 'longest-palindromic-substring', 'hand-of-straights'], ['dungeon-game', 'split-array-largest-sum']),
    ('Design data structures', ['lru-cache', 'insert-delete-getrandom-o1', 'logger-rate-limiter'], ['max-stack', 'first-unique-number']),
    # Weeks 13-16: interview-level problems across the patterns, timed; stretch re-solves earlier ones from a blank file.
    ('Hard mixed, timed', ['trapping-rain-water', 'minimum-window-substring', 'sliding-window-maximum'], ['subarray-sum-equals-k', 'merge-intervals']),
    ('Hard mixed, timed', ['merge-k-sorted-lists', 'binary-tree-maximum-path-sum', 'find-median-from-data-stream'], ['course-schedule-ii', 'top-k-frequent-elements']),
    ('Hard mixed, timed', ['word-ladder', 'alien-dictionary', 'basic-calculator'], ['number-of-islands', 'edit-distance']),
    ('Hard mixed, timed', ['maximum-profit-in-job-scheduling', 'russian-doll-envelopes', 'lfu-cache'], ['lru-cache', 'longest-substring-without-repeating-characters']),
    # Any further weeks: timed re-solves of the patterns interviews ask most, from a blank file.
    ('Hard mixed, timed (re-solve)', ['number-of-islands', 'lru-cache', 'merge-intervals'], ['3sum', 'koko-eating-bananas']),
    ('Hard mixed, timed (re-solve)', ['course-schedule-ii', 'edit-distance', 'top-k-frequent-elements'], ['meeting-rooms-ii', 'decode-ways']),
    ('Hard mixed, timed (re-solve)', ['minimum-window-substring', 'word-ladder', 'trapping-rain-water'], ['generate-parentheses', 'rotting-oranges']),
    ('Hard mixed, timed (re-solve)', ['subarray-sum-equals-k', 'search-in-rotated-sorted-array', 'partition-equal-subset-sum'],
     ['kth-smallest-element-in-a-bst', 'insert-delete-getrandom-o1']),
    ('Hard mixed, timed (re-solve)', ['longest-consecutive-sequence', 'pacific-atlantic-water-flow', 'hand-of-straights'],
     ['group-anagrams', 'cheapest-flights-within-k-stops']),
    ('Hard mixed, timed (re-solve)', ['implement-trie-ii-prefix-tree', 'meeting-rooms-iii', 'lfu-cache'],
     ['valid-parentheses', 'reverse-linked-list-ii']),
    ('Hard mixed, timed (re-solve)', ['word-ladder', 'merge-k-sorted-lists', 'edit-distance'], ['two-sum', 'search-suggestions-system']),
]
# Say it out loud (stretch on Tue, Thu, Fri), roughly following the topics the lessons have reached.
SPOKEN = [
    ['jvm-internals', 'java-stack-vs-heap-memory', 'oop-four-pillars'],
    ['java-equals-vs-double-equals-hashcode', 'java-string-immutability', 'hashmap-internals'],
    ['java-collections-hierarchy', 'java-8-features', 'solid-principles'],
    ['java-threading-fundamentals', 'process-vs-thread', 'web-services-fundamentals'],
    ['servlets-and-dispatcherservlet', 'what-is-spring-framework', 'spring-autowired-and-dependency-injection'],
    ['spring-applicationcontext-and-bean-lifecycle', 'spring-aop-and-advice-types', 'spring-requestmapping-annotation'],
    ['http-post-vs-put', 'sql-join-types', 'jdbc-connection-pooling'],
    ['spring-transactional-management', 'hibernate-save-vs-persist-vs-update-vs-merge', 'db-indexing-partitioning-sharding'],
    ['sql-query-optimization', 'java-garbage-collection', 'java-volatile-and-synchronized'],
    ['hashmap-vs-hashtable-vs-concurrenthashmap', 'java-create-and-fix-deadlock', 'runnable-callable-future-completablefuture'],
    ['describe-a-design-pattern', 'inheritance-vs-composition', 'producer-consumer-problem'],
    ['oauth-and-jwt', 'redis-internals', 'kafka-architecture'],
    ['rdbms-scaling-vs-nosql', 'microservices-design-patterns', 'microservices-communication-grpc-vs-rest-eda'],
    ['cap-theorem', 'idempotency-in-payment-systems', 'debug-java-memory-issues'],
    ['optimize-slow-api', 'debugging-across-microservices', 'zero-downtime-schema-migrations'],
    ['llm-cost-caching-and-observability', 'sql-injection-prevention', 'java-serialization'],
    ['maven-build-lifecycle', 'hexagonal-architecture', 'cqrs'],
    ['comparable-vs-comparator', 'collection-vs-collections', 'polymorphism-and-static-method-overriding'],
    ['java-final-and-synchronized-methods', 'deep-vs-shallow-copy', 'java-reflection'],
    ['java-final-finally-finalize', 'how-to-synchronize-a-class', 'tcp-vs-udp'],
    ['java-streams-coding-drills', 'messaging-systems-fundamentals', 'sql-optimization-performance-scalability'],
    ['system-design-database-scalability-performance-techniques', 'diagnose-low-cache-hit-ratio', 'lock-free-stack-atomic-reference'],
    ['cqrs', 'hexagonal-architecture', 'maven-build-lifecycle'],
]
# Sunday core (60 min): one design in this order, or a scored mock (MOCK_WEEKS, then finals on the last two Sundays).
DESIGNS = ['design-parking-lot', 'design-url-shortener', 'design-vending-machine', 'design-unique-id-generator',
           'design-logging-framework', 'design-idempotent-api', 'design-cache-pluggable-eviction', 'design-ticket-booking',
           'design-rate-limiter', 'design-chat-application', 'design-proximity-service',
           'design-distributed-key-value-store', 'design-payment-system', 'design-file-system', 'design-web-crawler',
           'design-notification-system', 'design-elevator-system']
# Sunday stretch from week 3: the designs without a core Sunday, for a free hour.
EXTRA_DESIGNS = ['singleton-and-factory-patterns', 'design-employee-cab-service', 'design-image-hosting-service',
                 'design-distributed-message-queue', 'design-bounded-blocking-queue', 'design-distributed-cache',
                 'design-short-video-news-feed', 'design-undo-redo-text-editor']
# Weekly review bonus: a behavioural story, rehearsed before and during the first weeks of applications.
STORY = {6: 'walk-through-current-work', 7: 'why-are-you-looking-for-a-change', 8: 'most-challenging-project',
         9: 'tell-me-about-a-conflict', 10: 'tell-me-about-a-failure'}

LESSON_DETAIL = ("Read it in one-part-at-a-time mode; "
                 "Answer its Check yourself questions out loud before reading the answers; "
                 "Say the 2-minute summary out loud without looking")
DSA_DETAIL = ("2: Read it and say the brute force and its complexity out loud; "
              "{solve}: Solve it in the Playground with a timer, no hints; "
              "If stuck at {solve} minutes, read only the Approach section of the model answer, then finish it yourself; "
              "3: Write the pattern and the complexity in one line in your notes")
Q_DETAIL = ("10: Press Judge my answer and answer out loud in 4 layers: what it is, how it works inside, where you used it, what goes wrong; "
            "10: Read the feedback and the model answer, then answer the follow-up out loud; "
            "Write one line for each layer you missed")
LLD_DETAIL = ("10: Requirements and scope out loud, including what can happen concurrently; "
              "15: Class diagram: entities (nouns) first, then services; "
              "10: Name each pattern and where it goes; "
              "20: Code the core classes and the one key method in the Playground; "
              "5: Compare with the model answer and list what you missed")
HLD_DETAIL = ("5: Requirements, including how correct and what fails halfway; "
              "5: Estimates out loud: QPS, peak, storage; "
              "10: API and data model; "
              "15: Draw the architecture; "
              "15: Two deep dives: the scarce resource, and what breaks after the write; "
              "10: Compare with the model answer and list what you missed")
MOCK_LLD = ("Ask Claude in chat for an unseen LLD mock: 45 minutes, scored out of 10; "
            "Classes first, patterns named, one key method coded, concurrency discussed; "
            "15: Log the score and the gaps in Mock progress")
MOCK_HLD = ("Ask Claude in chat for an unseen system design mock: 45 minutes, scored out of 10; "
            "Run the checklist: requirements, estimates, API, data model, architecture, deep dives, failures; "
            "15: Log the score and the gaps in Mock progress")
REVIEW = ("10: Say 5 things from this week's lessons out loud, 1 minute each, no notes; "
          "5: Note the one you couldn't explain: it goes first next week")
CATCH_UP = ("If a weekday core session is unfinished, do the most important one now: DSA, then lesson; "
            "Nothing missed? This slot is free. Anything still unfinished on Sunday night is dropped, not carried")
MASAI = ("Live class; "
         "This week's assignment; "
         "Missed lectures first while there is a backlog")
MASAI_BACKLOG = ("Watch one missed Masai lecture or finish one pending assignment; "
                 "Backlog already clear? Skip this session")
BUILD_DETAIL = ("Open Academy {code} and read Goal, What you build and Acceptance criteria; "
                "45: Build it in your TriageDesk repo, following the Steps (Hints only when stuck); "
                "Run the acceptance checks until they pass; "
                "5: Commit and tag it, then say the interview angle out loud once; "
                "Bad day: do the first 30 minutes and finish it in Saturday's catch-up slot")
APPLY = ("Checkpoint: Java, Spring and Spring Boot, SQL and JPA, the system design kit, your behavioural stories, 4 DSA pattern lessons and 8 weeks of DSA problems are done; "
         "Update your resume with impact numbers and start applying this week (the job shortlist); "
         "From now on, each real interview's questions go into the Sunday review")

sessions = []
queue = list(QUEUE)
step = 0


def add(day, s, tier='core', floor=False):
    row = {'date': day.isoformat(), 'tier': tier}
    if floor:
        row['floor'] = True
    sessions.append({**row, **s})


def prose_words(mid):
    text = open(os.path.join(ROOT, 'data', 'academy', 'lessons', f'{mid}.md'), encoding='utf-8').read()
    return len(re.findall(r"[A-Za-z0-9'’-]+", re.sub(r'^```.*?^```', '', text, flags=re.S | re.M)))


# A long lesson fills a whole 30-minute slot on its own, so two of them never share a Monday or Wednesday.
LONG = {m for m in PATH if prose_words(m) > LONG_WORDS}


def lesson(day, floor=False, after=None):
    """Schedules the first lesson still in the queue (a milestone at the head waits for a Monday or Wednesday).
    As the second lesson of a day after a long one (`after`), it takes the next short lesson within a few places
    instead (looking up to eight ahead), as long as that lesson does not depend on any lesson it jumps over."""
    global step
    lessons = [i for i, (kind, _) in enumerate(queue) if kind == 'L']
    i = lessons[0]
    if after is not None and (after in LONG or queue[i][1] in LONG):
        for j in lessons[1:9]:
            cand = queue[j][1]
            jumped = {m for k, m in queue[:j] if k == 'L'}   # every lesson the candidate would overtake
            if cand not in LONG and not jumped & set(mods[cand]['prerequisites']):
                i = j
                break
    mid = queue.pop(i)[1]
    step += 1
    add(day, dict(minutes=30, title=f'Scratch · Step {step} of {len(PATH)} · {CODE[mid]}: {mods[mid]["title"]}',
                  detail=LESSON_DETAIL), floor=floor)


def build(day):
    pid = queue.pop(0)[1]
    add(day, dict(minutes=60, title=f'Build · {CODE[pid]}: {mods[pid]["title"]}', detail=BUILD_DETAIL.format(code=CODE[pid])),
        floor=True)


def lessons_left():
    return any(kind == 'L' for kind, _ in queue)


def dsa(day, pattern, slug, tier='core'):
    timed = pattern.startswith('Hard')
    solve = 25 if timed else 22
    kind = 'Re-solve' if tier == 'stretch' and timed else 'Timed DSA' if timed else 'DSA'
    add(day, dict(minutes=30, title=f'{kind} · {pattern} · {bank[Q(slug)]["title"]}',
                  detail=DSA_DETAIL.format(solve=solve), questions=[slug]), tier=tier)


def spoken(day, slug):
    add(day, dict(minutes=30, title=f'Say it out loud · {bank[Q(slug)]["title"]}', detail=Q_DETAIL, questions=[Q(slug)]),
        tier='stretch')


def design(day, key):
    if key in ('MOCK_LLD', 'FINAL_LLD'):
        title = ('Final ' if key.startswith('FINAL') else '') + 'LLD mock with Claude (unseen problem)'
        return dict(minutes=60, title=title, detail=MOCK_LLD)
    if key in ('MOCK_HLD', 'FINAL_HLD'):
        title = ('Final system' if key.startswith('FINAL') else 'System') + ' design mock with Claude (unseen problem)'
        return dict(minutes=60, title=title, detail=MOCK_HLD)
    lld = bank[Q(key)]['type'] == 'LLD'
    return dict(minutes=60, title=f'{"LLD" if lld else "HLD"} · {bank[Q(key)]["title"]}',
                detail=LLD_DETAIL if lld else HLD_DETAIL, questions=[key])


# Weekdays first: as many weeks as the lessons and milestones need.
k = 0
while queue:
    assert k < len(DSA) and k < len(SPOKEN), 'add DSA and SPOKEN rows for the extra weeks'
    monday = WEEK1_MONDAY + dt.timedelta(weeks=k)
    mon, tue, wed, thu, fri = (monday + dt.timedelta(days=d) for d in range(5))
    due = [pid for pid, pin_week in PINNED.items() if pin_week == k + 1]
    if due:
        first = next(i for i, (kind, _) in enumerate(queue) if kind == 'L')
        queue[first + 1:first + 1] = [('L', pid) for pid in due]   # in PINNED order, so prerequisites come first
    pattern, core_dsa, extra_dsa = DSA[k]
    assert len(core_dsa) == 3 and len(extra_dsa) == 2 and len(SPOKEN[k]) == 3, k
    for day, problem, question in ((mon, extra_dsa[0], None), (tue, core_dsa[0], SPOKEN[k][0]), (wed, extra_dsa[1], None),
                                   (thu, core_dsa[1], SPOKEN[k][1]), (fri, core_dsa[2], SPOKEN[k][2])):
        if not queue:
            break
        if question is None:
            # Mon and Wed: a milestone if one is due, else two lessons (stretch: a DSA problem).
            if queue[0][0] == 'P':
                build(day)
            else:
                first = next(m for kind, m in queue if kind == 'L')
                lesson(day, floor=True)
                if lessons_left():
                    lesson(day, after=first)  # a milestone that just became due waits for the next Mon/Wed
            dsa(day, pattern, problem, tier='stretch')
        else:
            # Tue, Thu, Fri: a lesson and a DSA problem (stretch: say it out loud); a milestone only once lessons run out.
            if lessons_left():
                lesson(day, floor=True)
            else:
                build(day)
            dsa(day, pattern, problem)
            spoken(day, question)
        last_day = day
    if k + 1 <= MASAI_BACKLOG_WEEKS:
        add(wed, dict(minutes=45, title='Masai · backlog: one missed lecture or assignment', detail=MASAI_BACKLOG))
    k += 1
weeks = k
LAST_DAY = last_day
dsa_core = sum(1 for s in sessions if s['title'].startswith(('DSA', 'Timed DSA')) and s['tier'] == 'core')
assert step == len(PATH), (step, len(PATH))
assert sum(1 for s in sessions if s['title'].startswith('Build ·')) == len(BUILD) == 10
add(KICKOFF, dict(minutes=20, title=f'Kickoff · the restarted plan (23 Nov to {LAST_DAY:%d %b})',
                  detail=("5: Read The rules and the week on the Plan page; "
                          f"5: Put your fixed weekday slot in your calendar for every weekday until {LAST_DAY:%d %b}; "
                          "5: Write down your Masai backlog: missed lectures and pending assignments; "
                          "5: Open Two Sum (Tuesday's problem) and say the brute force out loud")), floor=True)

# Weekends: Masai, the Saturday catch-up slot, and Sunday's design or mock plus the weekly review.
designs, extras = iter(DESIGNS), iter(EXTRA_DESIGNS)
for k in range(weeks):
    week = k + 1
    sat = WEEK1_MONDAY + dt.timedelta(weeks=k, days=5)
    sun = sat + dt.timedelta(days=1)
    add(sat, dict(minutes=0, title='Masai · live class and assignment', detail=MASAI), floor=True)
    add(sat, dict(minutes=30, title='Catch-up · only if a weekday session is unfinished', detail=CATCH_UP), tier='stretch')
    add(sun, dict(minutes=0, title='Masai · live class and assignment', detail=MASAI))
    key = ('FINAL_LLD' if week == weeks - 1 else 'FINAL_HLD' if week == weeks else MOCK_WEEKS.get(week) or next(designs))
    add(sun, design(sun, key))
    review = dict(minutes=15, title=f'Weekly review · week {week}: {DSA[k][0]}', detail=REVIEW)
    if week == APPLY_WEEK:
        review['detail'] = REVIEW + '; ' + APPLY
    if week in STORY:
        review['detail'] += "; Bonus: say your story for this week's behavioural question out loud (STAR, 2 minutes)"
        review['questions'] = [Q(STORY[week])]
    add(sun, review, floor=True)
    extra = next(extras, None) if week >= 3 else None
    if extra:
        add(sun, design(sun, extra), tier='stretch')



def step_of(s):
    m = re.match(r'Scratch · Step (\d+) of', s['title'])
    return int(m.group(1)) if m else 10 ** 6


# Each day reads core before stretch and lessons in order; the stable sort keeps every other session where it was.
sessions.sort(key=lambda s: (s['date'], s['tier'] != 'core', step_of(s)))

guide = open(os.path.join(os.path.dirname(__file__), 'guide.md'), encoding='utf-8').read()
rows = []
for k in range(weeks):
    monday = WEEK1_MONDAY + dt.timedelta(weeks=k)
    end = monday + dt.timedelta(days=6)
    week_of = [s for s in sessions if monday.isoformat() <= s['date'] <= end.isoformat()]
    steps = [s for s in week_of if s['title'].startswith('Scratch')]
    lessons_cell = '—'
    if steps:
        first, last = steps[0]['title'].split(' · ')[2], steps[-1]['title'].split(' · ')[2]
        lessons_cell = f"{step_of(steps[0])}–{step_of(steps[-1])}: {first.split(':')[0]} … {last.split(':')[0]}"
    builds = ', '.join(s['title'].split(' · ')[1].split(':')[0] for s in week_of if s['title'].startswith('Build ·')) or '—'
    sunday = next(s['title'] for s in week_of if s['date'] == end.isoformat() and s['tier'] == 'core' and s['minutes'] == 60)
    rows.append(f"| {k + 1} | {monday:%d %b} – {end:%d %b} | {lessons_cell} | {builds} | {DSA[k][0]} | "
                f"{sunday.replace(' (unseen problem)', '')} |")
MAP = ('| Week | Dates | Lessons (steps: first … last) | AI Build | DSA | Sunday design |\n|---|---|---|---|---|---|\n'
       + '\n'.join(rows))
for key, value in {'{MAP}': MAP, '{LESSONS}': str(len(PATH)), '{MILESTONES}': str(len(BUILD)), '{WEEKS}': str(weeks),
                   '{END}': f'{LAST_DAY:%a %d %b %Y}', '{DSA_CORE}': str(dsa_core)}.items():
    assert key in guide, key
    guide = guide.replace(key, value)

header = {
    'id': 'backend-2027-01',
    'name': f'Backend SDE II / Senior: {weeks}-week plan (23 Nov 2026 to {LAST_DAY:%d %b %Y})',
    'role': 'SDE II / Senior Software Engineer, Java backend (any product company)',
    'companies': {c: 1.0 for c in sorted({s['company'] for q in bank.values() for s in q.get('sightings') or []})},
    'start': KICKOFF.isoformat(),
    'interview': (LAST_DAY + dt.timedelta(days=3)).isoformat(),
    'minutes': {'weekday': 60, 'weekend': 60},
    'review_days': 3,
    'notes': ('A general plan, not tied to one company: the whole Academy from scratch, DSA, LLD, HLD and system design, '
              'Java and Spring Boot internals. Weekdays carry it; weekends belong to the Masai AI-ML course. '
              f'Applications start in mid-January; every lesson and AI Build milestone is done by {LAST_DAY:%a %d %b %Y}.'),
}
with open(OUT, 'w', encoding='utf-8', newline='\n') as f:
    f.write(f'# Generated by tools/backend-plan/generate.py ({weeks} weeks + a kickoff day). Each date is a fixed session;\n'
            '# tier core = the plan, stretch = optional, floor = the minimum for a bad day. Rules and the map: guide below.\n')
    f.write(yaml.safe_dump(header, sort_keys=False, allow_unicode=True, width=120))
    f.write('guide: |\n' + ''.join('  ' + line if line.strip() else '\n' for line in guide.splitlines(True)))
    if not guide.endswith('\n'):
        f.write('\n')
    f.write(yaml.safe_dump({'sessions': sessions}, sort_keys=False, allow_unicode=True, width=200))

days = sorted({s['date'] for s in sessions})
core = lambda d: sum(s['minutes'] for s in sessions if s['date'] == d and s['tier'] == 'core')
print('sessions', len(sessions), 'days', len(days), days[0], days[-1])
print('lessons', sum(1 for s in sessions if s['title'].startswith('Scratch · Step')),
      'dsa core', sum(1 for s in sessions if s['title'].startswith(('DSA', 'Timed DSA')) and s['tier'] == 'core'),
      'builds', sum(1 for s in sessions if s['title'].startswith('Build ·')),
      'designs', sum(1 for s in sessions if s['title'][:5] in ('LLD ·', 'HLD ·') and s['tier'] == 'core'),
      'mocks', sum(1 for s in sessions if 'mock with Claude' in s['title']),
      'stretch', sum(1 for s in sessions if s['tier'] == 'stretch'))
print('core minutes per day:', sorted({core(d) for d in days}), '| days without a floor:',
      [d for d in days if not any(s.get('floor') for s in sessions if s['date'] == d)])
print('weeks', weeks, 'last weekday', LAST_DAY, '| early lessons at steps', [i + 1 for i, m in enumerate(PATH) if m in EARLY])
print('builds:', [(s['date'], s['title'].split(':')[0]) for s in sessions if s['title'].startswith('Build ·')])
