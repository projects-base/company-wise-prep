import java.util.*;

class Solution {
    public String ftoa(double x, int precision) {
        boolean negative = x < 0;
        double a = Math.abs(x);
        long scale = 1;
        for (int i = 0; i < precision; i++) scale *= 10;
        // Round half away from zero on the magnitude, as a whole number of 10^-precision units.
        long units = (long) Math.floor(a * scale + 0.5);
        long intPart = units / scale, fracPart = units % scale;

        StringBuilder sb = new StringBuilder();
        if (negative && units != 0) sb.append('-');
        appendDigits(sb, intPart);
        if (precision > 0) {
            sb.append('.');
            char[] frac = new char[precision];
            for (int i = precision - 1; i >= 0; i--) {
                frac[i] = (char) ('0' + fracPart % 10);
                fracPart /= 10;
            }
            sb.append(frac);
        }
        return sb.toString();
    }

    private void appendDigits(StringBuilder sb, long v) {
        if (v == 0) {
            sb.append('0');
            return;
        }
        char[] buf = new char[20];
        int i = buf.length;
        while (v > 0) {
            buf[--i] = (char) ('0' + v % 10);
            v /= 10;
        }
        sb.append(buf, i, buf.length - i);
    }
}
