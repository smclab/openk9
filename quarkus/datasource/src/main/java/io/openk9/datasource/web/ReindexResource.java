package io.openk9.datasource.web;

import io.openk9.datasource.dto.ReindexRequestDto;
import io.openk9.datasource.dto.ReindexResponseDto;
import io.openk9.datasource.model.Datasource;

import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Path("/v1/index")
public class ReindexResource {

	@POST
	@Path("/reindex")
	@Transactional
	public List<ReindexResponseDto> reindex(ReindexRequestDto dto) {

		List<Datasource> datasourceList =
			Datasource.list("datasourceId in ?1", dto.getDatasourceIds());

		List<ReindexResponseDto> response = new ArrayList<>();

		for (Datasource datasource : datasourceList) {
			datasource.setLastIngestionDate(Instant.EPOCH);
			datasource.persistAndFlush();
			response.add(
				ReindexResponseDto.of(
					datasource.getDatasourceId(),
					true
				)
			);
		}

		return response;

	}

}
