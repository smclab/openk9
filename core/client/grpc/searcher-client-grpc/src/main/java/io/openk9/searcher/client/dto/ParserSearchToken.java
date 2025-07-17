package io.openk9.searcher.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParserSearchToken {
	@Schema(description = "Used for specify entityType to search in case of tokenType ENTITY")
	private String entityType;
	@Schema(description = "Used for specify entityName to search in case of tokenType ENTITY")
	private String entityName;
	@Schema(description = "Token Type to specify type of ParserSearchToken")
	private String tokenType;
	@Schema(description = "Used to specify specific keyword field to perform search")
	private String keywordKey;
	@Schema(description = "List of strings used to perform search. In case of multiple strings, search logic (MUST/SHOULD/...) depends on Openk9 search config")
	private List<String> values;
	@Schema(description = "Used to specify extra configurations to overwrite default configurations")
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
