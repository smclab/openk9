package io.openk9.plugin.driver.manager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(staticName = "of")
@Builder
public class SearchKeywordDTO {
	@EqualsAndHashCode.Include
	private String keyword;
	@EqualsAndHashCode.Include
	private Type type;
	private FieldBoostDTO fieldBoost;

	public boolean isText() {
		return type == Type.TEXT;
	}

	public boolean isDate() {
		return type == Type.DATE;
	}

	public boolean isNumber() {
		return type == Type.NUMBER;
	}

	public boolean isAutocomplete() {
		return type == Type.AUTOCOMPLETE;
	}

	public enum Type {
		DATE,TEXT,NUMBER,AUTOCOMPLETE
	}
}
