package io.openk9.datasource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SuggestionCategoryFieldDto{
	private Long tenantId;
	private Long categoryId;
	private String fieldName;
	private String name;
	private boolean enabled;
}
