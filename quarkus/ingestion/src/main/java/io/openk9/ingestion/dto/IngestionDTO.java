package io.openk9.ingestion.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@RegisterForReflection
public class IngestionDTO {
	private long datasourceId;
	private String contentId;
	private long parsingDate;
	private String rawContent;
	private Map<String, Object> datasourcePayload;
	private ResourcesDTO resources;
	private Map<String, List<String>> acl;
}