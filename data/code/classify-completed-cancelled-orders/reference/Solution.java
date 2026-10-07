import java.util.*;

class Solution {
    enum State {
        NEW, PLACED, PROCESSED, SHIPPED, DELIVERED, CANCELLED;

        /** The state after `event`, or this state unchanged if the event is not allowed here. */
        State on(String event) {
            switch (event) {
                case "placed": return this == NEW ? PLACED : this;
                case "processed": return this == PLACED ? PROCESSED : this;
                case "shipped": return this == PROCESSED ? SHIPPED : this;
                case "delivered": return this == SHIPPED ? DELIVERED : this;
                case "cancelled": return this == PLACED || this == PROCESSED || this == SHIPPED ? CANCELLED : this;
                default: return this;
            }
        }
    }

    public List<List<Integer>> classifyOrders(int[] orderIds, String[] statuses) {
        Map<Integer, State> state = new HashMap<>();
        for (int i = 0; i < orderIds.length; i++) {
            State cur = state.getOrDefault(orderIds[i], State.NEW);
            state.put(orderIds[i], cur.on(statuses[i]));
        }
        List<Integer> completed = new ArrayList<>(), cancelled = new ArrayList<>();
        for (Map.Entry<Integer, State> e : state.entrySet()) {
            if (e.getValue() == State.DELIVERED) completed.add(e.getKey());
            else if (e.getValue() == State.CANCELLED) cancelled.add(e.getKey());
        }
        Collections.sort(completed);
        Collections.sort(cancelled);
        return List.of(completed, cancelled);
    }
}
