"""Generates data/campaigns/backend-2027-01/campaign.yaml: a strict 13-week syllabus as fixed sessions.

Run from the repo root: python tools/backend-plan/generate.py . (edit the tables or guide.md, then rerun).

Every day has tiers: core sessions (the schedule is built on these alone), stretch sessions
(optional, they never put you behind) and one floor session (the minimum that keeps a bad day
alive: the day's Academy lesson). The Academy's "Zero to Pro" path runs one lesson a day, in order;
the DSA, the stretch question and the weekend designs follow the topics the lessons have covered."""
import datetime as dt
import glob
import os
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
PATH = next(p['modules'] for p in cur['paths'] if p['id'] == 'zero-to-pro')


def Q(slug):
    assert slug in bank, f'missing question {slug}'
    return slug


KICKOFF = dt.date(2026, 11, 1)          # Sunday: kickoff day
WEEK1_MONDAY = dt.date(2026, 11, 2)
INTERVIEW = dt.date(2027, 2, 1)         # ready for interviews

# week: (dsa pattern, [Mon-Fri dsa], Saturday timed dsa, [Mon-Fri stretch questions], Saturday LLD, Sunday HLD,
#        behavioural story for the Sunday review)
W = [
    ('Arrays, hashing, two pointers', ['two-sum', '3sum', 'container-with-most-water', 'longest-consecutive-sequence', 'group-anagrams'], 'trapping-rain-water',
     ['jvm-internals', 'java-stack-vs-heap-memory', 'java-string-immutability', 'oop-four-pillars', 'java-equals-vs-double-equals-hashcode'],
     'design-parking-lot', 'design-url-shortener', None),
    ('Sliding window, prefix sums', ['longest-substring-without-repeating-characters', 'permutation-in-string', 'longest-repeating-character-replacement', 'subarray-sum-equals-k', 'max-consecutive-ones-iii'], 'minimum-window-substring',
     ['solid-principles', 'java-threading-fundamentals', 'process-vs-thread', 'web-services-fundamentals', 'servlets-and-dispatcherservlet'],
     'design-vending-machine', 'design-unique-id-generator', None),
    ('Stacks, binary search', ['valid-parentheses', 'min-stack', 'basic-calculator-ii', 'search-in-rotated-sorted-array', 'koko-eating-bananas'], 'sliding-window-maximum',
     ['what-is-spring-framework', 'hibernate-save-vs-persist-vs-update-vs-merge', 'spring-autowired-and-dependency-injection', 'spring-applicationcontext-and-bean-lifecycle', 'spring-aop-and-advice-types'],
     'singleton-and-factory-patterns', 'design-image-hosting-service', None),
    ('Linked lists, intervals', ['reverse-linked-list-ii', 'linked-list-cycle-ii', 'merge-intervals', 'meeting-rooms-ii', 'find-the-duplicate-number'], 'merge-k-sorted-lists',
     ['java-reflection', 'spring-sync-vs-async-calls', 'spring-requestmapping-annotation', 'http-post-vs-put', 'sql-join-types'],
     'design-logging-framework', 'design-idempotent-api', None),
    ('Trees', ['binary-tree-zigzag-level-order-traversal', 'kth-smallest-element-in-a-bst', 'amount-of-time-for-binary-tree-to-be-infected', 'invert-binary-tree', 'maximum-width-of-binary-tree'], 'binary-tree-maximum-path-sum',
     ['jdbc-connection-pooling', 'spring-transactional-management', 'db-indexing-partitioning-sharding', 'sql-query-optimization', 'sql-optimization-performance-scalability'],
     'design-cache-pluggable-eviction', 'design-ticket-booking', None),
    ('Heaps, top-K', ['top-k-frequent-elements', 'top-k-frequent-words', 'single-threaded-cpu', 'implement-min-heap', 'meeting-rooms-iii'], 'find-median-from-data-stream',
     ['java-8-features', 'java-streams-coding-drills', 'java-garbage-collection', 'java-final-finally-finalize', 'java-volatile-and-synchronized'],
     'design-bounded-blocking-queue', 'design-distributed-cache', None),
    ('Graphs: BFS and DFS', ['number-of-islands', 'max-area-of-island', 'rotting-oranges', 'pacific-atlantic-water-flow', 'find-the-safest-path-in-a-grid'], 'word-ladder',
     ['runnable-callable-future-completablefuture', 'java-create-and-fix-deadlock', 'hashmap-internals', 'hashmap-vs-hashtable-vs-concurrenthashmap', 'describe-a-design-pattern'],
     'design-notification-system', 'design-chat-application', None),
    ('Graphs: topological sort, union-find, shortest paths', ['course-schedule-ii', 'is-graph-bipartite', 'the-earliest-moment-when-everyone-become-friends', 'dijkstra-shortest-path', 'cheapest-flights-within-k-stops'], 'alien-dictionary',
     ['inheritance-vs-composition', 'java-collections-hierarchy', 'how-to-synchronize-a-class', 'lock-free-stack-atomic-reference', 'producer-consumer-problem'],
     'design-rate-limiter', 'design-short-video-news-feed', None),
    ('Backtracking, tries', ['generate-parentheses', 'letter-combinations-of-a-phone-number', 'restore-ip-addresses', 'implement-trie-ii-prefix-tree', 'search-suggestions-system'], 'basic-calculator',
     ['debugging-across-microservices', 'redis-internals', 'rdbms-scaling-vs-nosql', 'system-design-database-scalability-performance-techniques', 'tcp-vs-udp'],
     'design-elevator-system', 'design-distributed-message-queue', 'walk-through-current-work'),
    ('Dynamic programming 1', ['best-time-to-buy-and-sell-stock', 'maximum-subarray', 'decode-ways', 'delete-and-earn', 'partition-equal-subset-sum'], 'maximum-profit-in-job-scheduling',
     ['cap-theorem', 'idempotency-in-payment-systems', 'kafka-architecture', 'messaging-systems-fundamentals', 'diagnose-low-cache-hit-ratio'],
     'MOCK', 'MOCK', 'most-challenging-project'),
    ('Dynamic programming 2, greedy', ['edit-distance', 'longest-palindromic-substring', 'dungeon-game', 'hand-of-straights', 'split-array-largest-sum'], 'russian-doll-envelopes',
     ['microservices-design-patterns', 'microservices-communication-grpc-vs-rest-eda', 'debug-java-memory-issues', 'optimize-slow-api', 'oauth-and-jwt'],
     'design-undo-redo-text-editor', 'design-payment-system', 'tell-me-about-a-conflict'),
    ('Design data structures', ['lru-cache', 'insert-delete-getrandom-o1', 'logger-rate-limiter', 'max-stack', 'first-unique-number'], 'lfu-cache',
     ['llm-cost-caching-and-observability', 'zero-downtime-schema-migrations', 'sql-injection-prevention', 'java-serialization', 'deep-vs-shallow-copy'],
     'MOCK', 'MOCK', 'tell-me-about-a-failure'),
]
# Week 13: Mon-Thu re-solves (the lessons finish on Monday), Fri-Sun review and final mocks.
W13_DSA = ['subarray-sum-equals-k', 'course-schedule-ii', 'top-k-frequent-elements', 'edit-distance']
W13_STRETCH = ['polymorphism-and-static-method-overriding', 'java-final-and-synchronized-methods', 'comparable-vs-comparator', 'collection-vs-collections']

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
              "8: API and data model; "
              "12: Draw the architecture; "
              "10: Two deep dives: the scarce resource, and what breaks after the write; "
              "5: Compare with the model answer and list what you missed")
