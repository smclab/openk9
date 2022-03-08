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
public class QueryAnalysisTokenDTO {
	private String text;
	private Integer start;
	private Integer end;
	private Map<String, Object> token;
}
