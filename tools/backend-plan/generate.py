"""Generates data/campaigns/backend-2027-01/campaign.yaml: a strict 13-week syllabus as fixed sessions.

Run from the repo root: python tools/backend-plan/generate.py . (edit the W table or guide.md, then rerun)."""
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
mods = {m['id']: m['title'] for t in cur['tracks'] for m in t['modules']}


def Q(slug):
    assert slug in bank, f'missing question {slug}'
    return slug


def L(mid):
    assert mid in mods, f'missing module {mid}'
    return f'Academy {mid}: {mods[mid]}'


# week: (dsa pattern, [weekday dsa], saturday timed dsa, concept theme, [concepts], (lld lesson, lld problem|MOCK),
#        (hld lesson, hld case|MOCK|LESSON:<id>), behavioural for the Sunday gate)
W = [
    ('Arrays, hashing, two pointers', ['two-sum', '3sum', 'container-with-most-water', 'longest-consecutive-sequence'], 'trapping-rain-water',
     'OOP and core Java', ['Q:oop-four-pillars', 'Q:java-equals-vs-double-equals-hashcode', 'Q:immutable-class', 'Q:java-checked-vs-unchecked-exceptions'],
     ('E1', 'design-parking-lot'), ('F1', 'LESSON:H4'), None),
    ('Sliding window, prefix sums', ['longest-substring-without-repeating-characters', 'permutation-in-string', 'longest-repeating-character-replacement', 'subarray-sum-equals-k', 'max-consecutive-ones-iii'], 'minimum-window-substring',
     'Collections internals', ['Q:hashmap-internals', 'Q:hashmap-vs-hashtable-vs-concurrenthashmap', 'Q:java-collections-hierarchy', 'Q:fail-fast-vs-fail-safe-iterators', 'Q:comparable-vs-comparator'],
     ('E2', 'singleton-and-factory-patterns'), ('F3', 'design-url-shortener'), None),
    ('Stacks, binary search', ['valid-parentheses', 'min-stack', 'basic-calculator-ii', 'search-in-rotated-sorted-array', 'koko-eating-bananas'], 'sliding-window-maximum',
     'JVM: memory and GC', ['L:A1', 'L:A2', 'Q:java-stack-vs-heap-memory', 'L:A4', 'Q:java-garbage-collection'],
     ('E3', 'design-cache-pluggable-eviction'), ('F2', 'design-image-hosting-service'), None),
    ('Linked lists, intervals', ['reverse-linked-list-ii', 'linked-list-cycle-ii', 'merge-intervals', 'meeting-rooms-ii', 'find-the-duplicate-number'], 'merge-k-sorted-lists',
     'JVM internals and modern Java', ['L:A3', 'Q:jvm-internals', 'L:A7', 'Q:java-8-features', 'Q:java-streams-coding-drills'],
     ('E4', 'design-vending-machine'), ('F4', 'MOCK'), None),
    ('Trees', ['binary-tree-zigzag-level-order-traversal', 'kth-smallest-element-in-a-bst', 'amount-of-time-for-binary-tree-to-be-infected', 'convert-binary-search-tree-to-sorted-doubly-linked-list', 'maximum-width-of-binary-tree'], 'binary-tree-maximum-path-sum',
     'Concurrency 1: threads, locks, memory model', ['L:B1', 'L:B2', 'Q:java-create-and-fix-deadlock', 'L:A6', 'Q:java-volatile-and-synchronized'],
     ('E5', 'design-rate-limiter'), ('F5', 'design-chat-application'), None),
    ('Heaps, top-K', ['top-k-frequent-elements', 'top-k-frequent-words', 'single-threaded-cpu', 'implement-min-heap', 'maximum-product-of-k-elements'], 'find-median-from-data-stream',
     'Concurrency 2: atomics, executors, futures', ['L:B3', 'L:B4', 'Q:runnable-callable-future-completablefuture', 'L:B5', 'L:B6'],
     ('E6', 'MOCK'), ('F6', 'design-distributed-cache'), None),
    ('Graphs: BFS and DFS', ['number-of-islands', 'max-area-of-island', 'rotting-oranges', 'pacific-atlantic-water-flow', 'shortest-bridge'], 'word-ladder',
     'Spring core: IoC, DI, bean lifecycle', ['L:D2', 'L:L4', 'Q:spring-applicationcontext-and-bean-lifecycle', 'Q:spring-autowired-and-dependency-injection', 'Q:what-is-spring-framework'],
     ('B7', 'design-bounded-blocking-queue'), ('Q8', 'design-short-video-news-feed'), None),
    ('Graphs: topological sort, union-find, shortest paths', ['course-schedule-ii', 'is-graph-bipartite', 'the-earliest-moment-when-everyone-become-friends', 'dijkstra-shortest-path', 'cheapest-flights-within-k-stops'], 'alien-dictionary',
     'Spring AOP and Boot internals', ['L:D3', 'Q:spring-aop-and-advice-types', 'L:D4', 'L:L5', 'L:D9'],
     ('D1', 'design-elevator-system'), ('S8', 'MOCK'), None),
    ('Backtracking, tries', ['generate-parentheses', 'letter-combinations-of-a-phone-number', 'restore-ip-addresses', 'implement-trie-ii-prefix-tree', 'search-suggestions-system'], 'basic-calculator',
     'Web layer and security', ['L:S2', 'Q:servlets-and-dispatcherservlet', 'L:D5', 'L:L6', 'L:D7'],
     ('C4', 'design-notification-system'), ('S9', 'design-distributed-message-queue'), 'walk-through-current-work'),
    ('Dynamic programming 1', ['best-time-to-buy-and-sell-stock', 'maximum-subarray', 'decode-ways', 'delete-and-earn', 'partition-equal-subset-sum'], 'maximum-profit-in-job-scheduling',
     'Data: JPA, transactions, pooling', ['L:D6', 'Q:spring-transactional-management', 'L:L7', 'Q:hibernate-save-vs-persist-vs-update-vs-merge', 'Q:jdbc-connection-pooling'],
     ('H2', 'MOCK'), ('Q6', 'design-payment-system'), 'most-challenging-project'),
    ('Dynamic programming 2, greedy', ['edit-distance', 'longest-palindromic-substring', 'grid-paths-with-checkpoints', 'hand-of-straights', 'heaters'], 'russian-doll-envelopes',
     'SQL you write by hand', ['L:Q2', 'L:Q3', 'L:Q4', 'Q:sql-scenario-based-queries', 'Q:second-highest-salary'],
     ('C1', 'design-logging-framework'), ('F7', 'design-job-scheduler'), 'tell-me-about-a-conflict'),
    ('Design data structures', ['lru-cache', 'insert-delete-getrandom-o1', 'logger-rate-limiter', 'max-stack', 'moving-average-from-data-stream'], 'lfu-cache',
     'Database internals', ['L:Q5', 'Q:sql-query-optimization', 'L:Q6', 'L:Q7', 'Q:sql-injection-prevention'],
     ('H1', 'design-undo-redo-text-editor'), ('F8', 'MOCK'), 'tell-me-about-a-failure'),
]
# Week 13: Mon-Thu re-solves and concepts, Fri-Sun review (the last 3 days).
W13_DSA = ['subarray-sum-equals-k', 'course-schedule-ii', 'top-k-frequent-elements', 'edit-distance']
W13_CONCEPT = ['Q:microservices-design-patterns', 'Q:kafka-architecture', 'Q:oauth-and-jwt', 'Q:optimize-slow-api']

