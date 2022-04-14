package io.openk9.ingestion.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@RegisterForReflection
public  class IngestionPayload {
	private String ingestionId;
	private long datasourceId;
	private String contentId;
	private long parsingDate;
	private String rawContent;
	private Map<String, Object> datasourcePayload;
	private long tenantId;
	private String[] documentTypes;
	private ResourcesPayload resources;
}