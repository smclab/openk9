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

package io.openk9.datasource.service;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.index.IndexService;
import io.openk9.datasource.index.mappings.MappingsKey;
import io.openk9.datasource.index.mappings.MappingsUtil;
import io.openk9.datasource.mapper.DataIndexMapper;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.UnknownTenantException;
import io.openk9.datasource.model.VectorIndex;
import io.openk9.datasource.model.dto.DataIndexDTO;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.openk9.datasource.processor.indexwriter.IndexerEvents;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.util.OpenSearchUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.support.IndicesOptions;
import org.opensearch.action.support.master.AcknowledgedResponse;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class DataIndexService
	extends BaseK9EntityService<DataIndex, DataIndexDTO> {

	@Inject
	DocTypeService docTypeService;
	@Inject
	IndexService indexService;
	@Inject
	RestHighLevelClient restHighLevelClient;
	@Inject
	HttpPluginDriverClient pluginDriverClient;
	@Inject
	IndexerEvents indexerEvents;
	@Inject
	IngestionPayloadMapper ingestionPayloadMapper;

	private static final String DETAILS_FIELD = "details";

	DataIndexService(DataIndexMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{DataIndex_.NAME, DataIndex_.DESCRIPTION};
	}

	public Uni<DataIndex> findByIdWithVectorIndex(Mutiny.Session session, long dataIndexId) {

		return session.createQuery(
				"from DataIndex di join fetch di.vectorIndex where di.id = :dataIndexId",
				DataIndex.class
			)
			.setParameter("dataIndexId", dataIndexId)
			.getSingleResultOrNull();

	}

	public Uni<DataIndex> bindVectorDataIndex(long dataIndexId, long vectorIndexId) {

		return sessionFactory.withTransaction((s, t) ->
			bindVectorDataIndex(s, dataIndexId, vectorIndexId));
	}

	public Uni<DataIndex> bindVectorDataIndex(
		Mutiny.Session session, long dataIndexId, long vectorIndexId) {

		return session.find(DataIndex.class, dataIndexId)
			.flatMap(dataIndex -> session
				.find(VectorIndex.class, vectorIndexId)
				.flatMap(vectorIndex -> {
					dataIndex.setVectorIndex(vectorIndex);
					return session.persist(dataIndex)
						.map(unused -> dataIndex);
				})
			);

	}

	public Uni<DataIndex> unbindVectorDataIndex(long dataIndexId) {

		return sessionFactory.withTransaction((s, t) ->
			unbindVectorDataIndex(s, dataIndexId));

	}

	public Uni<DataIndex> unbindVectorDataIndex(
		Mutiny.Session session, long dataIndexId) {

		return session.find(DataIndex.class, dataIndexId)
			.flatMap(dataIndex -> {
					dataIndex.setVectorIndex(null);
					return session.persist(dataIndex)
						.map(unused -> dataIndex);
				}
			);

	}

	public Uni<Set<DocType>> getDocTypes(DataIndex dataIndex) {
		return sessionFactory.withTransaction(s -> s.fetch(dataIndex.getDocTypes()));
	}

	public Uni<Page<DocType>> getDocTypes(
		long dataIndexId, Pageable pageable) {
		return getDocTypes(dataIndexId, pageable, Filter.DEFAULT);
	}

	public Uni<Connection<DocType>> getDocTypesConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean not) {
		return findJoinConnection(
			id, DataIndex_.DOC_TYPES, DocType.class,
			docTypeService.getSearchFields(),
			after, before, first, last, searchText, sortByList, not
		);
	}

	public Uni<Long> getCountIndexDocuments(String name) {
		return indexService.indexCount(name);
	}

	public Uni<Page<DocType>> getDocTypes(
		long dataIndexId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[]{dataIndexId}, DataIndex_.DOC_TYPES, DocType.class,
			pageable.getLimit(),
			pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), searchText
		);
	}

	public Uni<Page<DocType>> getDocTypes(
		long dataIndexId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[]{dataIndexId}, DataIndex_.DOC_TYPES, DocType.class,
			pageable.getLimit(),
			pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), filter
		);
	}

	public Uni<Tuple2<DataIndex, DocType>> addDocType(
		long dataIndexId, long docTypeId) {
		return sessionFactory.withTransaction((s) -> findById(s, dataIndexId)
			.onItem()
			.ifNotNull()
			.transformToUni(dataIndex ->
				docTypeService.findById(s, docTypeId)
					.onItem()
					.ifNotNull()
					.transformToUni(
						docType -> s.fetch(dataIndex.getDocTypes())
							.flatMap(dts -> {
								if (dts.add(docType)) {
									dataIndex.setDocTypes(dts);
									return create(s, dataIndex)
										.map(di -> Tuple2.of(di, docType));
								}
								return Uni.createFrom().nullItem();
							})
					)
			));
	}

	public Uni<Tuple2<DataIndex, DocType>> removeDocType(
		long dataIndexId, long docTypeId) {
		return sessionFactory.withTransaction((s) -> findById(s, dataIndexId)
			.onItem()
			.ifNotNull()
			.transformToUni(dataIndex ->
				docTypeService.findById(s, docTypeId)
					.onItem()
					.ifNotNull()
					.transformToUni(
						docType -> s.fetch(dataIndex.getDocTypes())
							.flatMap(dts -> {
								if (dts.remove(docType)) {
									dataIndex.setDocTypes(dts);
									return create(s, dataIndex)
										.map(di -> Tuple2.of(di, docType));
								}
								return Uni.createFrom().nullItem();
							})
					)
			));
	}

	@Override
	public Class<DataIndex> getEntityClass() {
		return DataIndex.class;
	}

	@Override
	public Uni<DataIndex> deleteById(long entityId) {
		return sessionFactory.withTransaction(s ->
			findById(s, entityId)
				.onItem()
				.transformToUni(dataIndex -> Uni.createFrom()
					.<AcknowledgedResponse>emitter(emitter -> {

						try {
							DeleteIndexRequest deleteIndexRequest =
								new DeleteIndexRequest(dataIndex.getIndexName());

							deleteIndexRequest
								.indicesOptions(
									IndicesOptions.fromMap(
										Map.of("ignore_unavailable", true),
										deleteIndexRequest.indicesOptions()
									)
								);

							AcknowledgedResponse delete = restHighLevelClient.indices().delete(
								deleteIndexRequest,
								RequestOptions.DEFAULT
							);

							emitter.complete(delete);
						}
						catch (UnknownTenantException | IOException e) {
							emitter.fail(e);
						}
					})
				)
				.onItem()
				.transformToUni(ignore -> findById(s, entityId)
					.call(dataIndex -> s.fetch(dataIndex.getDocTypes())))
				.onItem()
				.transformToUni(dataIndex -> {
					dataIndex.getDocTypes().clear();
					return s.persist(dataIndex);
				})
				.onItem()
				.transformToUni(ignore -> deleteById(s, entityId))
		);
	}

	public Uni<DataIndex> createByDatasource(Mutiny.Session session, Datasource datasource) {

		var pluginDriver = datasource.getPluginDriver();
		var jsonConfig = pluginDriver.getJsonConfig();
		var pluginDriverInfo = Json.decodeValue(jsonConfig, HttpPluginDriverInfo.class);

		return pluginDriverClient.getSample(pluginDriverInfo)
			.flatMap(ingestionPayload -> {

				var documentTypes = IngestionPayloadMapper.getDocumentTypes(ingestionPayload);

				var mappings = OpenSearchUtils.getDynamicMapping(
					ingestionPayload,
					ingestionPayloadMapper
				);

				var transientDataIndex = new DataIndex();

				transientDataIndex.setName(String.format("dataindex-%s", UUID.randomUUID()));
				transientDataIndex.setDatasource(datasource);

				return create(session, transientDataIndex)
					.flatMap(dataIndex -> indexerEvents
						.generateDocTypeFields(
							session, dataIndex, mappings.getMap(), documentTypes)
						.flatMap(__ -> findById(session, dataIndex.getId()))
					);
			});
	}

	public Uni<DataIndex> createDataIndexFromDocTypes(
		long datasourceId, List<Long> docTypeIds, String name,
		Map<String, Object> indexSettings) {

		String dataIndexName = name == null ? "data-" + OffsetDateTime.now() : name;

		return sessionFactory.withTransaction((s, t) -> docTypeService
			.findDocTypes(docTypeIds, s)
			.flatMap(docTypeList -> {

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

				return persist(s, dataIndex)
					.map(__ -> {
						Map<MappingsKey, Object> mappings =
							MappingsUtil.docTypesToMappings(dataIndex.getDocTypes());

						Settings settings;

						Map<String, Object> settingsMap =
							indexSettings != null && !indexSettings.isEmpty() ?
								indexSettings :
								MappingsUtil.docTypesToSettings(dataIndex.getDocTypes());

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
							var indexName = dataIndex.getIndexName();

							composableIndexTemplate = new ComposableIndexTemplate(
								List.of(indexName),
								new Template(settings, new CompressedXContent(
									Json.encode(mappings)), null),
								null, null, null, null
							);

							putComposableIndexTemplateRequest
								.name(indexName + "-template")
								.indexTemplate(composableIndexTemplate);

							return putComposableIndexTemplateRequest;
						}
						catch (UnknownTenantException e) {
							throw new WebApplicationException(Response
								.status(Response.Status.INTERNAL_SERVER_ERROR)
								.entity(JsonObject.of(
									DETAILS_FIELD, "cannot obtain a proper index name"
								))
								.build());
						}
						catch (IOException e) {
							throw new WebApplicationException(Response
								.status(Response.Status.INTERNAL_SERVER_ERROR)
								.entity(JsonObject.of(
									DETAILS_FIELD, "failed creating IndexTemplate"
								))
								.build());
						}

					})
					.call((req) -> Uni.createFrom().emitter((sink) -> {

						try {
							IndicesClient indices = restHighLevelClient.indices();

							indices.putIndexTemplate(req, RequestOptions.DEFAULT);

							sink.complete(null);
						}
						catch (OpenSearchStatusException e) {
							sink.fail(new WebApplicationException(javax.ws.rs.core.Response
								.status(e.status().getStatus())
								.entity(JsonObject.of(
									DETAILS_FIELD, e.getMessage()))
								.build()));
						}
						catch (Exception e) {
							sink.fail(new WebApplicationException(javax.ws.rs.core.Response
								.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR)
								.entity(JsonObject.of(
									DETAILS_FIELD, e.getMessage()))
								.build()));
						}

					}))
					.map(__ -> dataIndex);

			})
		);
	}

}
