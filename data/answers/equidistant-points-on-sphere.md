**Short answer:** Four. The vertices of a regular tetrahedron inscribed in the sphere are all at the same distance from each other. Five is impossible, because five mutually equidistant points would form a regular 4-simplex, which needs four dimensions, and a sphere sits in three-dimensional space.

## Explanation

Build it up:

- **2 points:** any two points are equidistant from each other.
- **3 points:** an equilateral triangle. Any plane cut through the sphere gives a circle; put an equilateral triangle on that circle.
- **4 points:** place a fourth point equidistant from all three. The set of points equidistant from the triangle's vertices is the line through the triangle's centre perpendicular to its plane. Moving along that line until the distance equals the side gives a regular tetrahedron. Every regular tetrahedron has a circumscribed sphere, so scale it to fit.
- **5 points:** the fifth point would also have to lie on the perpendicular line through the centre of every face triangle. For the face (A, B, C) the only points at distance `s` from A, B and C on that line are D and its mirror image D' across the plane ABC. D' is at distance about 1.63 `s` from D (`2·sqrt(2/3)·s`), not `s`. So no fifth point exists.

General fact: in `n`-dimensional space, at most `n + 1` points can be mutually equidistant (the regular simplex). In 3D that is 4.

**Does "distance" mean straight-line or along the surface?** It does not matter. On a sphere the great-circle distance is a strictly increasing function of the chord length, so "equal chords" and "equal arcs" are the same condition.

## Example

Coordinates on a sphere of radius `sqrt(3)`, centred at the origin:

```text
(1, 1, 1)  (1, -1, -1)  (-1, 1, -1)  (-1, -1, 1)
```

Every pair differs in exactly two coordinates by 2, so every distance is `sqrt(4 + 4) = sqrt(8)`. Each point has norm `sqrt(3)`, so all lie on the sphere.

## Pitfalls and follow-ups

- **Common wrong answers:** 6 (octahedron - opposite vertices are further apart) or "infinitely many on a circle" (a circle gives equal spacing to neighbours, not to every pair).
- **What about points "evenly spread" on a sphere?** That is a different problem (Thomson / Tammes problem): maximise the minimum distance. Only some counts (4, 6, 12) have perfectly symmetric solutions.
- **On a circle (2D)?** Three (equilateral triangle) - again `n + 1` with `n = 2`.
- **How to say it in an interview:** give the answer, the tetrahedron construction, and the one-line dimension argument. Interviewers mostly check that you reason rather than guess.
