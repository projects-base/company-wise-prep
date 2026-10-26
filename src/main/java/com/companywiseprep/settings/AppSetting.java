package com.companywiseprep.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One setting saved from the Settings page. API keys are stored encrypted (see SettingsService). */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class AppSetting {

	@Id
	private String name;

	// "value" is a reserved word in H2.
	@Column(name = "setting_value", length = 4_000)
	private String value;

	public AppSetting(String name, String value) {
		this.name = name;
		this.value = value;
	}
}
