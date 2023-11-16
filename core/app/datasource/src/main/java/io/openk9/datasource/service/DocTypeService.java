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
import io.openk9.datasource.mapper.DocTypeFieldMapper;
import io.openk9.datasource.mapper.DocTypeMapper;
import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.AclMapping_;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.dto.DocTypeDTO;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.FlushMode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Set;

;

@ApplicationScoped
public class DocTypeService extends BaseK9EntityService<DocType, DocTypeDTO> {
	DocTypeService(DocTypeMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {DocType_.NAME, DocType_.DESCRIPTION};
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

		DocTypeField docTypeField =
			docTypeFieldMapper.create(docTypeFieldDTO);

		return sessionFactory.withTransaction((s) -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(docType -> s.fetch(docType.getDocTypeFields()).flatMap(docTypeFields -> {
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

	public Uni<List<DocType>> getDocTypeListByNames(String[] docTypeNames) {
		return sessionFactory.withTransaction(s -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			Class<DocType> entityClass = getEntityClass();

			CriteriaQuery<DocType> query = cb.createQuery(entityClass);

			Root<DocType> from = query.from(entityClass);

			query.where(from.get(DocType_.name).in(List.of(docTypeNames)));

			return s
				.createQuery(query)
				.getResultList();

		});
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

	public Uni<Collection<DocType>> getDocTypesAndDocTypeFields(Collection<Long> ids) {
		return findByIds(Set.copyOf(ids)).chain(dts -> docTypeFieldService.expandDocTypes(dts));
	}

	public Uni<Collection<DocType>> getDocTypesAndDocTypeFieldsByNames(Collection<String> docTypeNames) {
		return getDocTypeListByNames(docTypeNames.toArray(String[]::new))
			.chain(dts -> docTypeFieldService.expandDocTypes(dts));
	}

	@Override
	public Uni<DocType> deleteById(long entityId) {
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

		// dereference aclMappings
		CriteriaUpdate<AclMapping> updateAclMapping = cb.createCriteriaUpdate(AclMapping.class);
		Root<AclMapping> updateAclMappingFrom = updateAclMapping.from(AclMapping.class);
		Join<AclMapping, DocTypeField> updateAclMappingJoin =
			updateAclMappingFrom.join(AclMapping_.docTypeField);
		updateAclMapping.where(
			cb.equal(updateAclMappingJoin.get(DocTypeField_.docType), entityId));
		updateAclMapping.set(
			updateAclMappingFrom.get(AclMapping_.docTypeField), cb.nullLiteral(DocTypeField.class));

		// dereference parents and analyzer
		CriteriaUpdate<DocTypeField> updateDocTypeField =
			cb.createCriteriaUpdate(DocTypeField.class);
		Root<DocTypeField> updateDocTypeFieldFrom = updateDocTypeField.from(DocTypeField.class);
		updateDocTypeField.where(
			cb.equal(updateDocTypeFieldFrom.get(DocTypeField_.docType), entityId));
		updateDocTypeField.set(
			updateDocTypeFieldFrom.get(DocTypeField_.analyzer), cb.nullLiteral(Analyzer.class));
		updateDocTypeField.set(
			updateDocTypeFieldFrom.get(DocTypeField_.parentDocTypeField),
			cb.nullLiteral(DocTypeField.class));

		// delete docTypeFields
		CriteriaDelete<DocTypeField> deleteDocTypeFields =
			cb.createCriteriaDelete(DocTypeField.class);
		Root<DocTypeField> deleteFrom = deleteDocTypeFields.from(DocTypeField.class);
		deleteDocTypeFields.where(cb.equal(deleteFrom.get(DocTypeField_.docType), entityId));

		// dereference docTypeTemplate
		CriteriaUpdate<DocType> updateDocType = cb.createCriteriaUpdate(DocType.class);
		Root<DocType> docTypeFrom = updateDocType.from(DocType.class);
		updateDocType.where(cb.equal(docTypeFrom.get(DocType_.id), entityId));
		updateDocType.set(DocType_.docTypeTemplate, cb.nullLiteral(DocTypeTemplate.class));

		return sessionFactory.withTransaction((s, t) -> findById(s, entityId)
			.call(docType -> s.createQuery(updateAclMapping).executeUpdate())
			.call(docType -> s.createQuery(updateDocTypeField).executeUpdate())
			.call(docType -> s.createQuery(deleteDocTypeFields).executeUpdate())
			.call(docType -> s.createQuery(updateDocType).executeUpdate())
			.call(s::remove)
		);
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
