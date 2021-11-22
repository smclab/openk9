package io.openk9.plugin.driver.manager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
@Builder
public class SearchKeywordDTO {
	private String keyword;
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

	public enum Type {
		DATE,TEXT,NUMBER
	}
}
