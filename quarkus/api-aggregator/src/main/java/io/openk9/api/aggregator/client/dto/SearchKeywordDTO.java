package io.openk9.api.aggregator.client.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
@Builder
@RegisterForReflection
public class SearchKeywordDTO {
	private String keyword;
	private boolean text;
	private FieldBoostDTO fieldBoost;
}
