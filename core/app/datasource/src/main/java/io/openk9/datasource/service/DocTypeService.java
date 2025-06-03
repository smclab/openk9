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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.criteria.Subquery;
import jakarta.validation.ValidationException;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.DocTypeFieldMapper;
import io.openk9.datasource.mapper.DocTypeMapper;
import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.AclMapping_;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.request.DocTypeWithTemplateDTO;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.exception.K9Error;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;
import org.hibernate.FlushMode;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class DocTypeService extends BaseK9EntityService<DocType, DocTypeDTO> {
	DocTypeService(DocTypeMapper mapper) {
		this.mapper = mapper;
	}
	@Override
	public String[] getSearchFields() {
		return new String[] {DocType_.NAME, DocType_.DESCRIPTION};
	}

	@Override
	public Uni<DocType> create(DocTypeDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) -> create(session, dto));
	}

	@Override
	public Uni<DocType> create(Mutiny.Session session, DocTypeDTO dto) {
		var entity = createMapper(session, dto);

		return super.create(session, entity);
	}


	/**
	 * Deletes a DocType entity by its ID after verifying the provided name matches the entity's name.
	 *
	 * <p>This method adds a validation step to the deletion process by requiring the caller to provide
	 * the correct name of the DocType being deleted. This serves as a safety mechanism to prevent
	 * accidental deletions by confirming the caller has knowledge of the entity they intend to remove.
	 *
	 * <p>The validation flow:
	 * <ol>
	 *   <li>Retrieves the DocType entity by the provided ID</li>
	 *   <li>Validates that the provided name exactly matches the entity's name</li>
	 *   <li>If validation passes, proceeds with the deletion process</li>
	 * </ol>
	 *
	 * @param docTypeId   The ID of the DocType to delete
	 * @param docTypeName The name of the DocType to verify before deletion
	 * @return A {@link io.smallrye.mutiny.Uni} containing the deleted DocType if successful
	 * @throws ValidationException If the provided name doesn't match the entity's actual name
	 * @see #deleteById(long) The underlying deletion method called after validation
	 */
	public Uni<DocType> deleteById(long docTypeId, String docTypeName) {
		return findById(docTypeId)
			.flatMap(docType -> {
				if (!docType.getName().equals(docTypeName)) {
					throw new ValidationException("docTypeName is not the same");
				}

				return deleteById(docTypeId);
			});
	}

	/**
	 * Deletes a DocType entity by its ID, handling all necessary dereferencing of related entities.
	 *
	 * <p>This method performs a series of database operations to properly delete a DocType:
	 * <ol>
	 *   <li>Dereferences AclMappings linked to the DocType's fields</li>
	 *   <li>Removes parent and analyzer references from DocTypeFields</li>
	 *   <li>Deletes all DocTypeFields associated with the DocType</li>
	 *   <li>Dereferences any DocTypeTemplate connection</li>
	 *   <li>Removes the DocType from associated DataIndex entities</li>
	 *   <li>Finally deletes the DocType itself</li>
	 * </ol>
	 *
	 * <p>The method executes these operations within a transaction to ensure data integrity.
	 * If the deletion of DocTypeFields fails due to constraint violations (meaning they're
	 * still referenced by other entities), the operation will fail with a K9Error.
	 *
	 * @param entityId The ID of the DocType to delete
	 * @return A {@link io.smallrye.mutiny.Uni} containing the deleted DocType if successful
	 * @throws K9Error If DocTypeFields are still referenced by other entities
	 */
	@Override
	public Uni<DocType> deleteById(long entityId) {
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

		// dereference aclMappings
		CriteriaUpdate<AclMapping> updateAclMapping = cb.createCriteriaUpdate(AclMapping.class);
		Root<AclMapping> updateAclMappingFrom = updateAclMapping.from(AclMapping.class);

		Subquery<Long> fieldSubquery = updateAclMapping.subquery(Long.class);
		Root<DocTypeField> fieldSubqueryFrom = fieldSubquery.from(DocTypeField.class);
		fieldSubquery.select(fieldSubqueryFrom.get(DocTypeField_.id));
		fieldSubquery.where(cb.equal(
			fieldSubqueryFrom.get(DocTypeField_.docType).get(DocType_.id),
			entityId
		));

		updateAclMapping.where(
			updateAclMappingFrom
				.get(AclMapping_.docTypeField)
				.get(DocTypeField_.id)
				.in(fieldSubquery)
		);

		updateAclMapping.set(
			updateAclMappingFrom.get(AclMapping_.docTypeField),
			cb.nullLiteral(DocTypeField.class)
		);

		// dereference parents and analyzer
		CriteriaUpdate<DocTypeField> updateDocTypeField =
			cb.createCriteriaUpdate(DocTypeField.class);
		Root<DocTypeField> updateDocTypeFieldFrom = updateDocTypeField.from(DocTypeField.class);

		updateDocTypeField.where(
			cb.equal(
				updateDocTypeFieldFrom
					.get(DocTypeField_.docType)
					.get(DocType_.id),
				entityId
			)
		);

		updateDocTypeField.set(
			updateDocTypeFieldFrom.get(DocTypeField_.analyzer),
			cb.nullLiteral(Analyzer.class)
		);
		updateDocTypeField.set(
			updateDocTypeFieldFrom.get(DocTypeField_.parentDocTypeField),
			cb.nullLiteral(DocTypeField.class)
		);

		// delete docTypeFields
		CriteriaDelete<DocTypeField> deleteDocTypeFields =
			cb.createCriteriaDelete(DocTypeField.class);
		Root<DocTypeField> deleteFrom = deleteDocTypeFields.from(DocTypeField.class);

		deleteDocTypeFields.where(
			cb.equal(
				deleteFrom.get(DocTypeField_.docType).get(DocType_.id),
				entityId
			)
		);

		// dereference docTypeTemplate
		CriteriaUpdate<DocType> updateDocType = cb.createCriteriaUpdate(DocType.class);
		Root<DocType> docTypeFrom = updateDocType.from(DocType.class);
		updateDocType.where(cb.equal(docTypeFrom.get(DocType_.id), entityId));
		updateDocType.set(DocType_.docTypeTemplate, cb.nullLiteral(DocTypeTemplate.class));

		// queries dataIndex
		CriteriaQuery<DataIndex> dataIndexQuery = cb.createQuery(DataIndex.class);
		Root<DataIndex> dataIndexFrom = dataIndexQuery.from(DataIndex.class);
		dataIndexFrom.fetch(DataIndex_.docTypes, JoinType.INNER);
		SetJoin<DataIndex, DocType> join = dataIndexFrom.join(DataIndex_.docTypes, JoinType.INNER);
		dataIndexQuery.where(cb.equal(join.get(DocType_.id), entityId));

		return sessionFactory.withTransaction((s, t) -> findById(s, entityId)
			.call(docType -> s.createQuery(dataIndexQuery)
				.getResultList()
				.flatMap(dataIndices -> {
					for (DataIndex dataIndex : dataIndices) {
						Set<DocType> docTypes = dataIndex.getDocTypes();
						docTypes.remove(docType);
					}
					return s.mergeAll(dataIndices.toArray());
				})
			)
			.call(docType -> s.createQuery(updateAclMapping).executeUpdate())
			.call(docType -> s.createQuery(updateDocTypeField).executeUpdate())
			.call(docType -> s.createQuery(deleteDocTypeFields).executeUpdate()
				.onFailure()
				.transform(throwable -> switch (throwable) {
					case ConstraintViolationException c -> new K9Error(
						"There are some DocTypeFields referenced by other entities");
					default -> throwable;
				})
			)
			.call(docType -> s.createQuery(updateDocType).executeUpdate())
			.call(docType -> remove(s, docType))
		);
	}

	public Uni<Connection<DocTypeField>> getDocTypeFieldsConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, DocType_.DOC_TYPE_FIELDS, DocTypeField.class,
			docTypeFieldService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}

	public Uni<Connection<DocTypeField>> getDocTypeFieldsConnectionByParent(
		long docTypeId, long parentId, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			docTypeId, DocType_.DOC_TYPE_FIELDS, DocTypeField.class,
			docTypeFieldService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual,
			(criteriaBuilder, join) -> {
				Path<DocTypeField> parentField = join.get(DocTypeField_.parentDocTypeField);

				return parentId > 0
					? criteriaBuilder.equal(parentField.get(DocTypeField_.id), parentId)
					: criteriaBuilder.isNull(parentField);
			});
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long docTypeId, Pageable pageable) {
		return getDocTypeFields(docTypeId, pageable, Filter.DEFAULT);
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long docTypeId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[] { docTypeId },
			DocType_.DOC_TYPE_FIELDS, DocTypeField.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			searchText);
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long docTypeId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[] { docTypeId },
			DocType_.DOC_TYPE_FIELDS, DocTypeField.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			filter);
	}

	public Uni<Set<DocTypeField>> getDocTypeFields(DocType docType) {
		return sessionFactory.withTransaction(s -> s.fetch(docType.getDocTypeFields()));
	}

	public Uni<Tuple2<DocType, DocTypeField>> addDocTypeField(long id, DocTypeFieldDTO docTypeFieldDTO) {

		return sessionFactory.withTransaction((s) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(docType -> s.fetch(docType.getDocTypeFields()).flatMap(docTypeFields -> {

				DocTypeField docTypeField =
					docTypeFieldService.createTransient(s, docTypeFieldDTO);

				if (docType.addDocTypeField(docTypeFields, docTypeField)) {
					return persist(s, docType)
						.map(dt -> Tuple2.of(dt, docTypeField));
				}
				return Uni.createFrom().nullItem();
			})));
	}

	public Uni<Tuple2<DocType, Long>> removeDocTypeField(long id, long docTypeFieldId) {
		return sessionFactory.withTransaction((s) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(docType -> s.fetch(docType.getDocTypeFields())
				.flatMap(docTypeFields -> {
					if (docType.removeDocTypeField(docTypeFields, docTypeFieldId)) {
						return merge(s, docType)
							.map(dt -> Tuple2.of(dt, docTypeFieldId));
					}
					return Uni.createFrom().nullItem();
				})
			)
		);
	}

	public Uni<Tuple2<DocType, DocTypeTemplate>> setDocTypeTemplate(long docTypeId, long docTypeTemplateId) {
		return sessionFactory.withTransaction(s -> findById(s, docTypeId)
			.onItem()
			.ifNotNull()
			.transformToUni(docType -> docTypeTemplateService.findById(s, docTypeTemplateId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeTemplate -> {
					docType.setDocTypeTemplate(docTypeTemplate);
					return persist(s, docType)
						.map(d -> Tuple2.of(d, docTypeTemplate));
				})
			)
		);
	}

	public Uni<DocType> unsetDocType(long docTypeId) {
		return sessionFactory.withTransaction(s -> findById(s, docTypeId)
			.onItem()
			.ifNotNull()
			.transformToUni(docType -> {
				docType.setDocTypeTemplate(null);
				return persist(s, docType);
			})
		);
	}

	public Uni<List<DocTypeField>> getDocTypeFieldsByName(String docTypeName) {
		return sessionFactory.withTransaction((s) -> {
			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
			CriteriaQuery<DocTypeField> cq = cb.createQuery(DocTypeField.class);
			Root<DocType> root = cq.from(DocType.class);
			cq.select(root.join(DocType_.docTypeFields));
			cq.where(cb.equal(root.get(DocType_.name), docTypeName));
			return s.createQuery(cq).getResultList();
		});
	}

	public Uni<DocType> findByName(String name) {
		return sessionFactory.withTransaction((s) -> {
			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
			CriteriaQuery<DocType> cq = cb.createQuery(DocType.class);
			Root<DocType> root = cq.from(DocType.class);
			cq.where(cb.equal(root.get(DocType_.name), name));
			return s.createQuery(cq)
				.setFlushMode(FlushMode.MANUAL)
				.getSingleResultOrNull();
		});
	}

	public Uni<Collection<DocType>> getDocTypesAndDocTypeFieldsByNames(
		Mutiny.Session session, Collection<String> docTypeNames) {

		return getDocTypesInDocTypeNames(
			session, docTypeNames.toArray(String[]::new))
			.chain(dts -> docTypeFieldService.expandDocTypes(session, dts));
	}

	public Uni<List<DocType>> getDocTypesInDocTypeNames(
		Mutiny.Session session, String[] docTypeNames) {
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

		Class<DocType> entityClass = getEntityClass();

		CriteriaQuery<DocType> query = cb.createQuery(entityClass);

		Root<DocType> from = query.from(entityClass);

		query.where(from.get(DocType_.name).in(List.of(docTypeNames)));

		return session
			.createQuery(query)
			.getResultList();
	}

	public Uni<Boolean> existsByName(String name) {
		return sessionFactory.withTransaction(s -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			Class<DocType> entityClass = getEntityClass();

			CriteriaQuery<Long> query = cb.createQuery(Long.class);

			Root<DocType> from = query.from(entityClass);

			query.select(cb.count(from));

			query.where(cb.equal(from.get(DocType_.name), name));

			return s
				.createQuery(query)
				.getSingleResult()
				.map(count -> count > 0);

		});
	}

	public Uni<Collection<DocType>> getDocTypesAndDocTypeFields(
		Mutiny.Session session, Collection<Long> ids) {
		return findByIds(session, Set.copyOf(ids))
			.chain(dts -> docTypeFieldService.expandDocTypes(
				session, dts));
	}

	public Uni<List<DocType>> getDocTypesNotInDocTypeNames(
		Mutiny.Session session, String[] docTypeNames) {
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

		Class<DocType> entityClass = getEntityClass();

		CriteriaQuery<DocType> query = cb.createQuery(entityClass);

		Root<DocType> from = query.from(entityClass);

		query.where(
			from.get(DocType_.name)
				.in(List.of(docTypeNames))
				.not()
		);

		return session
			.createQuery(query)
			.getResultList();
	}

	public Uni<List<DocType>> findDocTypes(List<Long> docTypeIds, Mutiny.Session session) {

		return getDocTypesAndDocTypeFields(session, docTypeIds).map(LinkedList::new);
	}


	public Uni<List<DocType>> findDocTypes(List<Long> docTypeIds) {

		return sessionFactory.withSession(s -> findDocTypes(docTypeIds, s));
	}

	@Override
	public Uni<DocType> patch(long id, DocTypeDTO dto) {

		return sessionFactory.withTransaction(
			(session, transaction) -> patch(session, id, dto));
	}

	@Override
	public Uni<DocType> patch(Mutiny.Session session, long id, DocTypeDTO dto) {

		return findThenMapAndPersist(
			session,
			id,
			dto,
			(docType, docTypeDTO) -> patchMapper(session, docType, docTypeDTO)
		);
	}

	@Override
	public Uni<DocType> update(long id, DocTypeDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) -> update(session, id, dto));
	}

	@Override
	public Uni<DocType> update(Mutiny.Session session, long id, DocTypeDTO dto) {
		return findThenMapAndPersist(
			session,
			id,
			dto,
			(docType, docTypeDTO) -> updateMapper(session, docType, docTypeDTO)
		);
	}

	private DocType createMapper(Mutiny.Session session, DocTypeDTO docTypeDTO) {

		var docType = mapper.create(docTypeDTO);

		if (docTypeDTO instanceof DocTypeWithTemplateDTO docTypeWithTemplateDTO) {

			// set docTypeTemplate only when docTypeTemplateId is not null
			var docTypeTemplateId = docTypeWithTemplateDTO.getDocTypeTemplateId();
			if (docTypeTemplateId != null) {
				var docTypeTemplate = session.getReference(
					DocTypeTemplate.class,
					docTypeTemplateId
				);

				docType.setDocTypeTemplate(docTypeTemplate);
			}

		}

		return docType;
	}

	private DocType patchMapper(Mutiny.Session session, DocType docType, DocTypeDTO docTypeDTO) {

		mapper.patch(docType, docTypeDTO);

		if (docTypeDTO instanceof DocTypeWithTemplateDTO docTypeWithTemplateDTO) {

			// set docTypeTemplate only when docTypeTemplateId is not null
			var docTypeTemplateId = docTypeWithTemplateDTO.getDocTypeTemplateId();
			if (docTypeTemplateId != null) {
				var docTypeTemplate = session.getReference(
					DocTypeTemplate.class,
					docTypeTemplateId
				);

				docType.setDocTypeTemplate(docTypeTemplate);
			}


		}

		return docType;
	}

	private DocType updateMapper(Mutiny.Session session, DocType docType, DocTypeDTO docTypeDTO) {

		mapper.update(docType, docTypeDTO);

		if (docTypeDTO instanceof DocTypeWithTemplateDTO docTypeWithTemplateDTO) {

			// always set docTypeTemplate
			DocTypeTemplate docTypeTemplate = null;
			if (docTypeWithTemplateDTO.getDocTypeTemplateId() != null) {
				docTypeTemplate = session.getReference(
					DocTypeTemplate.class,
					docTypeWithTemplateDTO.getDocTypeTemplateId()
				);
			}

			docType.setDocTypeTemplate(docTypeTemplate);
		}

		return docType;
	}

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	DocTypeTemplateService docTypeTemplateService;

	@Inject
	DocTypeFieldMapper docTypeFieldMapper;

	@Override
	public Class<DocType> getEntityClass() {
		return DocType.class;
	}

}
