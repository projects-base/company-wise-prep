import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shared input/output for every code challenge (docs/CODE-CHALLENGES.md).
 *
 * Input: one argument per line, written as JSON-like literals — 5, -3, 2.5, true, "abc",
 * [1,2,3], [[1,2],[3]], ["a","b"], [1,null,2] (trees). Output: IO.print(...) writes the same
 * canonical form, so expected outputs are plain text compared exactly.
 */
public final class IO {

	private final List<String> lines = new ArrayList<>();
	private int at;

	private IO(List<String> lines) {
		for (String l : lines) {
			if (!l.isBlank()) this.lines.add(l.strip());
		}
	}

	/** Reads all of standard input. */
	public static IO stdin() {
		try (BufferedReader r = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
			return new IO(r.lines().toList());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public boolean hasNext() {
		return at < lines.size();
	}

	public String nextLine() {
		if (!hasNext()) throw new IllegalStateException("Input has fewer lines than the harness expects");
		return lines.get(at++);
	}

	/** The next line parsed as a JSON-like value: Long, Double, String, Boolean, null or List. */
	public Object nextValue() {
		return new Parser(nextLine()).parse();
	}

	public int nextInt() { return (int) toLong(nextValue()); }
	public long nextLong() { return toLong(nextValue()); }
	public double nextDouble() { return ((Number) nextValue()).doubleValue(); }
	public boolean nextBool() { return (Boolean) nextValue(); }
	public String nextString() { return (String) nextValue(); }
	public char nextChar() { return ((String) nextValue()).charAt(0); }

	public int[] nextIntArray() { return toIntArray(nextValue()); }
	public long[] nextLongArray() {
		List<?> l = (List<?>) nextValue();
		long[] a = new long[l.size()];
		for (int i = 0; i < a.length; i++) a[i] = toLong(l.get(i));
		return a;
	}
	public double[] nextDoubleArray() {
		List<?> l = (List<?>) nextValue();
		double[] a = new double[l.size()];
		for (int i = 0; i < a.length; i++) a[i] = ((Number) l.get(i)).doubleValue();
		return a;
	}
	public String[] nextStringArray() {
		List<?> l = (List<?>) nextValue();
		return l.stream().map(x -> (String) x).toArray(String[]::new);
	}
	public char[] nextCharArray() {
		Object v = nextValue();
		if (v instanceof String s) return s.toCharArray();
		List<?> l = (List<?>) v;
		char[] a = new char[l.size()];
		for (int i = 0; i < a.length; i++) a[i] = ((String) l.get(i)).charAt(0);
		return a;
	}
	public int[][] nextIntMatrix() {
		List<?> rows = (List<?>) nextValue();
		int[][] m = new int[rows.size()][];
		for (int i = 0; i < m.length; i++) m[i] = toIntArray(rows.get(i));
		return m;
	}
	/** [["1","0"],["0","1"]] — LeetCode's char grids. */
	public char[][] nextCharMatrix() {
		List<?> rows = (List<?>) nextValue();
		char[][] m = new char[rows.size()][];
		for (int i = 0; i < m.length; i++) {
			List<?> r = (List<?>) rows.get(i);
			m[i] = new char[r.size()];
			for (int j = 0; j < r.size(); j++) m[i][j] = ((String) r.get(j)).charAt(0);
		}
		return m;
	}
	public String[][] nextStringMatrix() {
		List<?> rows = (List<?>) nextValue();
		String[][] m = new String[rows.size()][];
		for (int i = 0; i < m.length; i++) m[i] = ((List<?>) rows.get(i)).stream().map(x -> (String) x).toArray(String[]::new);
		return m;
	}
	public List<Integer> nextIntList() {
		List<Integer> out = new ArrayList<>();
		for (Object x : (List<?>) nextValue()) out.add((int) toLong(x));
		return out;
	}
	public List<String> nextStringList() {
		List<String> out = new ArrayList<>();
		for (Object x : (List<?>) nextValue()) out.add((String) x);
		return out;
	}
	public List<List<Integer>> nextIntListList() {
		List<List<Integer>> out = new ArrayList<>();
		for (Object row : (List<?>) nextValue()) {
			List<Integer> r = new ArrayList<>();
			for (Object x : (List<?>) row) r.add((int) toLong(x));
			out.add(r);
		}
		return out;
	}
	public List<List<String>> nextStringListList() {
		List<List<String>> out = new ArrayList<>();
		for (Object row : (List<?>) nextValue()) {
			List<String> r = new ArrayList<>();
			for (Object x : (List<?>) row) r.add((String) x);
			out.add(r);
		}
		return out;
	}
	/** The raw parsed list, for design problems: ["LRUCache","put","get"] then [[2],[1,1],[1]]. */
	public List<?> nextList() { return (List<?>) nextValue(); }

	/** Level order with nulls, LeetCode style: [3,9,20,null,null,15,7]. */
	public TreeNode nextTree() {
		List<?> l = (List<?>) nextValue();
		if (l.isEmpty() || l.get(0) == null) return null;
		TreeNode root = new TreeNode((int) toLong(l.get(0)));
		Deque<TreeNode> q = new ArrayDeque<>();
		q.add(root);
		int i = 1;
		while (!q.isEmpty() && i < l.size()) {
			TreeNode n = q.poll();
			if (i < l.size() && l.get(i) != null) q.add(n.left = new TreeNode((int) toLong(l.get(i))));
			i++;
			if (i < l.size() && l.get(i) != null) q.add(n.right = new TreeNode((int) toLong(l.get(i))));
			i++;
		}
		return root;
	}

	public ListNode nextLinkedList() {
		ListNode dummy = new ListNode(0), t = dummy;
		for (Object x : (List<?>) nextValue()) t = t.next = new ListNode((int) toLong(x));
		return dummy.next;
	}

	// ── output ───────────────────────────────────────────────────────────

	/** Prints a value in the canonical form expected outputs are written in. */
	public static void print(Object value) {
		System.out.println(format(value));
	}

	public static String format(Object v) {
		StringBuilder sb = new StringBuilder();
		write(sb, v);
		return sb.toString();
	}

	private static void write(StringBuilder sb, Object v) {
		if (v == null) sb.append("null");
		else if (v instanceof String s) quote(sb, s);
		else if (v instanceof Character c) quote(sb, String.valueOf(c));
		else if (v instanceof Double || v instanceof Float) sb.append(String.format(Locale.ROOT, "%.5f", ((Number) v).doubleValue()));
		else if (v instanceof Number || v instanceof Boolean) sb.append(v);
		else if (v instanceof int[] a) { sb.append('['); for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(','); sb.append(a[i]); } sb.append(']'); }
		else if (v instanceof long[] a) { sb.append('['); for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(','); sb.append(a[i]); } sb.append(']'); }
		else if (v instanceof double[] a) { sb.append('['); for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(','); write(sb, a[i]); } sb.append(']'); }
		else if (v instanceof boolean[] a) { sb.append('['); for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(','); sb.append(a[i]); } sb.append(']'); }
		else if (v instanceof char[] a) { sb.append('['); for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(','); quote(sb, String.valueOf(a[i])); } sb.append(']'); }
		else if (v instanceof Object[] a) { sb.append('['); for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(','); write(sb, a[i]); } sb.append(']'); }
		else if (v instanceof Collection<?> c) { sb.append('['); boolean first = true; for (Object x : c) { if (!first) sb.append(','); first = false; write(sb, x); } sb.append(']'); }
		else if (v instanceof Map<?, ?> m) { sb.append('{'); boolean first = true; for (var e : m.entrySet()) { if (!first) sb.append(','); first = false; write(sb, String.valueOf(e.getKey())); sb.append(':'); write(sb, e.getValue()); } sb.append('}'); }
		else if (v instanceof TreeNode t) write(sb, treeToList(t));
		else if (v instanceof ListNode n) { List<Integer> l = new ArrayList<>(); for (ListNode x = n; x != null; x = x.next) l.add(x.val); write(sb, l); }
		else sb.append(v);
	}

	private static List<Integer> treeToList(TreeNode root) {
		List<Integer> out = new ArrayList<>();
		Deque<TreeNode> q = new ArrayDeque<>();
		q.add(root);
		List<TreeNode> order = new ArrayList<>();
		while (!q.isEmpty()) {
			TreeNode n = q.poll();
			order.add(n);
			if (n != null) { q.add(n.left); q.add(n.right); }
		}
		for (TreeNode n : order) out.add(n == null ? null : n.val);
		while (!out.isEmpty() && out.get(out.size() - 1) == null) out.remove(out.size() - 1);
		return out;
	}

	private static void quote(StringBuilder sb, String s) {
		sb.append('"');
		for (char c : s.toCharArray()) {
			if (c == '"' || c == '\\') sb.append('\\').append(c);
			else if (c == '\n') sb.append("\\n");
			else sb.append(c);
		}
		sb.append('"');
	}

	private static long toLong(Object o) {
		if (o instanceof Number n) return n.longValue();
		throw new IllegalArgumentException("Expected a number but got " + o);
	}

	private static int[] toIntArray(Object v) {
		List<?> l = (List<?>) v;
		int[] a = new int[l.size()];
		for (int i = 0; i < a.length; i++) a[i] = (int) toLong(l.get(i));
		return a;
	}

	/** A tiny JSON-ish parser: numbers, strings, true/false/null and nested lists. */
	private static final class Parser {
		private final String s;
		private int i;

		Parser(String s) {
			this.s = s;
		}

		Object parse() {
			Object v = value();
			ws();
			if (i != s.length()) throw new IllegalArgumentException("Unexpected text after value: " + s.substring(i));
			return v;
		}

		private Object value() {
			ws();
			if (i >= s.length()) throw new IllegalArgumentException("Empty value");
			char c = s.charAt(i);
			if (c == '[') return list();
			if (c == '"') return string();
			if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
			if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
			if (s.startsWith("null", i)) { i += 4; return null; }
			return number();
		}

		private List<Object> list() {
			List<Object> out = new ArrayList<>();
			i++;
			ws();
			if (s.charAt(i) == ']') { i++; return out; }
			while (true) {
				out.add(value());
				ws();
				char c = s.charAt(i++);
				if (c == ']') return out;
				if (c != ',') throw new IllegalArgumentException("Expected , or ] at " + (i - 1) + " in " + s);
			}
		}

		private String string() {
			StringBuilder sb = new StringBuilder();
			i++;
			while (s.charAt(i) != '"') {
				char c = s.charAt(i++);
				if (c == '\\') {
					char e = s.charAt(i++);
					sb.append(e == 'n' ? '\n' : e == 't' ? '\t' : e);
				} else sb.append(c);
			}
			i++;
			return sb.toString();
		}

		private Number number() {
			int start = i;
			while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
			String t = s.substring(start, i);
			if (t.isEmpty()) throw new IllegalArgumentException("Cannot read a value at: " + s.substring(start));
			return t.contains(".") || t.contains("e") || t.contains("E") ? (Number) Double.parseDouble(t) : (Number) Long.parseLong(t);
		}

		private void ws() {
			while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
		}
	}
}

/** LeetCode's binary tree node. */
class TreeNode {
	int val;
	TreeNode left, right;

	TreeNode() {
	}

	TreeNode(int val) {
		this.val = val;
	}

	TreeNode(int val, TreeNode left, TreeNode right) {
		this.val = val;
		this.left = left;
		this.right = right;
	}
}

/** LeetCode's singly linked list node. */
class ListNode {
	int val;
	ListNode next;

	ListNode() {
	}

	ListNode(int val) {
		this.val = val;
	}

	ListNode(int val, ListNode next) {
		this.val = val;
		this.next = next;
	}
}
