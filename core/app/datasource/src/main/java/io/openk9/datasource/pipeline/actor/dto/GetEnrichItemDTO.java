package io.openk9.datasource.pipeline.actor.dto;

import lombok.Data;

@Data
public class GetEnrichItemDTO {
	private Long id;
	private String name;
	private String serviceName;
	private String script;
	private String type;
	private String jsonConfig;
	private String jsonPath;
	private String behaviorMergeType;
	private Long requestTimeout;
	private String behaviorOnError;
}