DSA_DETAIL = ("2: Read it and say the brute force and its complexity out loud; "
              "20: Solve it in the Playground with a timer, no hints; "
              "If stuck at 20 minutes, read only the Approach section of the model answer, then finish it yourself; "
              "3: Write the pattern and the complexity in one line in your notes")
LESSON_DETAIL = ("{lesson}: read it in one-part-at-a-time mode; "
                 "Answer its Check yourself questions out loud before reading the answers; "
                 "Say the 2-minute summary out loud without looking")
Q_DETAIL = ("8: Answer out loud in 4 layers: what it is, how it works inside, where you used it, what goes wrong; "
            "8: Open the model answer: compare, then say every follow-up answer out loud; "
            "4: Write one line for each layer you missed")
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
MOCK_LLD = ("Ask Claude in chat for an unseen LLD mock: 45 minutes, scored; "
            "Classes first, patterns named, one key method coded, concurrency discussed; "
            "15: Log the score and the gaps in Mock progress")
MOCK_HLD = ("Ask Claude in chat for an unseen system design mock: 45 minutes, scored out of 10; "
            "Run the checklist: requirements, estimates, API, data model, architecture, deep dives, failures; "
            "15: Log the score and the gaps in Mock progress")
GATE = ("15: Answer this week's 5 concept items out loud, 1 minute each, no notes, in 4 layers; "
        "15: Re-solve one of this week's DSA problems from a blank file; "
        "Pass = 4 of 5 concepts clear and the problem solved; "
        "Fail = next Saturday's timed DSA slot repeats this week's weakest problem, and the missed concepts go first next week")


