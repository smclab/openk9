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
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.openk9.datasource.index.mappings.MappingsKey;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.processor.indexwriter.IndexerEvents;
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.DocTypeService;

import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.hibernate.reactive.mutiny.Mutiny;

@CircuitBreaker
@Path("/v1/data-index")
@RolesAllowed("k9-admin")
public class DataIndexResource {

	@Inject
	Mutiny.SessionFactory sessionFactory;
	@Inject
	IndexerEvents indexerEvents;
	@Inject
	DocTypeService docTypeService;
	@Inject
	DataIndexService dataIndexService;

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

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CreateDataIndexFromDocTypesRequest {
		private List<Long> docTypeIds;
		private String indexName;
		private Map<String, Object> settings;

	}

	@Path("/auto-generate-doc-types")
	@POST
	public Uni<Void> autoGenerateDocTypes(
		AutoGenerateDocTypesRequest request) {

		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<DataIndex> query = cb.createQuery(DataIndex.class);

			Root<Datasource> from = query.from(Datasource.class);

			query.select(from.get(Datasource_.dataIndex));

			query.where(from.get(Datasource_.id).in(request.getDatasourceId()));

			return session
				.createQuery(query)
				.getSingleResult()
				.onItem()
				.transformToUni(
					dataIndex -> indexerEvents.generateDocTypeFields(
						session, dataIndex));

		});

	}

	@Path("/get-mappings-from-doc-types")
	@POST
	public Uni<Map<MappingsKey, Object>> getMappings(
		GetMappingsOrSettingsFromDocTypesRequest request) {

		return docTypeService.getMappingsFromDocTypes(request.getDocTypeIds());
	}

	@Path("/get-settings-from-doc-types")
	@POST
	public Uni<Map<String, Object>> getSettings(
		GetMappingsOrSettingsFromDocTypesRequest request) {

		return docTypeService.getSettingsFromDocTypes(request.getDocTypeIds());
	}

	@Path("/create-data-index-from-doc-types/{datasourceId}")
	@POST
	public Uni<DataIndex> createDataIndexFromDocTypes(
		@PathParam("datasourceId") long datasourceId,
		CreateDataIndexFromDocTypesRequest request) {

		return dataIndexService.createDataIndexFromDocTypes(
			datasourceId,
			request.getDocTypeIds(),
			request.getIndexName(),
			request.getSettings()
		);

	}

}
