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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
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
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.dto.base.DataIndexDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.web.DataIndexResource;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
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
	EmbeddingModelService embeddingModelService;
	@Inject
	IndexService indexService;
	@Inject
	IndexMappingService indexMappingService;

	DataIndexService(DataIndexMapper mapper) {
		this.mapper = mapper;
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

	@Override
	public Uni<DataIndex> create(DataIndex entity) {
		return create((Mutiny.Session) null, entity);
	}

	@Override
	public Uni<DataIndex> create(DataIndexDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) -> create(session, dto));
	}

	@Override
	public Uni<DataIndex> create(
		Mutiny.Session session, DataIndexDTO dataIndexDTO) {

		if (dataIndexDTO == null) {
			return Uni.createFrom()
				.failure(new ValidationException("dataIndexDTO cannot be null."));
		}

		var constraintViolations = validator.validate(dataIndexDTO);

		if (!constraintViolations.isEmpty()) {
			return Uni.createFrom()
				.failure(new ConstraintViolationException(constraintViolations));
		}

		var settingsMap = getSettingsMap(dataIndexDTO.getSettings());

		return createDataIndexTransient(session, dataIndexDTO)
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

	@Override
	public Uni<DataIndex> create(Mutiny.Session session, DataIndex dataIndex) {
		// cannot be created from dataIndex entity, because we cannot obtain a setting map from there.

		throw new UnsupportedOperationException("DataIndex can only be created from dataIndexDTO");
	}

	@Override
	public Uni<DataIndex> create(String tenantId, DataIndexDTO dto) {
		return sessionFactory.withTransaction(
			tenantId, (session, transaction) -> create(session, dto));
	}

	@Override
	public Uni<DataIndex> create(String tenantId, DataIndex entity) {
		return create((Mutiny.Session) null, entity);
	}

	public Uni<Response<DataIndex>> createDataIndex(DataIndexDTO dataIndexDTO) {

		return sessionFactory.withTransaction(
				(session, transaction) -> create(session, dataIndexDTO))
			.onFailure()
			.invoke(throwable -> log.errorf(
				throwable,
				"DataIndexDTO %s cannot be created.",
				dataIndexDTO
			))
			.onItemOrFailure()
			.transform(BaseK9EntityService::toResponse);

	}

	/**
	 * Deletes all data indices associated with a specific datasource, processing them in controlled chunks.
	 *
	 * <p>This method performs a multi-step deletion process:
	 * <ul>
	 *   <li>Retrieves all data indices linked to the given datasource</li>
	 *   <li>Processes deletions in chunks of up to 10 indices to manage resource consumption</li>
	 *   <li>Concurrently deletes index entries from both the search index and the database</li>
	 *   <li>Publishes delete events for each processed data index</li>
	 * </ul>
	 * </p>
	 *
	 * @param session      The Hibernate reactive session used for database operations
	 * @param datasourceId The unique identifier of the datasource whose indices should be deleted
	 * @return A {@link Uni} that resolves to a list of all deleted {@link DataIndex} instances
	 * or an empty list if no indices are found for the datasource
	 * @implNote <ul>
	 * <li>Uses {@link UniJoin} to manage concurrent deletions with controlled concurrency</li>
	 * <li>Logs a warning if no data indices are found for the given datasource</li>
	 * <li>Publishes {@link K9EntityEvent} for each deleted data index</li>
	 * <li>Limits chunk size to 10 to prevent overwhelming system resources</li>
	 * </ul>
	 */
	public Uni<List<DataIndex>> deleteAllByDatasourceId(Mutiny.Session session, long datasourceId) {

		return session.createNamedQuery(
				DataIndex.DATA_INDICES_WITH_DOC_TYPES_BY_DATASOURCE, DataIndex.class)
			.setParameter("datasourceId", datasourceId)
			.getResultList()
			.flatMap(dataIndices -> {

				if (dataIndices.isEmpty()) {
					log.warnf("No dataIndex founds for datasource with id %s", datasourceId);

					return Uni.createFrom().item(new ArrayList<>());
				}

				UniJoin.Builder<List<DataIndex>> uniJoin = Uni.join().builder();

				var iterator = dataIndices.iterator();
				while (iterator.hasNext()) {

					// create a chunk of max 10 elements
					List<DataIndex> chunk = new ArrayList<>(10);
					Set<IndexName> dataIndexNames = new HashSet<>(10);
					do {
						var dataIndex = iterator.next();

						dataIndexNames.add(new IndexName(dataIndex.getIndexName()));
						chunk.add(dataIndex);
					}
					while (iterator.hasNext() && chunk.size() <= 10);

					// construct the uni chain to invoke for chunk deletions
					var deletions = indexService.deleteIndices(dataIndexNames)
						.flatMap(unused -> session.removeAll(chunk.toArray()))
						.invoke(unused -> {
							for (DataIndex dataIndex : chunk) {
								getProcessor().onNext(K9EntityEvent.of(
										K9EntityEvent.EventType.DELETE,
										dataIndex
									)
								);
							}
						})
						.map(unused -> chunk);

					uniJoin.add(deletions);

					// create a new chunk if dataIndex iterator has other elements
				}

				return uniJoin
					.joinAll()
					.usingConcurrencyOf(1)
					.andCollectFailures()
					.map(lists -> lists
						.stream()
						.flatMap(Collection::stream)
						.toList()
					);
			});
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
	public Uni<DataIndex> patch(long id, DataIndexDTO dto) {
		return patch((Mutiny.Session) null, id, dto);
	}

	@Override
	public Uni<DataIndex> patch(String tenantId, long id, DataIndexDTO dto) {
		return patch((Mutiny.Session) null, id, dto);
	}

	@Override
	public Uni<DataIndex> update(long id, DataIndexDTO dto) {
		return update((Mutiny.Session) null, id, dto);
	}

	@Override
	public Uni<DataIndex> update(String tenantId, long id, DataIndexDTO dto) {
		return update((Mutiny.Session) null, id, dto);
	}

	@Override
	public Uni<DataIndex> update(
		Mutiny.Session session, long id, DataIndexDTO dto) {
		// a dataIndex cannot be updated

		throw new UnsupportedOperationException("update not supported for DataIndex");
	}

	@Override
	protected Uni<DataIndex> patch(
		Mutiny.Session session, long id, DataIndexDTO dto) {
		// a dataIndex cannot be updated

		throw new UnsupportedOperationException("patch not supported for DataIndex");
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

	private Uni<DataIndex> createDataIndexTransient(
		Mutiny.Session session, DataIndexDTO dto) {

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
				var datasource = session.getReference(Datasource.class, dto.getDatasourceId());
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

}
