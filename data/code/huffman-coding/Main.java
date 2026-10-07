import java.util.*;

public class Main {
    public static void main(String[] args) {
        IO in = IO.stdin();
        int[] freq = in.nextIntArray();
        String[] codes = new Solution().huffmanCodes(freq);
        IO.print(check(freq, codes));
    }

    // Many optimal codes exist, so validate the code and report its total length instead of the codewords.
    static Object check(int[] freq, String[] codes) {
        if (codes == null || codes.length != freq.length)
            return "invalid: expected " + freq.length + " codewords";
        long total = 0;
        for (int i = 0; i < codes.length; i++) {
            String c = codes[i];
            if (c == null || c.isEmpty()) return "invalid: codeword " + i + " is empty";
            for (char ch : c.toCharArray())
                if (ch != '0' && ch != '1') return "invalid: codeword " + i + " is not binary";
            total += (long) freq[i] * c.length();
        }
        // After sorting, if any codeword is a prefix of another, it is a prefix of its next neighbour.
        Integer[] order = new Integer[codes.length];
        for (int i = 0; i < order.length; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> codes[a].compareTo(codes[b]));
        for (int k = 0; k + 1 < order.length; k++) {
            if (codes[order[k + 1]].startsWith(codes[order[k]]))
                return "invalid: codeword " + order[k] + " is a prefix of codeword " + order[k + 1];
        }
        return total;
    }
}
