/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.web;

import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.openk9.datasource.index.IndexMappingService;
import io.openk9.datasource.index.model.MappingsKey;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.dto.base.DataIndexDTO;
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.service.util.K9EntityServiceException;
import io.openk9.datasource.web.dto.DataIndexByDocTypes;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

@CircuitBreaker
@Path("/v1/data-index")
@RolesAllowed("k9-admin")
public class DataIndexResource {

	@Inject
	DocTypeService docTypeService;
	@Inject
	DataIndexService dataIndexService;
	@Inject
	IndexMappingService indexMappingService;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AutoGenerateDocTypesRequest {
		private long datasourceId;

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class GetMappingsOrSettingsFromDocTypesRequest {
		private List<Long> docTypeIds;

	}

	@Path("/auto-generate-doc-types")
	@POST
	public Uni<Void> autoGenerateDocTypes(
		AutoGenerateDocTypesRequest request) {

		return dataIndexService.autoGenerateDocTypes(request);

	}

	@Path("/get-mappings-from-doc-types")
	@POST
	public Uni<Map<MappingsKey, Object>> getMappings(
		GetMappingsOrSettingsFromDocTypesRequest request) {

		return indexMappingService.getMappingsFromDocTypes(request.getDocTypeIds());
	}

	@Path("/get-settings-from-doc-types")
	@POST
	public Uni<Map<String, Object>> getSettings(
		GetMappingsOrSettingsFromDocTypesRequest request) {

		return indexMappingService.getSettingsFromDocTypes(request.getDocTypeIds());
	}

	@Path("/create-data-index-from-doc-types/{datasourceId}")
	@POST
	@Deprecated(forRemoval = true)
	public Uni<DataIndex> createDataIndexFromDocTypes(
		@PathParam("datasourceId") long datasourceId,
		DataIndexByDocTypes request) {

		var settings = JsonObject.mapFrom(request.getSettings()).encode();

		DataIndexDTO dataIndexDto = DataIndexDTO.builder()
			.name(request.getIndexName())
			.docTypeIds(Set.copyOf(request.getDocTypeIds()))
			.settings(settings)
			.build();

		return dataIndexService.create(datasourceId, dataIndexDto)
			.map(response -> {
				if (response.getFieldValidators() != null
					&& !response.getFieldValidators().isEmpty()) {

					throw new K9EntityServiceException("An error occurred while creating dataIndex");
				}
				else {
					return response.getEntity();
				}
			});
	}

}
