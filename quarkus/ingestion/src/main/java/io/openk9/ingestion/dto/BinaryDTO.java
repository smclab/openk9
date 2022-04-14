package io.openk9.ingestion.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@RegisterForReflection
public class BinaryDTO {
	private String id;
	private String name;
	private String contentType;
	private String data;
}