import java.util.*;

class Solution {
    public boolean areSentencesSimilar(String sentence1, String sentence2) {
        String[] a = sentence1.split(" "), b = sentence2.split(" ");
        if (a.length < b.length) { String[] t = a; a = b; b = t; }
        int i = 0, j = 0;
        while (i < b.length && a[i].equals(b[i])) i++;
        while (j < b.length - i && a[a.length - 1 - j].equals(b[b.length - 1 - j])) j++;
        return i + j >= b.length;
    }
}