MOCK_LLD = ("Ask Claude in chat for an unseen LLD mock: 45 minutes, scored; "
            "Classes first, patterns named, one key method coded, concurrency discussed; "
            "15: Log the score and the gaps in Mock progress")
MOCK_HLD = ("Ask Claude in chat for an unseen system design mock: 35 minutes, scored out of 10; "
            "Run the checklist: requirements, estimates, API, data model, architecture, deep dives, failures; "
            "10: Log the score and the gaps in Mock progress")
REVIEW = ("10: Say 5 things from this week's lessons out loud, 1 minute each, no notes; "
          "5: Note the one you couldn't explain: it goes first next week; "
          "Catch-up: if a core session this week is unfinished, do it now instead (anything still left is dropped, not carried)")

sessions = []
lessons = iter(PATH)


def add(day, s, tier='core', floor=False):
    row = {'date': day.isoformat(), 'tier': tier}
    if floor:
        row['floor'] = True
    sessions.append({**row, **s})


def lesson(day):
    mid = next(lessons)
    m = mods[mid]
    add(day, dict(minutes=30, title=f'Scratch · Academy {mid}: {m["title"]}', detail=LESSON_DETAIL), floor=True)


def dsa(day, pattern, slug, timed=False):
    solve = 25 if timed else 22
    add(day, dict(minutes=30, title=f'{"Timed DSA" if timed else "DSA"} · {pattern} · {bank[Q(slug)]["title"]}',
                  detail=DSA_DETAIL.format(solve=solve), questions=[slug]))


