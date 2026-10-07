A help forum has a list of open questions and a list of volunteers. Every question carries a set of topic tags (`questions[i]`), and every volunteer lists the tags they are skilled in (`volunteers[j]`). Volunteer `j` is able to answer question `i` when the two share **at least one** tag.

Each volunteer can be given at most one question, and each question needs at most one volunteer. Return the largest number of questions that can be assigned at the same time.

**Example 1**
Input: questions = [["java","spring"],["python"],["sql"]], volunteers = [["java"],["python","sql"]]
Output: 2
Why: volunteer 0 takes question 0 and volunteer 1 takes question 1 (or question 2). Only two volunteers exist, so 2 is the maximum.

**Example 2**
Input: questions = [["go"],["go"],["rust"]], volunteers = [["go","rust"],["go"]]
Output: 2
Why: give question 0 to volunteer 1 and question 2 to volunteer 0. Giving a "go" question to volunteer 0 first would leave question 2 unanswered — a greedy choice is not enough.

**Example 3**
Input: questions = [["c"]], volunteers = [["java"]]
Output: 0

**Constraints**
- 1 ≤ questions.length, volunteers.length ≤ 500
- each question and volunteer has 1 to 5 tags; a tag is 1 to 12 lowercase letters, digits or `+`/`#`
- tags inside one list are distinct

**Notes**: tag matching is exact and case-sensitive. Only the number of assigned questions is returned, so any optimal assignment gives the same answer. This is maximum bipartite matching (augmenting paths / Hopcroft–Karp); greedy assignment fails on cases like Example 2.
