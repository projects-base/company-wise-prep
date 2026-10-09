**Short answer:** Model the map as a weighted graph: intersections are nodes with (x, y) coordinates, roads are edges with a length. Shortest distance is Dijkstra (or A* with straight-line distance as the heuristic). Nearest landmarks are a multi-source question: either run Dijkstra from the query point and stop after k landmarks of the wanted type, or pre-filter candidates with a spatial grid index and then confirm by road distance.

## Requirements

- Points on a 2-D plane; roads connect points with non-negative lengths (directed or undirected).
- `shortestDistance(from, to)` returns the road distance, or "unreachable".
- `nearestLandmarks(point, type, k)` returns the k closest landmarks of a type (SCHOOL, HOSPITAL) by road distance.
- Landmarks can be added at runtime. Read-heavy workload.
- Out of scope: live traffic, turn restrictions.

## Classes

- `Point` (record: id, x, y): a node.
- `Edge` (record: to, weight): an adjacency entry.
- `Landmark` (record: id, name, type, pointId): attached to a node.
- `LandmarkType` (enum).
- `MapGraph`: owns the adjacency list and the landmark registry. Only knows structure.
- `PathFinder` (interface): `distance(graph, from, to)`. Implementations `DijkstraPathFinder`, `AStarPathFinder`.
- `LandmarkLocator`: answers nearest-k queries using the graph plus a `GridIndex`.
- `GridIndex`: buckets points into fixed-size cells for cheap "what is near (x, y)" lookups.

## Patterns used

- **Strategy** for `PathFinder`: Dijkstra for general use, A* when coordinates give an admissible heuristic (straight-line distance never overestimates road distance). Callers do not change.
- **Facade**: a `MapService` exposes the two queries and hides graph, index and algorithm choice.
- SRP: the graph stores data, algorithms live elsewhere, so you can test each alone.

## Code

```java
enum LandmarkType { SCHOOL, HOSPITAL, PARK }

record Point(int id, double x, double y) {}
record Edge(int to, double weight) {}
record Landmark(int id, String name, LandmarkType type, int pointId) {}
record Hit(Landmark landmark, double distance) {}

final class MapGraph {
    private final Map<Integer, Point> points = new HashMap<>();
    private final Map<Integer, List<Edge>> adj = new HashMap<>();
    private final Map<Integer, List<Landmark>> landmarksAt = new HashMap<>();

    void addPoint(Point p) { points.put(p.id(), p); adj.putIfAbsent(p.id(), new ArrayList<>()); }
    void addRoad(int a, int b, double w) {          // undirected
        adj.get(a).add(new Edge(b, w));
        adj.get(b).add(new Edge(a, w));
    }
    void addLandmark(Landmark l) { landmarksAt.computeIfAbsent(l.pointId(), k -> new ArrayList<>()).add(l); }
    List<Edge> edges(int id) { return adj.getOrDefault(id, List.of()); }
    List<Landmark> landmarksAt(int id) { return landmarksAt.getOrDefault(id, List.of()); }
    Point point(int id) { return points.get(id); }
}

interface PathFinder { OptionalDouble distance(MapGraph g, int from, int to); }

final class DijkstraPathFinder implements PathFinder {
    public OptionalDouble distance(MapGraph g, int from, int to) {
        record State(int node, double dist) {}
        var best = new HashMap<Integer, Double>();
        var pq = new PriorityQueue<State>(Comparator.comparingDouble(State::dist));
        best.put(from, 0.0);
        pq.add(new State(from, 0));
        while (!pq.isEmpty()) {
            State s = pq.poll();
            if (s.dist() > best.getOrDefault(s.node(), Double.MAX_VALUE)) continue; // stale entry
            if (s.node() == to) return OptionalDouble.of(s.dist());               // early exit
            for (Edge e : g.edges(s.node())) {
                double nd = s.dist() + e.weight();
                if (nd < best.getOrDefault(e.to(), Double.MAX_VALUE)) {
                    best.put(e.to(), nd);
                    pq.add(new State(e.to(), nd));
                }
            }
        }
        return OptionalDouble.empty();
    }
}

final class LandmarkLocator {
    // Dijkstra from the query point; nodes pop in distance order,
    // so the first k landmarks we meet are the k nearest by road.
    List<Hit> nearest(MapGraph g, int from, LandmarkType type, int k) {
        record State(int node, double dist) {}
        var best = new HashMap<Integer, Double>();
        var pq = new PriorityQueue<State>(Comparator.comparingDouble(State::dist));
        var result = new ArrayList<Hit>();
        best.put(from, 0.0);
        pq.add(new State(from, 0));
        while (!pq.isEmpty() && result.size() < k) {
            State s = pq.poll();
            if (s.dist() > best.get(s.node())) continue;
            for (Landmark l : g.landmarksAt(s.node()))
                if (l.type() == type && result.size() < k) result.add(new Hit(l, s.dist()));
            for (Edge e : g.edges(s.node())) {
                double nd = s.dist() + e.weight();
                if (nd < best.getOrDefault(e.to(), Double.MAX_VALUE)) {
                    best.put(e.to(), nd);
                    pq.add(new State(e.to(), nd));
                }
            }
        }
        return result;
    }
}
```

Complexity: Dijkstra with a binary heap is O((V + E) log V). The landmark search stops early, so in a dense city it touches only the neighbourhood around the point.

## Extensions

- **A\***: same loop, but order the heap by `dist + euclid(node, target)`. Straight-line distance is admissible when edge weights are at least the Euclidean length, so the answer stays optimal and far fewer nodes are expanded.
- **"Nearest" by straight line instead of road**: use the `GridIndex` (cell size about the typical query radius) or a k-d tree / quadtree. Search the query cell, then rings of neighbour cells until k candidates are found and the next ring cannot be closer.
- **Many queries for the same type**: precompute with a multi-source Dijkstra, seeding every hospital at distance 0. One run gives each node its nearest hospital. Recompute or patch when a hospital is added.
- **Huge maps**: contraction hierarchies or precomputed landmark distances (ALT) for fast point-to-point queries; partition the graph by region.
- **Concurrency**: reads dominate. Build an immutable graph snapshot and swap a reference (`volatile` or `AtomicReference`) on updates, so queries never lock. Each query keeps its own `best` map and heap, so queries are thread-safe by construction.
- **Negative weights** do not exist on roads; if they did, Dijkstra would be wrong and you would need Bellman-Ford.

Related: [E5 · The LLD interview method](../academy/lessons/E5.md), [E4 · Behavioural patterns](../academy/lessons/E4.md).
