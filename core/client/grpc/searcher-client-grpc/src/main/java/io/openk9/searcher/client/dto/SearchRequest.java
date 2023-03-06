package io.openk9.searcher.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {
	private List<ParserSearchToken> searchQuery;
	private List<Integer> range;
	private String afterKey;
	private String suggestKeyword;
	private Long suggestionCategoryId;
	private String order = "asc";
	private List<Map<String, Map<String, String>>> sort;
}