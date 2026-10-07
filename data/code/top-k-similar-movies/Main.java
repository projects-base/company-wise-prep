import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] ratings = in.nextIntArray();
        int[][] similar = in.nextIntMatrix();
        int movie = in.nextInt();
        int k = in.nextInt();
        IO.print(new Solution().topSimilarMovies(ratings, similar, movie, k));
    }
}
