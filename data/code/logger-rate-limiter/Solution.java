import java.util.*;

class Logger {
    // Print a message only if it was not printed in the last 10 seconds. Timestamps are non-decreasing.
    public Logger() {
    }

    public boolean shouldPrintMessage(int timestamp, String message) {
        return false;
    }
}
