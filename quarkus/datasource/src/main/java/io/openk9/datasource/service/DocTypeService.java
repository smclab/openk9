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

import io.openk9.datasource.graphql.util.relay.Connection;
import io.openk9.datasource.mapper.DocTypeFieldMapper;
import io.openk9.datasource.mapper.DocTypeMapper;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.dto.DocTypeDTO;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.FlushMode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class DocTypeService extends BaseK9EntityService<DocType, DocTypeDTO> {
	DocTypeService(DocTypeMapper mapper) {
		this.mapper = mapper;
	}

	public Uni<Connection<DocTypeField>> getDocTypeFieldsConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {
		return findJoinConnection(
			id, DocType_.DOC_TYPE_FIELDS, DocTypeField.class,
			docTypeFieldService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
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

	public Uni<List<DocTypeField>> getDocTypeFields(DocType docType) {
		return withTransaction(s -> Mutiny2.fetch(s, docType.getDocTypeFields()));
	}

	public Uni<Tuple2<DocType, DocTypeField>> addDocTypeField(long id, DocTypeFieldDTO docTypeFieldDTO) {

		DocTypeField docTypeField =
			docTypeFieldMapper.create(docTypeFieldDTO);

		return withTransaction((s) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(docType -> Mutiny2.fetch(s, docType.getDocTypeFields()).flatMap(docTypeFields -> {
				if (docType.addDocTypeField(docTypeFields, docTypeField)) {
					return persist(docType)
						.map(dt -> Tuple2.of(dt, docTypeField));
				}
				return Uni.createFrom().nullItem();
			})));
	}

	public Uni<Tuple2<DocType, Long>> removeDocTypeField(long id, long docTypeFieldId) {
		return withTransaction((s) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(docType -> Mutiny2.fetch(s, docType.getDocTypeFields()).flatMap(docTypeFields -> {
				if (docType.removeDocTypeField(docTypeFields, docTypeFieldId)) {
					return persist(docType)
						.map(dt -> Tuple2.of(dt, docTypeFieldId));
				}
				return Uni.createFrom().nullItem();
			})));
	}

	public Uni<List<DocTypeField>> getDocTypeFieldsByName(String docTypeName) {
		return withStatelessTransaction((s) -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<DocTypeField> cq = cb.createQuery(DocTypeField.class);
			Root<DocType> root = cq.from(DocType.class);
			cq.select(root.join(DocType_.docTypeFields));
			cq.where(cb.equal(root.get(DocType_.name), docTypeName));
			return s.createQuery(cq).getResultList();
		});
	}

	public Uni<DocType> findByName(String name) {
		return withStatelessTransaction((s) -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<DocType> cq = cb.createQuery(DocType.class);
			Root<DocType> root = cq.from(DocType.class);
			cq.where(cb.equal(root.get(DocType_.name), name));
			return s.createQuery(cq)
				.setFlushMode(FlushMode.MANUAL)
				.getSingleResultOrNull();
		});
	}

	public Uni<List<DocType>> getDocTypeListByNames(String[] docTypeNames) {
		return withTransaction(s -> {

			CriteriaBuilder cb = em.getCriteriaBuilder();

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
		return withTransaction(s -> {

			CriteriaBuilder cb = em.getCriteriaBuilder();

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

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	DocTypeFieldMapper docTypeFieldMapper;

	@Override
	public Class<DocType> getEntityClass() {
		return DocType.class;
	}

}
