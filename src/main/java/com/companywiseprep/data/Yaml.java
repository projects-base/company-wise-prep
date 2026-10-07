package com.companywiseprep.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Safe YAML loading for data/ files.
 *
 * SnakeYAML turns {@code 2026-10-07} into a java.util.Date and {@code 2026} into an Integer.
 * Everything here is display data, so dates come back as ISO strings; numbers (counts) stay numeric.
 */
public final class Yaml {

	private Yaml() {
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> load(String text) {
		Object root = new org.yaml.snakeyaml.Yaml(new SafeConstructor(new LoaderOptions())).load(text);
		return root == null ? new LinkedHashMap<>() : (Map<String, Object>) normalise(root);
	}

	public static String str(Object value) {
		return value == null ? null : String.valueOf(value).trim();
	}

	public static List<String> strings(Object value) {
		List<String> out = new ArrayList<>();
		if (value instanceof Collection<?> c) {
			c.forEach(v -> {
				if (v != null) out.add(String.valueOf(v).trim());
			});
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> map(Object value) {
		return value instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
	}

	public static List<Map<String, Object>> maps(Object value) {
		List<Map<String, Object>> out = new ArrayList<>();
		if (value instanceof Collection<?> c) {
			c.forEach(v -> out.add(map(v)));
		}
		return out;
	}

	private static Object normalise(Object value) {
		if (value instanceof Map<?, ?> m) {
			Map<String, Object> out = new LinkedHashMap<>();
			m.forEach((k, v) -> out.put(String.valueOf(k), normalise(v)));
			return out;
		}
		if (value instanceof Collection<?> c) {
			List<Object> out = new ArrayList<>();
			c.forEach(v -> out.add(normalise(v)));
			return out;
		}
		if (value instanceof Date d) {
			SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd");
			iso.setTimeZone(TimeZone.getTimeZone("UTC"));
			return iso.format(d);
		}
		return value;
	}
}
