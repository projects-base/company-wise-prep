import java.util.*;

class Logger {
    private final Map<String, Integer> nextAllowed = new HashMap<>();

    public Logger() {
    }

    public boolean shouldPrintMessage(int timestamp, String message) {
        Integer t = nextAllowed.get(message);
        if (t != null && timestamp < t) return false;
        nextAllowed.put(message, timestamp + 10);
        return true;
    }
}
