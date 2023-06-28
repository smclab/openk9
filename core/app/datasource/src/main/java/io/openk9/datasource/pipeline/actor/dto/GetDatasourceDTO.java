package io.openk9.datasource.pipeline.actor.dto;

import lombok.Data;

import java.util.Collection;

@Data
public class GetDatasourceDTO {

	private Long id;
	private Collection<GetEnrichItemDTO> enrichItems;

}
