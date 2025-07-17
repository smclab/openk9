package io.openk9.searcher.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {
	@Schema(description = "List of ParserSearchToken to compose search query.")
	private List<ParserSearchToken> searchQuery;
	@Schema(description = "List of integer for pagination where first element is start element and second element is page size")
	private List<Integer> range;
	@Schema(description = "After key used for pagination in suggestions endpoint")
	private String afterKey;
	@Schema(description = "Keyword used to filter options in suggestions endpoint")
	private String suggestKeyword;
	@Schema(format = "uuid",
			description = "Unique string that identifies the Suggestion Category to return suggestions")
	private Long suggestionCategoryId;
	@Schema(description = "How to order suggestions")
	private String order = "asc";
	@Schema(description = "List of objects to define sort logic")
	private List<Map<String, Map<String, String>>> sort;
	@Schema(description = "After key used for pagination when sort is present")
	private String sortAfterKey;
	@Schema(description = "Language used to apply search in specific language.")
	private String language;
}