package io.openk9.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SuggestionCategory {
	private Long suggestionCategoryId;
	private Long tenantId;
	private Long parentCategoryId;
	private String name;
}