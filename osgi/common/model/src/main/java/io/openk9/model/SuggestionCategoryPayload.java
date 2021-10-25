package io.openk9.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SuggestionCategoryPayload {
	private Long suggestionCategoryId;
	private Long tenantId;
	private Long parentCategoryId;
	private String name;
	private List<SuggestionCategoryField> suggestionCategoryFields;
}