def concept(entry):
    kind, ref = entry.split(':', 1)
    if kind == 'L':
        return dict(minutes=20, title=f'Concept · {L(ref)}', detail=LESSON_DETAIL.format(lesson=f'Academy {ref}'))
    return dict(minutes=20, title=f'Concept · {bank[Q(ref)]["title"]}', detail=Q_DETAIL, questions=[ref])


def dsa(pattern, slug, timed=False):
    s = dict(minutes=30 if timed else 25,
             title=f'{"Timed DSA" if timed else "DSA"} · {pattern} · {bank[Q(slug)]["title"]}',
             detail=DSA_DETAIL if not timed else DSA_DETAIL.replace('20: Solve it', '25: Solve it').replace('20 minutes', '25 minutes'),
             questions=[slug])
    return s


START = dt.date(2026, 10, 27)
WEEK1_MONDAY = dt.date(2026, 10, 26)
sessions = []


def add(day, s):
    sessions.append({'date': day.isoformat(), **s})


for k, (pattern, wd, timed, theme, concepts, (lld_l, lld_p), (hld_l, hld_c), beh) in enumerate(W):
    monday = WEEK1_MONDAY + dt.timedelta(weeks=k)
    weekdays = [monday + dt.timedelta(days=i) for i in range(5) if monday + dt.timedelta(days=i) >= START]
    assert len(weekdays) == len(wd) == len(concepts), (k, len(weekdays), len(wd), len(concepts))
    for day, p, c in zip(weekdays, wd, concepts):
        add(day, dsa(pattern, p))
        add(day, concept(c))
    sat, sun = monday + dt.timedelta(days=5), monday + dt.timedelta(days=6)
    add(sat, dict(minutes=30, title=f'LLD lesson · {L(lld_l)}', detail=LESSON_DETAIL.format(lesson=f'Academy {lld_l}')))
    if lld_p == 'MOCK':
        add(sat, dict(minutes=60, title='LLD mock with Claude (unseen problem)', detail=MOCK_LLD))
    else:
        add(sat, dict(minutes=60, title=f'LLD · {bank[Q(lld_p)]["title"]}', detail=LLD_DETAIL, questions=[lld_p]))
    add(sat, dsa(pattern, timed, timed=True))
    add(sun, dict(minutes=30, title=f'HLD lesson · {L(hld_l)}', detail=LESSON_DETAIL.format(lesson=f'Academy {hld_l}')))
    if hld_c == 'MOCK':
        add(sun, dict(minutes=60, title='System design mock with Claude (unseen problem)', detail=MOCK_HLD))
    elif hld_c.startswith('LESSON:'):
        m = hld_c.split(':')[1]
        add(sun, dict(minutes=60, title=f'HLD lesson · {L(m)}', detail=LESSON_DETAIL.format(lesson=f'Academy {m}') +
                      '; Write the requirements checklist and the numbers card from memory'))
    else:
        add(sun, dict(minutes=60, title=f'HLD · {bank[Q(hld_c)]["title"]}', detail=HLD_DETAIL, questions=[hld_c]))
    gate = dict(minutes=30, title=f'Weekly gate · week {k + 1}: {theme} + {pattern}', detail=GATE)
    if beh:
        gate['detail'] = GATE + "; Bonus: say your story for this week's behavioural question out loud (STAR, 2 minutes)"
        gate['questions'] = [Q(beh)]
    add(sun, gate)

