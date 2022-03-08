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
public class BundleInfoResponseDTO {
	private long id;
	private long lastModified;
	private String state;
	private String symbolicName;
	private String version;
}