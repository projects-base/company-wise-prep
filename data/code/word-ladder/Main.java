import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        String beginWord = in.nextString();
        String endWord = in.nextString();
        List<String> wordList = in.nextStringList();
        IO.print(new Solution().ladderLength(beginWord, endWord, wordList));
    }
}
