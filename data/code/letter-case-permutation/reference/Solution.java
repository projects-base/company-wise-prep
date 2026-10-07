import java.util.*;

class Solution {
    public List<String> letterCasePermutation(String s) {
        List<String> out = new ArrayList<>();
        build(s.toCharArray(), 0, out);
        return out;
    }

    private void build(char[] cs, int i, List<String> out) {
        if (i == cs.length) {
            out.add(new String(cs));
            return;
        }
        if (Character.isLetter(cs[i])) {
            cs[i] = Character.toLowerCase(cs[i]);
            build(cs, i + 1, out);
            cs[i] = Character.toUpperCase(cs[i]);
            build(cs, i + 1, out);
        } else {
            build(cs, i + 1, out);
        }
    }
}
