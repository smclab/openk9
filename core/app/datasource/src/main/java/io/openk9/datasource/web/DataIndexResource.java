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

import io.openk9.datasource.index.mappings.MappingsKey;
import io.openk9.datasource.index.mappings.MappingsUtil;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.processor.indexwriter.IndexerEvents;
import io.openk9.datasource.service.DocTypeService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.hibernate.reactive.mutiny.Mutiny;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.PutComposableIndexTemplateRequest;
import org.opensearch.cluster.metadata.ComposableIndexTemplate;
import org.opensearch.cluster.metadata.Template;
import org.opensearch.common.compress.CompressedXContent;
import org.opensearch.common.settings.Settings;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@CircuitBreaker
@Path("/v1/data-index")
@RolesAllowed("k9-admin")
public class DataIndexResource {

	private static final String DETAILS_FIELD = "details";
	@Inject
	RestHighLevelClient restHighLevelClient;
	@Inject
	Mutiny.SessionFactory sessionFactory;
	@Inject
	IndexerEvents indexerEvents;
	@Inject
	DocTypeService docTypeService;

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
						session,
						dataIndex));

		});

	}

	@Path("/get-mappings-from-doc-types")
	@POST
	public Uni<Map<MappingsKey, Object>> getMappings(
		GetMappingsOrSettingsFromDocTypesRequest request) {

		return getMappingsFromDocTypes(request.getDocTypeIds());

	}

	@Path("/get-settings-from-doc-types")
	@POST
	public Uni<Map<String, Object>> getSettings(
		GetMappingsOrSettingsFromDocTypesRequest request) {

		return getSettingsFromDocTypes(request.getDocTypeIds());

	}

	private Uni<Map<MappingsKey, Object>> getMappingsFromDocTypes(
		List<Long> docTypeIds) {

		return sessionFactory.withTransaction(session -> {

			Uni<List<DocType>> docTypeListUni =
				_findDocTypes(docTypeIds, session, false);

			return docTypeListUni.map(MappingsUtil::docTypesToMappings);

		});
	}

	private Uni<Map<String, Object>> getSettingsFromDocTypes(
		List<Long> docTypeIds) {

		return sessionFactory.withTransaction(session -> {

			Uni<List<DocType>> docTypeListUni =
				_findDocTypes(docTypeIds, session, true);

			return docTypeListUni.map(MappingsUtil::docTypesToSettings);

		});
	}

	private Uni<List<DocType>> _findDocTypes(
		List<Long> docTypeIds, Mutiny.Session session, boolean includeAnalyzerSubtypes) {

		return docTypeService
			.getDocTypesAndDocTypeFields(session, docTypeIds)
			.map(LinkedList::new);
	}

	@Path("/create-data-index-from-doc-types/{datasourceId}")
	@POST
	public Uni<DataIndex> createDataIndexFromDocTypes(
		@PathParam("datasourceId") long datasourceId,
		CreateDataIndexFromDocTypesRequest request) {

		String dataIndexName;

		if (request.getIndexName() == null) {
			dataIndexName = "data-" + OffsetDateTime.now();
		}
		else {
			dataIndexName = request.getIndexName();
		}

		return sessionFactory.withTransaction(s -> {

			List<Long> docTypeIds = request.getDocTypeIds();

			Uni<List<DocType>> docTypeListUni =
				_findDocTypes(docTypeIds, s, true);

			return docTypeListUni
				.onItem()
				.transformToUni(Unchecked.function(docTypeList -> {

					if (docTypeList.size() != docTypeIds.size()) {
						throw new RuntimeException(
							"docTypeIds found: " + docTypeList.size() +
							" docTypeIds requested: " + docTypeIds.size());
					}

					DataIndex dataIndex = new DataIndex();

					dataIndex.setDescription("auto-generated");

					dataIndex.setName(dataIndexName);

					dataIndex.setDocTypes(new LinkedHashSet<>(docTypeList));

					dataIndex.setDatasource(s.getReference(Datasource.class, datasourceId));

					return s.persist(dataIndex)
						.invoke(s::flush)
						.invoke(() -> s.detach(dataIndex))
						.flatMap(__ -> s.find(DataIndex.class, dataIndex.getId()))
						.map(persisted -> {

							Map<MappingsKey, Object> mappings =
								MappingsUtil.docTypesToMappings(persisted.getDocTypes());

							Settings settings;

							Map<String, Object> settingsMap = null;

							Map<String, Object> requestSettings = request.getSettings();

							if (requestSettings != null && !requestSettings.isEmpty()) {
								settingsMap = requestSettings;
							}
							else {
								settingsMap =
									MappingsUtil.docTypesToSettings(persisted.getDocTypes());
							}

							if (settingsMap.isEmpty()) {
								settings = Settings.EMPTY;
							}
							else {
								settings = Settings.builder()
									.loadFromMap(settingsMap)
									.build();
							}

							PutComposableIndexTemplateRequest
								putComposableIndexTemplateRequest =
								new PutComposableIndexTemplateRequest();

							ComposableIndexTemplate composableIndexTemplate = null;

							try {
								composableIndexTemplate = new ComposableIndexTemplate(
									List.of(persisted.getIndexName()),
									new Template(settings, new CompressedXContent(
										Json.encode(mappings)), null),
									null, null, null, null
								);
							}
							catch (IOException e) {
								throw new WebApplicationException(Response
									.status(Response.Status.INTERNAL_SERVER_ERROR)
									.entity(JsonObject.of(
										DETAILS_FIELD, "failed creating IndexTemplate"
									))
									.build());
							}

							putComposableIndexTemplateRequest
								.name(persisted.getIndexName() + "-template")
								.indexTemplate(composableIndexTemplate);

							return putComposableIndexTemplateRequest;
						})
						.call((req) -> Uni.createFrom().emitter((sink) -> {

							try {
								IndicesClient indices = restHighLevelClient.indices();

								indices.putIndexTemplate(req, RequestOptions.DEFAULT);

								sink.complete(null);
							}
							catch (OpenSearchStatusException e) {
								sink.fail(new WebApplicationException(Response
									.status(e.status().getStatus())
									.entity(JsonObject.of(
										DETAILS_FIELD, e.getMessage()))
									.build()));
							}
							catch (Exception e) {
								sink.fail(new WebApplicationException(Response
									.status(Response.Status.INTERNAL_SERVER_ERROR)
									.entity(JsonObject.of(
										DETAILS_FIELD, e.getMessage()))
									.build()));
							}

						}))
						.map(__ -> dataIndex);

				}));

		});

	}

}
