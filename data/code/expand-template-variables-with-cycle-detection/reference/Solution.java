import java.util.*;

class Solution {
    private Map<String, String> vars;
    private final Map<String, String> done = new HashMap<>();   // name -> expanded value
    private final Set<String> inProgress = new HashSet<>();     // names on the current path

    public String expand(Map<String, String> vars, String template) {
        this.vars = vars;
        return expandText(template);
    }

    // Expands one piece of text; null means failure.
    private String expandText(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0, n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (c != '%') {
                sb.append(c);
                i++;
            } else if (i + 1 < n && s.charAt(i + 1) == '%') {
                sb.append('%');
                i += 2;
            } else {
                int close = s.indexOf('%', i + 1);
                if (close < 0) return null; // unclosed reference
                String value = resolve(s.substring(i + 1, close));
                if (value == null) return null;
                sb.append(value);
                i = close + 1;
            }
        }
        return sb.toString();
    }

    private String resolve(String name) {
        String memo = done.get(name);
        if (memo != null) return memo;
        if (!vars.containsKey(name)) return null;    // missing variable
        if (!inProgress.add(name)) return null;      // already on the path: cycle
        String value = expandText(vars.get(name));
        inProgress.remove(name);
        if (value != null) done.put(name, value);
        return value;
    }
}
