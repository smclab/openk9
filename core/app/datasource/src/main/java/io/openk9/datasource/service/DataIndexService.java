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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.index.DataIndexTemplate;
import io.openk9.datasource.index.IndexMappingService;
import io.openk9.datasource.index.IndexName;
import io.openk9.datasource.index.IndexService;
import io.openk9.datasource.mapper.DataIndexMapper;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.dto.DataIndexDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.web.DataIndexResource;
import io.openk9.datasource.web.dto.DataIndexByDocTypes;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DataIndexService
	extends BaseK9EntityService<DataIndex, DataIndexDTO> {

	public static final String DETAILS_FIELD = "details";
	private static final Logger log = Logger.getLogger(DataIndexService.class);

	@Inject
	DocTypeService docTypeService;
	@Inject
	public
	EmbeddingModelService embeddingModelService;
	@Inject
	IndexService indexService;
	@Inject
	IndexMappingService indexMappingService;

	DataIndexService(DataIndexMapper mapper) {
		this.mapper = mapper;
	}

	public Uni<DataIndex> createDataIndex(
		Mutiny.Session session, long datasourceId, @Nullable DataIndexDTO dataIndexDTO) {

		dataIndexDTO = requireDataIndexDTOElseGet(dataIndexDTO, datasourceId);

		var settingsMap = getSettingsMap(dataIndexDTO.getSettings());

		return createDataIndexTransient(session, datasourceId, dataIndexDTO)
			.flatMap(dataIndex -> merge(session, dataIndex)
				.call(merged -> session.find(TenantBinding.class, 1L)
					.map(K9Entity::getTenant)
					.call(tenantId -> embeddingModelService
						.fetchCurrent(session)
						.onFailure()
						.recoverWithNull()
						.flatMap(embeddingModel ->
							indexMappingService.createDataIndexTemplate(
								new DataIndexTemplate(
									tenantId, settingsMap, merged, embeddingModel)
							)
						)
					)
				)
			);

	}

	public Uni<DataIndex> createDataIndex(
		Mutiny.Session session, Datasource datasource, @Nullable DataIndexDTO dataIndexDTO) {

		var datasourceId = datasource.getId();

		return createDataIndex(session, datasourceId, dataIndexDTO);

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

	/**
	 * Given a datasource, this method fetches the related dataIndex, then it gets
	 * the mappings of the related index, it generates the docType and docTypeFields
	 * from the mappings and it finally binds dataIndex and docTypes.
	 *
	 * @param request contains the datasourceId from where the docTypes are generated.
	 * @return the return is an empty void, the caller does not need any return.
	 * The operation is allowed to fail with a UniFail.
	 */
	public Uni<Void> autoGenerateDocTypes(
		DataIndexResource.AutoGenerateDocTypesRequest request) {

		return sessionFactory.withTransaction(session -> {

			// select d.dataIndex from Datasource d where d.id = :datasourceId
			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
			CriteriaQuery<DataIndex> query = cb.createQuery(DataIndex.class);
			Root<Datasource> from = query.from(Datasource.class);
			query.select(from.get(Datasource_.dataIndex));
			query.where(from.get(Datasource_.id).in(request.getDatasourceId()));

			return session
				.createQuery(query)
				.getSingleResult()
				.flatMap(dataIndex -> indexMappingService
					.generateDocTypeFieldsFromIndexName(session, dataIndex.getIndexName())
					.flatMap(docTypes -> {
						dataIndex.setDocTypes(docTypes);
						return merge(session, dataIndex);
					})
				)
				.replaceWithVoid();

		});
	}

	/**
	 * Create a new dataIndex for a datasource, the indexMapping will be created
	 * from the docTypes fetched by their ids.
	 *
	 * @param datasourceId the id of the datasource related to this dataIndex
	 * @param request
	 * @return
	 */
	public Uni<DataIndex> createDataIndexByDocTypes(
		long datasourceId, DataIndexByDocTypes request) {

		var dataIndexDTO = requireDataIndexDTOElseGet(
			request.getDataIndex(), datasourceId);

		var docTypeIds = request.getDocTypeIds();
		var settings = request.getSettings();

		return sessionFactory.withTransaction((s, t) ->
			docTypeService
				.findDocTypes(docTypeIds, s)
				.flatMap(docTypeList -> createDataIndexTransient(
					s, datasourceId, dataIndexDTO)
					.flatMap(transientDataIndex -> {
						if (docTypeList.size() != docTypeIds.size()) {
							throw new RuntimeException(
								"docTypeIds found: " + docTypeList.size() +
								" docTypeIds requested: " + docTypeIds.size());
						}

						transientDataIndex.setDocTypes(
							new LinkedHashSet<>(docTypeList));
						transientDataIndex.setDatasource(s.getReference(
							Datasource.class,
							datasourceId
						));

						Uni<EmbeddingModel> knnFlowUni;

						if (transientDataIndex.getKnnIndex()) {

							var embeddingModelId = request.getEmbeddingModelId();

							if (embeddingModelId != null && embeddingModelId > 0) {
								knnFlowUni = embeddingModelService.findById(
									s, embeddingModelId);
							}
							else {
								knnFlowUni = embeddingModelService.fetchCurrent(s);
							}

						}
						else {
							knnFlowUni = Uni.createFrom().nullItem();
						}

						return knnFlowUni
							.onFailure()
							.transform(EmbeddingModelNotFound::new)
							.flatMap(embeddingModel -> persist(s, transientDataIndex)
								.flatMap(dataIndex -> indexMappingService
									.createDataIndexTemplate(new DataIndexTemplate(
										null, settings, dataIndex, embeddingModel))
									.map(unused -> dataIndex)
								)
							);

					})
				)
		);
	}

	private static Map<String, Object> getSettingsMap(String settingsJson) {

		Map<String, Object> settingsMap = null;
		try {
			var settingsJsonObj = (JsonObject) Json.decodeValue(settingsJson);
			settingsMap = settingsJsonObj.getMap();
		}
		catch (Exception exception) {
			log.warnf(exception, "Cannot decode settingsJson %s", settingsJson);
			settingsMap = Map.of();
		}

		return settingsMap;
	}

	@Override
	public Uni<DataIndex> deleteById(long entityId) {
		return sessionFactory.withTransaction(s -> findById(s, entityId)
			.call(dataIndex -> indexService.deleteIndex(
				new IndexName(dataIndex.getIndexName())))
			.call(dataIndex -> s.fetch(dataIndex.getDocTypes()))
			.flatMap(dataIndex -> {
				dataIndex.getDocTypes().clear();
				return s.persist(dataIndex);
			})
			.flatMap(ignore -> deleteById(s, entityId))
		);
	}

	public Uni<Long> getCountIndexDocuments(String name) {
		return indexService.indexCount(name);
	}

	public Uni<Set<DocType>> getDocTypes(DataIndex dataIndex) {
		return sessionFactory.withTransaction(s -> s.fetch(dataIndex.getDocTypes()));
	}

	public Uni<Page<DocType>> getDocTypes(
		long dataIndexId, Pageable pageable) {
		return getDocTypes(dataIndexId, pageable, Filter.DEFAULT);
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

	public Uni<Connection<DocType>> getDocTypesConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean not) {
		return findJoinConnection(
			id, DataIndex_.DOC_TYPES, DocType.class,
			docTypeService.getSearchFields(),
			after, before, first, last, searchText, sortByList, not
		);
	}

	public Uni<DocTypeField> getEmbeddingDocTypeField(long id) {
		return sessionFactory.withTransaction((s, t) -> findById(s, id)
			.flatMap(dataIndex -> s.fetch(dataIndex.getEmbeddingDocTypeField()))
		);
	}

	@Override
	public Class<DataIndex> getEntityClass() {
		return DataIndex.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{DataIndex_.NAME, DataIndex_.DESCRIPTION};
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
	public Uni<DataIndex> update(
		Mutiny.Session session, long id, DataIndexDTO dto) {

		return findThenMapAndPersist(
			session, id, dto,
			(dIdx, dIdxDto) -> updateMapperClosure(
				session, dIdx, dIdxDto)
		);
	}

	@Override
	protected Uni<DataIndex> patch(
		Mutiny.Session session, long id, DataIndexDTO dto) {

		return findThenMapAndPersist(
			session, id, dto,
			(dIdx, dIdxDto) -> patchMapperClosure(
				session, dIdx, dIdxDto)
		);
	}

	private static DataIndexDTO requireDataIndexDTOElseGet(
		DataIndexDTO dataIndexDTO,
		Long datasourceId) {
		DataIndexDTO dto;
		if (dataIndexDTO == null) {
			dto = new DataIndexDTO();
		}
		else {
			dto = dataIndexDTO;
		}

		if (dto.getName() == null) {
			var dataIndexName = String.format(
				"%s-%s",
				datasourceId,
				UUID.randomUUID()
			);

			dto.setName(dataIndexName);
		}
		return dto;
	}

	private Uni<DataIndex> createDataIndexTransient(
		Mutiny.Session session, long datasourceId, DataIndexDTO dto) {

		// get docTypeIds
		Set<Long> docTypeIds =
			Objects.requireNonNullElseGet(dto.getDocTypeIds(), Set::of);

		return docTypeService.getDocTypesAndDocTypeFields(session, docTypeIds)
			.map(docTypes -> {

				// mapping basic field
				var dataIndex = mapper.create(dto);

				// mapping docTypes
				dataIndex.setDocTypes(Set.copyOf(docTypes));

				// mapping datasource
				var datasource = session.getReference(Datasource.class, datasourceId);
				dataIndex.setDatasource(datasource);

				// mapping embeddingDocTypeField
				if (dto.getEmbeddingDocTypeFieldId() != null) {
					dataIndex.setEmbeddingDocTypeField(session.getReference(
							DocTypeField.class,
							dto.getEmbeddingDocTypeFieldId()
						)
					);
				}

				return dataIndex;
			});
	}

	private DataIndex patchMapperClosure(
		Mutiny.Session session, DataIndex prev, DataIndexDTO dto) {

		var patched = mapper.patch(prev, dto);

		if (dto.getEmbeddingDocTypeFieldId() != null) {
			patched.setEmbeddingDocTypeField(session.getReference(
				DocTypeField.class, dto.getEmbeddingDocTypeFieldId()));
		}

		return patched;
	}

	private DataIndex updateMapperClosure(
		Mutiny.Session session, DataIndex prev, DataIndexDTO dto) {

		var updated = mapper.update(prev, dto);

		if (dto.getEmbeddingDocTypeFieldId() == null) {
			updated.setEmbeddingDocTypeField(null);
		}
		else {
			updated.setEmbeddingDocTypeField(session.getReference(
				DocTypeField.class, dto.getEmbeddingDocTypeFieldId()));
		}

		return updated;
	}

}
