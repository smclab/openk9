package io.openk9.datasource.pipeline.actor.dto;

import io.openk9.datasource.model.EnrichItem;
import lombok.Data;

@Data
public class EnrichItemDTO {
	private Long id;
	private String name;
	private String serviceName;
	private String script;
	private EnrichItem.EnrichItemType type;
	private String jsonConfig;
	private String jsonPath;
	private EnrichItem.BehaviorMergeType behaviorMergeType;
	private Long requestTimeout;
	private EnrichItem.BehaviorOnError behaviorOnError;
}
