import java.util.*;

class Solution {
    public List<String> restoreIpAddresses(String s) {
        List<String> out = new ArrayList<>();
        build(s, 0, new ArrayList<>(), out);
        return out;
    }

    private void build(String s, int at, List<String> parts, List<String> out) {
        if (parts.size() == 4) {
            if (at == s.length()) out.add(String.join(".", parts));
            return;
        }
        int left = s.length() - at, need = 4 - parts.size();
        if (left < need || left > 3 * need) return;
        for (int len = 1; len <= 3 && at + len <= s.length(); len++) {
            String part = s.substring(at, at + len);
            if (len > 1 && part.charAt(0) == '0') break;
            if (Integer.parseInt(part) > 255) break;
            parts.add(part);
            build(s, at + len, parts, out);
            parts.remove(parts.size() - 1);
        }
    }
}
