package io.openk9.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SuggestionCategoryField {
	private Long suggestionCategoryFieldId;
	private Long tenantId;
	private Long categoryId;
	private String fieldName;
	private String name;
}