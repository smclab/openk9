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
}