def stretch(day, slug):
    add(day, dict(minutes=30, title=f'Say it out loud · {bank[Q(slug)]["title"]}', detail=Q_DETAIL, questions=[slug]),
        tier='stretch')


# Day 0: kickoff
lesson(KICKOFF)
add(KICKOFF, dict(minutes=30, title='Kickoff · set up the next 13 weeks',
                  detail=("10: Read The rules and the map on the Plan page; "
                          "5: Pick your fixed daily time slot and put it in your calendar for every day until 31 Jan; "
                          "5: Settings: check the Gemini key with Test connection; "
                          "10: Open Two Sum (tomorrow's problem) and say the brute force out loud")))

for k, (pattern, wd, timed, qs, lld, hld, beh) in enumerate(W):
    monday = WEEK1_MONDAY + dt.timedelta(weeks=k)
    assert len(wd) == len(qs) == 5, k
    for i in range(5):
        day = monday + dt.timedelta(days=i)
        lesson(day)
        dsa(day, pattern, wd[i])
        stretch(day, qs[i])
    sat, sun = monday + dt.timedelta(days=5), monday + dt.timedelta(days=6)
    lesson(sat)
    if lld == 'MOCK':
        add(sat, dict(minutes=60, title='LLD mock with Claude (unseen problem)', detail=MOCK_LLD))
    else:
        add(sat, dict(minutes=60, title=f'LLD · {bank[Q(lld)]["title"]}', detail=LLD_DETAIL, questions=[lld]))
    dsa(sat, pattern, timed, timed=True)
    lesson(sun)
    if hld == 'MOCK':
        add(sun, dict(minutes=45, title='System design mock with Claude (unseen problem)', detail=MOCK_HLD))
    else:
        add(sun, dict(minutes=45, title=f'HLD · {bank[Q(hld)]["title"]}', detail=HLD_DETAIL, questions=[hld]))
    review = dict(minutes=15, title=f'Weekly review · week {k + 1}: {pattern}', detail=REVIEW)
    if beh:
        review['detail'] = REVIEW + "; Bonus: say your story for this week's behavioural question out loud (STAR, 2 minutes)"
        review['questions'] = [Q(beh)]
    add(sun, review)

# Week 13
monday = WEEK1_MONDAY + dt.timedelta(weeks=12)
for i, (p, s) in enumerate(zip(W13_DSA, W13_STRETCH)):
    day = monday + dt.timedelta(days=i)
    if i == 0:
        lesson(day)
    else:
        add(day, dict(minutes=30, title='Scratch · re-read the lesson you found hardest',
                      detail="Pick it from your weekly review notes; Answer its Check yourself questions out loud first; Say the 2-minute summary without looking"),
            floor=True)
    dsa(day, 'Re-solve, timed', p)
    stretch(day, s)
