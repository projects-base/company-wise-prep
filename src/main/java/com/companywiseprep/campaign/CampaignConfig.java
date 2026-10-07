package com.companywiseprep.campaign;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import com.companywiseprep.data.Yaml;

/** The parsed form of data/campaigns/<id>/campaign.yaml. */
public record CampaignConfig(
		String id,
		String name,
		String role,
		Map<String, Double> companies,
		LocalDate start,
		LocalDate interview,
		int weekdayMinutes,
		int weekendMinutes,
		int reviewDays,
		String notes) {

	public static CampaignConfig from(Campaign c) {
		Map<String, Object> y = Yaml.load(c.getYaml());
		Map<String, Double> weights = new LinkedHashMap<>();
		Object companies = y.get("companies");
		if (companies instanceof Map<?, ?> m) {
			m.forEach((k, v) -> weights.put(String.valueOf(k), v instanceof Number n ? n.doubleValue() : 1.0));
		} else {
			Yaml.strings(companies).forEach(s -> weights.put(s, 1.0));
		}
		Map<String, Object> minutes = Yaml.map(y.get("minutes"));
		return new CampaignConfig(
				c.getId(),
				c.getName(),
				Yaml.str(y.get("role")),
				weights,
				LocalDate.parse(Yaml.str(y.get("start"))),
				LocalDate.parse(Yaml.str(y.get("interview"))),
				intOr(minutes.get("weekday"), 25),
				intOr(minutes.get("weekend"), 40),
				intOr(y.get("review_days"), 3),
				Yaml.str(y.get("notes")));
	}

	private static int intOr(Object v, int fallback) {
		return v instanceof Number n ? n.intValue() : fallback;
	}
}
