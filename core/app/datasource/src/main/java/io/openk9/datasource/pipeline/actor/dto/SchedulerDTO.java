package io.openk9.datasource.pipeline.actor.dto;

import io.openk9.datasource.model.Scheduler;
import lombok.Data;

import java.util.Set;

@Data
public class SchedulerDTO {
	private Long id;
	private String scheduleId;
	private Long datasourceId;
	private Set<EnrichItemDTO> enrichItems;
	private Long oldDataIndexId;
	private String oldDataIndexName;
	private Long newDataIndexId;
	private String newDataIndexName;
	private Scheduler.SchedulerStatus status;
}
