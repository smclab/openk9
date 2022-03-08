package io.openk9.api.aggregator.client.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
@RegisterForReflection
public class SuggestionCategoryFieldRequestDTO {
	private Long tenantId;
	private Long categoryId;
	private String fieldName;
	private String name;
	private boolean enabled;
}