fri, sat, sun = (monday + dt.timedelta(days=d) for d in (4, 5, 6))
add(fri, dict(minutes=45, title='Review · the 3 problems you flagged hardest',
              detail="Re-solve each from a blank file, 15 minutes each; Say the pattern before you code"), floor=True)
add(fri, dict(minutes=15, title='Review · why you are looking for a change', detail="Say it out loud in under a minute; Positive, specific, about growth",
              questions=[Q('why-are-you-looking-for-a-change')]))
add(sat, dict(minutes=60, title='Final LLD mock with Claude', detail=MOCK_LLD), floor=True)
add(sat, dict(minutes=60, title='Review · concepts you missed in any weekly review', detail="Answer each out loud in 4 layers; Re-read only the layer you missed"))
add(sun, dict(minutes=45, title='Final system design mock with Claude', detail=MOCK_HLD), floor=True)
add(sun, dict(minutes=45, title='Rest and readiness check',
              detail="Read the requirements checklist and numbers card once; Prepare 3 stories: hardest project, conflict, failure; Stop by evening and sleep well"))

assert next(lessons, None) is None, 'every Academy lesson is scheduled'

GUIDE = open(os.path.join(os.path.dirname(__file__), 'guide.md'), encoding='utf-8').read()

header = {
    'id': 'backend-2027-01',
    'name': 'Backend SDE II / Senior: 13-week plan (1 Nov 2026 to 31 Jan 2027)',
    'role': 'SDE II / Senior Software Engineer, Java backend (any product company)',
    'companies': {c: 1.0 for c in sorted({s['company'] for q in bank.values() for s in q.get('sightings') or []})},
    'start': KICKOFF.isoformat(),
    'interview': INTERVIEW.isoformat(),
    'minutes': {'weekday': 60, 'weekend': 120},
    'review_days': 3,
    'notes': ('A general plan, not tied to one company: the whole Academy from scratch, DSA, LLD, HLD and system design, '
              'Java and Spring Boot internals. Core sessions are the plan; stretch sessions are extra. '
              'Ready-for-interviews date: Mon 1 Feb 2027.'),
}
with open(OUT, 'w', encoding='utf-8', newline='\n') as f:
    f.write('# Generated by tools/backend-plan/generate.py (13 weeks + a kickoff day). Each date is a fixed session;\n'
            '# tier core = the plan, stretch = optional, floor = the minimum for a bad day. Rules and the map: guide below.\n')
    f.write(yaml.safe_dump(header, sort_keys=False, allow_unicode=True, width=120))
    f.write('guide: |\n' + ''.join('  ' + line if line.strip() else '\n' for line in GUIDE.splitlines(True)))
    if not GUIDE.endswith('\n'):
        f.write('\n')
    f.write(yaml.safe_dump({'sessions': sessions}, sort_keys=False, allow_unicode=True, width=200))

days = sorted({s['date'] for s in sessions})
core = lambda d: sum(s['minutes'] for s in sessions if s['date'] == d and s['tier'] == 'core')
print('sessions', len(sessions), 'days', len(days), days[0], days[-1])
print('lessons', sum(1 for s in sessions if s['title'].startswith('Scratch · Academy')),
      'dsa', sum(1 for s in sessions if 'DSA' in s['title']), 'lld', sum(1 for s in sessions if s['title'].startswith('LLD ·')),
      'hld', sum(1 for s in sessions if s['title'].startswith('HLD ·')), 'mocks', sum(1 for s in sessions if 'mock' in s['title'].lower()),
      'stretch', sum(1 for s in sessions if s['tier'] == 'stretch'))
print('core minutes per day:', sorted({core(d) for d in days}), '| days without a floor:',
      [d for d in days if not any(s.get('floor') for s in sessions if s['date'] == d)])
