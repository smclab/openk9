package io.openk9.api.aggregator.client.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
@RegisterForReflection
public class SearchTokenDTO {
	private String entityType;
	private String tokenType;
	private String keywordKey;
	private String[] values;
	private Map<String, Object> extra;
	private Boolean filter;
}