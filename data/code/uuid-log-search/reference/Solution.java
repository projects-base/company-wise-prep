import java.util.*;

class Solution {
    public List<Integer> searchLogs(String[] logs, String query) {
        List<Integer> out = new ArrayList<>();
        int m = query.length();
        for (int i = 0; i < logs.length; i++) {
            String line = logs[i];
            for (int p = 0; p + m <= line.length(); p++) {
                if (!line.regionMatches(true, p, query, 0, m)) continue;
                boolean leftOk = p == 0 || !isWordChar(line.charAt(p - 1));
                boolean rightOk = p + m == line.length() || !isWordChar(line.charAt(p + m));
                if (leftOk && rightOk) {
                    out.add(i);
                    break;
                }
            }
        }
        return out;
    }

    private boolean isWordChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-';
    }
}
