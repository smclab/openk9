package io.openk9.datasource.pipeline.actor.dto;

import lombok.Data;

import java.util.Set;

@Data
public class GetDatasourceDTO {

	private Long id;
	private String dataIndexName;
	private Set<GetEnrichItemDTO> enrichItems;

}