# Week 13
monday = WEEK1_MONDAY + dt.timedelta(weeks=12)
for i, (p, c) in enumerate(zip(W13_DSA, W13_CONCEPT)):
    day = monday + dt.timedelta(days=i)
    add(day, dsa('Re-solve, timed', p))
    add(day, concept(c))
fri, sat, sun = (monday + dt.timedelta(days=d) for d in (4, 5, 6))
add(fri, dict(minutes=45, title='Review · the 3 problems you flagged hardest',
              detail="Re-solve each from a blank file, 15 minutes each; Say the pattern before you code", questions=[Q('why-are-you-looking-for-a-change')]))
add(sat, dict(minutes=60, title='Final LLD mock with Claude', detail=MOCK_LLD))
add(sat, dict(minutes=60, title='Review · concepts you failed in any weekly gate', detail="Answer each out loud in 4 layers; Re-read only the layer you missed"))
add(sun, dict(minutes=60, title='Final system design mock with Claude', detail=MOCK_HLD))
add(sun, dict(minutes=60, title='Rest and readiness check',
              detail="Read the requirements checklist and numbers card once; Prepare 3 stories: hardest project, conflict, failure; Stop by evening and sleep well"))

GUIDE = open(os.path.join(os.path.dirname(__file__), 'guide.md'), encoding='utf-8').read()

header = {
    'id': 'backend-2027-01',
    'name': 'Backend SDE II / Senior: 13-week plan (27 Oct 2026 to 24 Jan 2027)',
    'role': 'SDE II / Senior Software Engineer, Java backend (any product company)',
    'companies': {c: 1.0 for c in sorted({s['company'] for q in bank.values() for s in q.get('sightings') or []})},
    'start': '2026-10-27',
    'interview': '2027-01-25',
    'minutes': {'weekday': 45, 'weekend': 120},
    'review_days': 3,
    'notes': ('A general plan, not tied to one company: DSA, LLD, HLD and system design, Java and Spring Boot internals. '
              'Every day is fixed below; the strict rules are in the guide. Ready-for-interviews date: Mon 25 Jan 2027.'),
}
with open(OUT, 'w', encoding='utf-8', newline='\n') as f:
    f.write('# Generated from a weekly syllabus (13 weeks). Each date is a fixed session; questions link to\n'
            '# bank items with model answers (examples + follow-ups). Rules and the weekly map: guide below.\n')
    f.write(yaml.safe_dump(header, sort_keys=False, allow_unicode=True, width=120))
    f.write('guide: |\n' + ''.join('  ' + line if line.strip() else '\n' for line in GUIDE.splitlines(True)))
    if not GUIDE.endswith('\n'):
        f.write('\n')
    f.write(yaml.safe_dump({'sessions': sessions}, sort_keys=False, allow_unicode=True, width=200))

days = sorted({s['date'] for s in sessions})
print('sessions', len(sessions), 'days', len(days), days[0], days[-1])
print('dsa', sum(1 for s in sessions if 'DSA' in s['title']), 'lld', sum(1 for s in sessions if s['title'].startswith('LLD ·')),
      'hld', sum(1 for s in sessions if s['title'].startswith('HLD ·')), 'mocks', sum(1 for s in sessions if 'mock' in s['title'].lower()))
