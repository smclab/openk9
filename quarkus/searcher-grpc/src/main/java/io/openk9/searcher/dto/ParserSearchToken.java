package io.openk9.searcher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParserSearchToken {
	private String entityType;
	private String entityName;
	private String tokenType;
	private String keywordKey;
	private List<String> values;
	private Map<String, String> extra;
	private boolean filter;

	public static ParserSearchToken ofText(String value) {
		return ofText(List.of(value), null, false);
	}

	public static ParserSearchToken ofText(List<String> values) {
		return ofText(values, null, false);
	}

	public static ParserSearchToken ofText(List<String> values, String keywordKey) {
		return ofText(values, keywordKey, false);
	}

	public static ParserSearchToken ofText(
		List<String> values, String keywordKey, boolean filter) {

		return new ParserSearchToken(
			null, null, TEXT, keywordKey, values, Map.of(), filter);
	}

	public static final String TEXT = "TEXT";

}
