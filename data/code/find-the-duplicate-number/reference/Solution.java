import java.util.*;

class Solution {
    public int findDuplicate(int[] nums) {
        // Treat i -> nums[i] as a linked list; the duplicate is where the cycle begins.
        int slow = nums[0], fast = nums[0];
        do {
            slow = nums[slow];
            fast = nums[nums[fast]];
        } while (slow != fast);
        slow = nums[0];
        while (slow != fast) {
            slow = nums[slow];
            fast = nums[fast];
        }
        return slow;
    }
}
