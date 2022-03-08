package io.openk9.api.aggregator.client.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
@RegisterForReflection
public class SearchRequestDTO {
	private List<SearchTokenDTO> searchQuery;
	private int[] range;
	private String afterKey;
	private String suggestKeyword;
	private Long suggestionCategoryId;
}
