package io.openk9.datasource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SuggestionCategoryDto{
	private Long tenantId;
	private Long suggestionCategoryId;
	private Long parentCategoryId;
	private String name;
	private boolean enabled;
}
