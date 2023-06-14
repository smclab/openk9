package io.openk9.datasource.pipeline.actor.dto;

import lombok.Data;

@Data
public class SchedulerDTO {
	private String scheduleId;
	private String datasourceId;
	private String oldDataIndexName;
	private String newDataIndexName;
	private String status;
}
