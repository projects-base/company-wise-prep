import java.util.*;

class Solution {
    public String simplifyPath(String path) {
        Deque<String> stack = new ArrayDeque<>();
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                if (!stack.isEmpty()) stack.pollLast();
            } else {
                stack.addLast(part);
            }
        }
        if (stack.isEmpty()) return "/";
        StringBuilder sb = new StringBuilder();
        for (String dir : stack) sb.append('/').append(dir);
        return sb.toString();
    }
}
