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

import io.openk9.datasource.index.exception.DeleteDataIndexException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;

import io.openk9.common.graphql.SortBy;
import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.web.Response;
import io.openk9.datasource.index.IndexMappingService;
import io.openk9.datasource.index.IndexService;
import io.openk9.datasource.index.exception.IndexMappingException;
import io.openk9.datasource.index.model.DataIndexTemplate;
import io.openk9.datasource.index.model.IndexName;
import io.openk9.datasource.index.model.MappingsKey;
import io.openk9.datasource.index.response.CatResponse;
import io.openk9.datasource.index.util.IndexMappingUtils;
import io.openk9.datasource.mapper.DataIndexMapper;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.dto.base.DataIndexDTO;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.web.DataIndexResource;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import org.eclipse.microprofile.graphql.Description;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.hibernate.HibernateException;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DataIndexService
	extends BaseK9EntityService<DataIndex, DataIndexDTO> {

	public static final String DETAILS_FIELD = "details";
	private static final Logger log = Logger.getLogger(DataIndexService.class);

	@Inject
	DocTypeFieldService docTypeFieldService;
	@Inject
	DocTypeService docTypeService;
	@Inject
	EmbeddingModelService embeddingModelService;
	@Inject
	IndexMappingService indexMappingService;
	@Inject
	IndexService indexService;
	@Inject
	SchedulerService schedulerService;

	DataIndexService(DataIndexMapper mapper) {
		this.mapper = mapper;
	}

	private static Map<String, Object> getSettingsMap(String settingsJson) {

		// no settings were requested, which is not a decoding failure
		if (settingsJson == null) {
			return Map.of();
		}

		Map<String, Object> settingsMap = null;
		try {
			var settingsJsonObj = (JsonObject) Json.decodeValue(settingsJson);
			settingsMap = settingsJsonObj.getMap();
		}
		catch (Exception exception) {
			log.warnf("Cannot decode settingsJson %s", settingsJson);
			settingsMap = Map.of();
		}

		return settingsMap;
	}

	public Uni<Tuple2<DataIndex, DocType>> addDocType(
		long dataIndexId, long docTypeId) {
		return sessionFactory.withTransaction(s -> findById(s, dataIndexId)
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

		return sessionFactory.withTransaction(session -> getCurrentTenant(session)
			.flatMap(tenantId -> {

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
						.generateDocTypeFieldsFromIndexName(
							session,
							IndexName.from(tenantId, dataIndex)
						)
						.flatMap(docTypes -> {
							dataIndex.setDocTypes(docTypes);
							return merge(session, dataIndex);
						})
					)
					.replaceWithVoid();

			}));
	}

	public Uni<CatResponse> catIndex(Long id) {
		return sessionFactory.withTransaction((session, transaction) ->
			getCurrentTenant(session)
				.flatMap(tenantId -> findById(session, id).flatMap(
					dataIndex -> indexService.get_catIndicesFirst(
						IndexName.from(tenantId, dataIndex).toString())))
		)
		.onFailure()
		.invoke(throwable ->
			log.debug(String.format("Error retrieving cat for index with id %d", id), throwable)
		);
	}

	@Override
	public Uni<DataIndex> create(DataIndex entity) {
		return sessionFactory.withTransaction(
			(session, transaction) -> create(session, entity));
	}

	@Override
	public Uni<DataIndex> create(DataIndexDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) -> create(session, dto));
	}

	@Override
	public Uni<DataIndex> create(Mutiny.Session s, DataIndexDTO dto) {
		// cannot create a dataIndex without a datasource associated

		throw new UnsupportedOperationException(
			"Dataindex cannot be created without a datasource associated");
	}


	public Uni<DataIndex> create(
		Mutiny.Session session, long datasourceId, DataIndexDTO dataIndexDTO) {

		if (dataIndexDTO == null) {
			return Uni.createFrom()
				.failure(new ValidationException("dataIndexDTO cannot be null."));
		}

		var constraintViolations = validator.validate(dataIndexDTO);

		if (!constraintViolations.isEmpty()) {
			return Uni.createFrom()
				.failure(new ConstraintViolationException(constraintViolations));
		}

		checkKnnIndex(dataIndexDTO);

		return createDataIndexTransient(session, datasourceId, dataIndexDTO)
			.flatMap(dataIndex -> create(session, dataIndex));

	}

	/**
	 * Creates a dataIndex, whoever the caller is: an operator through the
	 * creation request, or the scheduler when a reindex starts.
	 * <p>
	 * This is the single gate every dataIndex goes through, so it is where the
	 * rules about what a valid dataIndex is live, and where its index template
	 * is generated from the embedding model in force.
	 *
	 * @param session   the session the dataIndex is created in
	 * @param dataIndex the dataIndex to create
	 * @return the created dataIndex, or a failure carrying the reason it
	 * cannot be created
	 */
	@Override
	public Uni<DataIndex> create(Mutiny.Session session, DataIndex dataIndex) {

		return resolveEmbeddingModel(session, dataIndex)
			.flatMap(embeddingModel -> merge(session, dataIndex)
				.call(merged -> createDataIndexTemplate(
					session, merged, embeddingModel))
			);
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

	public Uni<Response<DataIndex>> create(long datasourceId, DataIndexDTO dataIndexDTO) {

		return sessionFactory.withTransaction(
				(session, transaction) -> create(session, datasourceId, dataIndexDTO))
			.onFailure()
			.invoke(throwable -> log.errorf(
				throwable,
				"DataIndexDTO %s cannot be created.",
				dataIndexDTO
			))
			.onItemOrFailure()
			.transform(BaseK9EntityService::toResponse);

	}

	public Uni<Datasource> datasource(Long id) {
		return sessionFactory.withTransaction((s, t) -> findById(s, id)
			.flatMap(dataIndex -> s.fetch(dataIndex.getDatasource()))
		);
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

		return getCurrentTenant(session).flatMap(tenantId -> session
			.createNamedQuery(DataIndex.DATA_INDICES_WITH_DOC_TYPES_BY_DATASOURCE, DataIndex.class)
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

						dataIndexNames.add(IndexName.from(tenantId, dataIndex));
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
			}));
	}

	/**
	 * Deletes a DataIndex entity by its ID after verifying the provided name matches the entity's name.
	 *
	 * <p>This method adds a validation step to the deletion process by requiring the caller to provide
	 * the correct name of the DataIndex being deleted. This serves as a safety mechanism to prevent
	 * accidental deletions by confirming the caller has knowledge of the entity they intend to remove.
	 *
	 * <p>The validation flow:
	 * <ol>
	 *   <li>Retrieves the DataIndex entity by the provided ID</li>
	 *   <li>Validates that the provided name exactly matches the entity's name</li>
	 *   <li>If validation passes, proceeds with the deletion process</li>
	 * </ol>
	 *
	 * @param dataIndexId   The ID of the DataIndex to delete
	 * @param dataIndexName The name of the DataIndex to verify before deletion
	 * @return A {@link io.smallrye.mutiny.Uni} containing the deleted DataIndex if successful
	 * @throws ValidationException If the provided name doesn't match the entity's actual name
	 * @see #deleteById(long) The underlying deletion method called after validation
	 */
	public Uni<DataIndex> deleteById(long dataIndexId, String dataIndexName) {
		return findById(dataIndexId)
			.flatMap(dataIndex -> {
				verifyNameMatches(dataIndex.getName(), dataIndexName);
				return deleteById(dataIndexId);
			});
	}

	/**
	 * Deletes a {@link DataIndex} and its associated OpenSearch index by ID.
	 * <p>
	 * Within a transaction, retrieves the current tenant, removes the index from OpenSearch,
	 * and finally deletes the entity.
	 *
	 * @param entityId the ID of the {@link DataIndex} to delete
	 * @return a {@link Uni} emitting the deleted {@link DataIndex}
	 */
	@Override
	public Uni<DataIndex> deleteById(long entityId) {
		return sessionFactory.withTransaction(s -> getCurrentTenant(s)
			.flatMap(tenantId -> findById(s, entityId)
				.call(dataIndex -> indexService.deleteIndex(IndexName.from(tenantId, dataIndex)))
				.call(dataIndex -> s.fetch(dataIndex.getDocTypes()))
				.flatMap(dataIndex -> deleteById(s, entityId))
			))
			.onFailure(HibernateException.class)
			.transform(throwable -> {
				log.errorf("Failed to delete DataIndex, it could be associated with a DataSource", throwable);
				throw new DeleteDataIndexException(throwable);
			});
	}

	public Uni<Long> getCountIndexDocuments(long dataIndexId) {
		return sessionFactory.withTransaction(s -> getCurrentTenant(s)
			.flatMap(tenantId -> findById(s, dataIndexId)
				.flatMap(dataIndex -> indexService
					.indexCount(IndexName.from(tenantId, dataIndex).toString())
				)
			)
		);
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

	public Uni<String> getMappings(long dataIndexId) {
		return sessionFactory.withTransaction(s -> getCurrentTenant(s)
			.flatMap(tenantId -> findById(s, dataIndexId)
				.flatMap(dataIndex -> indexService
					.getMappings(IndexName.from(tenantId, dataIndex))
				)
			)
		).map(Json::encode);
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{DataIndex_.NAME, DataIndex_.DESCRIPTION};
	}

	/**
	 * Reads the settings declared in the index template of a dataIndex, which
	 * are not necessarily the settings it was created with.
	 *
	 * @param dataIndexId the id of the dataIndex
	 * @return the settings the index template declares
	 */
	public Uni<String> getIndexTemplateSettings(long dataIndexId) {
		return sessionFactory.withTransaction(s -> getCurrentTenant(s)
			.flatMap(tenantId -> findById(s, dataIndexId)
				.flatMap(dataIndex -> indexService
					.getIndexTemplateSettings(IndexName.from(tenantId, dataIndex)))
			)
		);
	}

	/**
	 * Reads the custom settings recorded on a dataIndex, the ones an operator
	 * asked for: what the docTypes derive is not part of them.
	 *
	 * @param dataIndexId the id of the dataIndex
	 * @return the recorded settings, an empty object when none were asked for
	 */
	public Uni<String> getCustomSettings(long dataIndexId) {

		return sessionFactory.withTransaction(session -> findById(session, dataIndexId)
			.map(dataIndex -> dataIndex.getSettings() == null
				? new JsonObject().encode()
				: dataIndex.getSettings()
			)
		);
	}

	/**
	 * Aligns to the model every index that uses a docTypeField.
	 * <p>
	 * Modifying a docTypeField changes what the model derives, and nothing
	 * carries that to OpenSearch: this does, on the index each datasource is
	 * currently pointing at. The indices a reindex left behind are not touched,
	 * because they are neither searched nor written.
	 * <p>
	 * One index failing does not stop the others: each gets its own outcome.
	 *
	 * @param docTypeFieldId the docTypeField that was modified
	 * @return an outcome for each index that uses it
	 */
	public Uni<List<IndexAlignment>> alignDataIndexes(long docTypeFieldId) {

		return sessionFactory.withTransaction(session -> docTypeFieldService
			.findById(session, docTypeFieldId)
			.flatMap(docTypeField -> docTypeField == null
				? Uni.createFrom().<DocType>failure(new ValidationException(
					String.format("DocTypeField %s does not exist", docTypeFieldId)))
				: session.fetch(docTypeField.getDocType())
			)
			.flatMap(docType -> docType == null
				// a docTypeField detached from its docType is part of no index
				? Uni.createFrom().item(List.<DataIndex>of())
				: session
					.createNamedQuery(
						DataIndex.CURRENT_DATA_INDICES_BY_DOC_TYPE, DataIndex.class)
					.setParameter("docTypeId", docType.getId())
					.getResultList()
			)
			.flatMap(dataIndexes -> alignEach(session, dataIndexes))
		);
	}

	/**
	 * Aligns one index to the model, which is what a caller uses to retry an
	 * index that was skipped or failed.
	 *
	 * @param dataIndexId the dataIndex to align
	 * @return the outcome of that index
	 */
	public Uni<IndexAlignment> alignDataIndex(long dataIndexId) {

		return sessionFactory.withTransaction(session -> findById(session, dataIndexId)
			.flatMap(dataIndex -> dataIndex == null
				? Uni.createFrom().<IndexAlignment>failure(new ValidationException(
					String.format("DataIndex %s does not exist", dataIndexId)))
				: alignDataIndex(session, dataIndex)
			)
		);
	}

	/**
	 * Aligns one index to the model in the session of the caller.
	 * <p>
	 * The settings derived from the docTypes go first and the mappings after,
	 * because the mappings name the analyzers the settings define; the index is
	 * closed only when it does not already carry those definitions, since the
	 * analysis block is static. The index template is regenerated whatever
	 * happens on the live index: it is the only thing that exists when the
	 * index has not been written to yet, and it is what a new index is born
	 * from.
	 *
	 * @param session the session the entities are read in
	 * @param dataIndex the dataIndex to align
	 * @return the outcome of that index, never a failure for a refusal of
	 * OpenSearch
	 */
	public Uni<IndexAlignment> alignDataIndex(
		Mutiny.Session session, DataIndex dataIndex) {

		return getCurrentTenant(session).flatMap(tenantId -> {

			var indexName = IndexName.from(tenantId, dataIndex);

			return session.fetch(dataIndex.getDocTypes())
				// the mappings are built from the docTypeFields, and the
				// analyzers are reached from there: without expanding them
				// search_analyzer silently disappears from what is generated
				.flatMap(docTypes -> docTypeFieldService
					.expandDocTypes(session, docTypes))
				.flatMap(docTypes -> applyToIndex(
					session,
					dataIndex,
					indexName,
					IndexMappingUtils.docTypesToSettings(docTypes),
					IndexMappingUtils.docTypesToMappings(docTypes)
				))
				.flatMap(alignment -> resolveEmbeddingModel(session, dataIndex)
					.flatMap(embeddingModel -> createDataIndexTemplate(
						session, dataIndex, embeddingModel))
					.replaceWith(alignment)
				)
				.onFailure(IndexMappingException.class)
				.recoverWithItem(throwable -> failed(dataIndex, indexName, throwable));
		});
	}

	/**
	 * Applies the custom settings of a dataIndex, the ones an operator asks for
	 * and the model does not derive.
	 * <p>
	 * The document is received whole, but the semantics stay the ones of
	 * OpenSearch: a key the document does not name is left alone, and a key set
	 * to {@code null} goes back to its default and disappears from what is
	 * recorded. Deleting a line from the document therefore changes nothing.
	 * <p>
	 * The definitions the docTypes derive are refused: the mappings name them,
	 * and the two halves of an index template have to keep saying the same
	 * thing.
	 * <p>
	 * What the dataIndex records and its index template are written only when
	 * OpenSearch took the settings, so that what is recorded keeps telling what
	 * the index has.
	 *
	 * @param dataIndexId the dataIndex whose settings are updated
	 * @param settings the settings to apply, a JSON object
	 * @return the outcome of that index
	 */
	public Uni<IndexAlignment> updateIndexSettings(
		long dataIndexId, String settings) {

		JsonObject requested;

		try {
			requested = (JsonObject) Json.decodeValue(settings);
		}
		catch (Exception exception) {
			requested = null;
		}

		if (requested == null) {
			return Uni.createFrom().failure(
				new ValidationException("The settings must be a JSON object"));
		}

		var requestedSettings = requested;

		return sessionFactory.withTransaction(session -> findById(session, dataIndexId)
			.flatMap(dataIndex -> dataIndex == null
				? Uni.createFrom().<IndexAlignment>failure(new ValidationException(
					String.format("DataIndex %s does not exist", dataIndexId)))
				: updateIndexSettings(session, dataIndex, requestedSettings)
			)
		);
	}

	private Uni<IndexAlignment> updateIndexSettings(
		Mutiny.Session session, DataIndex dataIndex, JsonObject requested) {

		return getCurrentTenant(session).flatMap(tenantId -> {

			var indexName = IndexName.from(tenantId, dataIndex);

			return session.fetch(dataIndex.getDocTypes())
				.flatMap(docTypes -> docTypeFieldService
					.expandDocTypes(session, docTypes))
				.flatMap(docTypes -> {

					var reserved = IndexMappingUtils.derivedAnalysisNamesIn(
						IndexMappingUtils.docTypesToSettings(docTypes), requested);

					if (!reserved.isEmpty()) {
						return Uni.createFrom().<IndexAlignment>failure(
							new ValidationException(String.format(
								"These analysis definitions come from the docTypes "
									+ "of the dataIndex and cannot be set here: %s",
								String.join(", ", reserved)
							)));
					}

					return applyCustomSettings(session, dataIndex, indexName, requested)
						.flatMap(alignment -> recordSettings(
							session, dataIndex, requested, alignment));
				});
		});
	}

	private Uni<IndexAlignment> applyCustomSettings(
		Mutiny.Session session,
		DataIndex dataIndex,
		IndexName indexName,
		JsonObject requested) {

		var settings = requested.getMap();

		return indexService.putSettings(indexName, settings, false)
			.map(exists -> applied(dataIndex, indexName, exists))
			.onFailure(IndexMappingException.class)
			.recoverWithUni(throwable -> {

				if (!IndexService.requiresClosedIndex(throwable)) {
					return Uni.createFrom().item(
						failed(dataIndex, indexName, throwable));
				}

				return closingIsAllowed(session, dataIndex).flatMap(jobStatus -> {

					if (jobStatus != SchedulerService.JobStatus.NOT_RUNNING) {
						return Uni.createFrom().item(skipped(
							dataIndex,
							indexName,
							"the settings are static and the index has to be "
								+ "closed to take them",
							jobStatus
						));
					}

					return indexService.putSettings(indexName, settings, true)
						.map(exists -> applied(dataIndex, indexName, exists))
						.onFailure(IndexMappingException.class)
						.recoverWithItem(failure -> failed(
							dataIndex, indexName, failure));
				});
			});
	}

	private Uni<IndexAlignment> recordSettings(
		Mutiny.Session session,
		DataIndex dataIndex,
		JsonObject requested,
		IndexAlignment alignment) {

		var status = alignment.status();

		if (status != IndexAlignment.Status.APPLIED
			&& status != IndexAlignment.Status.TEMPLATE_ONLY) {

			// nothing reached the index, so nothing is recorded either
			return Uni.createFrom().item(alignment);
		}

		dataIndex.setSettings(IndexMappingUtils
			.mergeSettings(
				new JsonObject(getSettingsMap(dataIndex.getSettings())), requested)
			.encode()
		);

		return resolveEmbeddingModel(session, dataIndex)
			.flatMap(embeddingModel -> createDataIndexTemplate(
				session, dataIndex, embeddingModel))
			.replaceWith(alignment);
	}

	private Uni<List<IndexAlignment>> alignEach(
		Mutiny.Session session, List<DataIndex> dataIndexes) {

		// one index at a time: closing an index is not something to do to
		// several datasources at once
		Uni<List<IndexAlignment>> alignments =
			Uni.createFrom().item(new ArrayList<>());

		for (DataIndex dataIndex : dataIndexes) {
			alignments = alignments.flatMap(collected -> alignDataIndex(
					session, dataIndex)
				.map(alignment -> {
					collected.add(alignment);

					return collected;
				})
			);
		}

		return alignments.map(List::copyOf);
	}

	private Uni<IndexAlignment> applyToIndex(
		Mutiny.Session session,
		DataIndex dataIndex,
		IndexName indexName,
		Map<String, Object> settings,
		Map<MappingsKey, Object> mappings) {

		return indexService.getIndexSettings(indexName).flatMap(liveSettings -> {

			if (liveSettings == null) {
				return Uni.createFrom().item(
					templateOnly(dataIndex, indexName));
			}

			if (IndexMappingUtils.isAnalysisApplied(settings, liveSettings)) {
				// the definitions are already there, and OpenSearch refuses a
				// static setting on an open index even when nothing changes
				return indexService
					.putSettings(
						indexName, IndexMappingUtils.withoutAnalysis(settings), false)
					.flatMap(exists -> putMapping(
						dataIndex, indexName, mappings, exists));
			}

			return closingIsAllowed(session, dataIndex)
				.flatMap(jobStatus -> {

					if (jobStatus != SchedulerService.JobStatus.NOT_RUNNING) {
						return Uni.createFrom().item(skipped(
							dataIndex,
							indexName,
							"the index has to be closed to take the analyzers the "
								+ "model declares",
							jobStatus
						));
					}

					return indexService.putSettings(indexName, settings, true)
						.flatMap(exists -> putMapping(
							dataIndex, indexName, mappings, exists));
				});
		});
	}

	private Uni<IndexAlignment> putMapping(
		DataIndex dataIndex,
		IndexName indexName,
		Map<MappingsKey, Object> mappings,
		Boolean indexExists) {

		if (Boolean.FALSE.equals(indexExists)) {
			return Uni.createFrom().item(templateOnly(dataIndex, indexName));
		}

		return indexService.putMapping(indexName, mappings)
			.map(exists -> applied(dataIndex, indexName, exists));
	}

	private static IndexAlignment applied(
		DataIndex dataIndex, IndexName indexName, Boolean indexExists) {

		return Boolean.FALSE.equals(indexExists)
			? templateOnly(dataIndex, indexName)
			: new IndexAlignment(
				dataIndex.getId(),
				indexName.toString(),
				IndexAlignment.Status.APPLIED,
				null
			);
	}

	private Uni<SchedulerService.JobStatus> closingIsAllowed(
		Mutiny.Session session, DataIndex dataIndex) {

		// the scheduler takes part in none of these transactions, so this is a
		// filter and not a guarantee: an ingestion can start right after it
		return session.fetch(dataIndex.getDatasource())
			.flatMap(datasource -> schedulerService
				.getJobStatus(session, datasource.getId()));
	}

	private static IndexAlignment templateOnly(
		DataIndex dataIndex, IndexName indexName) {

		return new IndexAlignment(
			dataIndex.getId(),
			indexName.toString(),
			IndexAlignment.Status.TEMPLATE_ONLY,
			null
		);
	}

	private static IndexAlignment skipped(
		DataIndex dataIndex,
		IndexName indexName,
		String what,
		SchedulerService.JobStatus jobStatus) {

		return new IndexAlignment(
			dataIndex.getId(),
			indexName.toString(),
			IndexAlignment.Status.SKIPPED,
			String.format(
				"%s, and a scheduling is running on its datasource (%s)",
				what,
				jobStatus
			)
		);
	}

	private static IndexAlignment failed(
		DataIndex dataIndex, IndexName indexName, Throwable throwable) {

		return new IndexAlignment(
			dataIndex.getId(),
			indexName.toString(),
			IndexAlignment.Status.FAILED,
			throwable.getMessage()
		);
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

	private void checkKnnIndex(DataIndexDTO dataIndexDTO)
		throws ValidationException {

		var knnIndex = dataIndexDTO.getKnnIndex();
		var embeddingDocTypeFieldId = dataIndexDTO.getEmbeddingDocTypeFieldId();

		if (knnIndex != null && knnIndex && embeddingDocTypeFieldId == null) {
			throw new ValidationException(
				"Ambiguous Request: knnIndex is set to true but embeddingDocTypeFieldId is not defined." +
					" Add an embeddingDocTypeFieldId or disable knnIndex");
		}

	}

	/**
	 * Generates the index template of a dataIndex from the docTypes it is
	 * composed of, the settings it recorded and the embedding model in force.
	 *
	 * @param session        the session the docTypes are expanded in
	 * @param dataIndex      the dataIndex the index template belongs to
	 * @param embeddingModel the model the vector field comes from,
	 *                       {@code null} for a plain dataIndex
	 * @return an empty {@link Uni}, failing when the index template cannot be
	 * created
	 */
	private Uni<Void> createDataIndexTemplate(
		Mutiny.Session session, DataIndex dataIndex, EmbeddingModel embeddingModel) {

		return getCurrentTenant(session)
			.flatMap(tenantId -> session.fetch(dataIndex.getDocTypes())
				// the mappings are built from the docTypeFields, so the
				// docTypes have to be expanded whoever handed them over
				.flatMap(docTypes -> docTypeFieldService
					.expandDocTypes(session, docTypes))
				.flatMap(unused -> indexMappingService.createDataIndexTemplate(
					new DataIndexTemplate(
						tenantId,
						getSettingsMap(dataIndex.getSettings()),
						dataIndex,
						embeddingModel
					)
				))
			);
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

	/**
	 * Resolves the embedding model the index template must be generated from,
	 * refusing a knn dataIndex that would hold no vector.
	 * <p>
	 * A knn dataIndex needs a field to embed and the tenant active embedding
	 * model to receive its {@code knn_vector} mapping, so its creation is
	 * refused when either is missing. A plain dataIndex never consults the
	 * model.
	 *
	 * @param session   the session the active embedding model is looked up in
	 * @param dataIndex the dataIndex being created
	 * @return the tenant active embedding model, or {@code null} when the
	 * dataIndex is not a knn index
	 */
	private Uni<EmbeddingModel> resolveEmbeddingModel(
		Mutiny.Session session, DataIndex dataIndex) {

		var knnIndex = dataIndex.getKnnIndex();

		if (knnIndex == null || !knnIndex) {
			return Uni.createFrom().nullItem();
		}

		if (dataIndex.getEmbeddingDocTypeField() == null) {
			return Uni.createFrom().failure(new ValidationException(String.format(
				"Cannot create the dataIndex %s: knnIndex is set to true but" +
				" embeddingDocTypeField is not defined." +
				" Define an embeddingDocTypeField or disable knnIndex.",
				dataIndex.getName()
			)));
		}

		return embeddingModelService.fetchCurrent(session)
			.onFailure(NoResultException.class)
			.transform(throwable -> new ValidationException(String.format(
				"Cannot create the dataIndex %s: knnIndex is set to true but" +
				" there is no active embedding model on this tenant." +
				" Enable an embedding model or disable knnIndex.",
				dataIndex.getName()
			)));
	}

	@Override
	protected Uni<DataIndex> patch(
		Mutiny.Session session, long id, DataIndexDTO dto) {
		// a dataIndex cannot be updated

		throw new UnsupportedOperationException("patch not supported for DataIndex");
	}

	/**
	 * What happened to one index when it was aligned to the model.
	 *
	 * @param dataIndexId the dataIndex the outcome belongs to
	 * @param indexName the name of the index on OpenSearch
	 * @param status what happened
	 * @param reason the explanation OpenSearch gave, or why the index was
	 *               skipped, and {@code null} when there is nothing to tell
	 */
	public record IndexAlignment(
		@Description("The dataIndex the outcome belongs to")
		Long dataIndexId,
		@Description("The name of the index on OpenSearch")
		String indexName,
		@Description("What happened to the index")
		Status status,
		@Description(
			"The explanation OpenSearch gave, or why the index was skipped, "
				+ "and null when there is nothing to tell")
		String reason
	) {

		public enum Status {

			/**
			 * OpenSearch accepted the settings and the mappings. It does not
			 * mean the index matches the model: what the model no longer
			 * declares stays in the index, because a mapping is additive.
			 */
			@Description("OpenSearch accepted the settings and the mappings")
			APPLIED,

			/**
			 * The index does not exist yet, because nothing has been written to
			 * it: only its template was regenerated, and the index will be born
			 * from it.
			 */
			@Description(
				"The index does not exist yet, so only its template was written")
			TEMPLATE_ONLY,

			/**
			 * The index had to be closed to take the analyzers the model
			 * declares, and a scheduling is running on its datasource: closing
			 * it would have broken that ingestion, so the live index was left
			 * alone.
			 */
			@Description(
				"The index had to be closed and a scheduling is running on its "
					+ "datasource, so the live index was left alone")
			SKIPPED,

			/**
			 * OpenSearch refused. A field whose type, index-time analyzer or
			 * offset source changed cannot be applied to a live index, and the
			 * refusal rejects the whole document: that index needs a reindex.
			 */
			@Description(
				"OpenSearch refused, and rejects the whole document when it "
					+ "does: that index needs a reindex")
			FAILED
		}
	}

}
