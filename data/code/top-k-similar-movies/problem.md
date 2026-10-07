There are `n` movies labelled `0..n-1`; `ratings[i]` is the rating of movie `i`. Each pair `[a, b]` in `similar` says movies `a` and `b` are similar. Similarity is transitive: two movies are similar if they are linked through a chain of similar pairs. Given a movie `movie` and an integer `k`, return the `k` highest-rated movies that are similar to `movie`, excluding `movie` itself. Order them by rating, highest first, and break ties by smaller label. If fewer than `k` movies are similar, return all of them.

**Example 1**
Input: ratings = [6,7,9,4,8], similar = [[0,1],[1,2],[3,4]], movie = 0, k = 2
Output: [2,1]
Why: movies 1 and 2 are linked to 0 (2 through 1). Movie 2 is rated 9 and movie 1 is rated 7.

**Example 2**
Input: ratings = [5,5,5,1], similar = [[0,3],[3,1],[3,2]], movie = 3, k = 2
Output: [0,1]
Why: three similar movies share the rating 5, so the smaller labels win.

**Example 3**
Input: ratings = [3,8], similar = [], movie = 1, k = 4
Output: []
Why: movie 1 has no similar movies.

**Constraints**
- 1 ≤ n ≤ 10⁵, 0 ≤ similar.length ≤ 10⁵
- 0 ≤ ratings[i] ≤ 10⁹
- 0 ≤ a, b, movie < n, a ≠ b
- 1 ≤ k ≤ n

**Notes**: aim for O((n + e) + c log k), where c is the number of similar movies, using a size-k heap.
