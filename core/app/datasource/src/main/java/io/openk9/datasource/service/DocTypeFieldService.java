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
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class DocTypeFieldService extends BaseK9EntityService<DocTypeField, DocTypeFieldDTO> {
	 DocTypeFieldService(DocTypeFieldMapper mapper) {
		 this.mapper = mapper;
	}

	@Override
	public Class<DocTypeField> getEntityClass() {
		return DocTypeField.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {DocTypeField_.NAME, DocTypeField_.FIELD_TYPE};
	}

	public Uni<Analyzer> getAnalyzer(DocTypeField docTypeField) {
		return sessionFactory.withTransaction(
			s -> s.fetch(docTypeField.getAnalyzer()));
	}

	public Uni<Analyzer> getAnalyzer(long docTypeFieldId) {
		return getSessionFactory().withTransaction(s -> findById(s, docTypeFieldId)
			.flatMap(d -> s.fetch(d.getAnalyzer())));
	}

	public Uni<Tuple2<DocTypeField, Analyzer>> bindAnalyzer(long docTypeFieldId, long analyzerId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, docTypeFieldId)
			.onItem()
			.ifNotNull()
			.transformToUni(docTypeField -> _analyzerService.findById(s, analyzerId)
				.onItem()
				.ifNotNull()
				.transformToUni(analyzer -> {
					docTypeField.setAnalyzer(analyzer);
					return persist(s, docTypeField).map(t -> Tuple2.of(t, analyzer));
				})));
	}

	public Uni<Tuple2<DocTypeField, Analyzer>> unbindAnalyzer(long docTypeFieldId) {
		return sessionFactory.withTransaction((s, tr) -> findById(s, docTypeFieldId)
			.onItem()
			.ifNotNull()
			.transformToUni(docTypeField -> {
				docTypeField.setAnalyzer(null);
				return persist(s, docTypeField).map(t -> Tuple2.of(t, null));
			}));
	}

	public Uni<Connection<Analyzer>> getAnalyzersConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {
		return findJoinConnection(
			id, DocTypeField_.ANALYZER, Analyzer.class,
			_analyzerService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}

	public Uni<DocTypeField> getParent(DocTypeField docTypeField) {
		return sessionFactory.withTransaction(
			s -> s.fetch(docTypeField.getParentDocTypeField()));
	}

	public Uni<Connection<DocTypeField>> getSubDocTypeFields(
		DocTypeField docTypeField, String after, String before, Integer first,
		Integer last, String searchText, Set<SortBy> sortByList,
		boolean notEqual) {
		return findJoinConnection(
			docTypeField.getId(), DocTypeField_.SUB_DOC_TYPE_FIELDS,
			DocTypeField.class, getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}

	public Uni<DocTypeField> createSubField(
		long parentDocTypeFieldId, DocTypeFieldDTO docTypeFieldDTO) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, parentDocTypeFieldId)
			.onItem()
			.ifNotNull()
			.transformToUni(parentDocTypeField -> s
				.fetch(parentDocTypeField.getSubDocTypeFields())
				.onItem()
				.ifNotNull()
				.transformToUni(subList -> {

					DocTypeField docTypeField = mapper.create(docTypeFieldDTO);
					docTypeField.setParentDocTypeField(parentDocTypeField);
					docTypeField.setDocType(parentDocTypeField.getDocType());
					subList.add(docTypeField);
					return persist(s, docTypeField);
				})
			)
		);

	}

	public Uni<Collection<DocType>> expandDocTypes(Collection<DocType> docTypes) {

		 if (docTypes != null && !docTypes.isEmpty()) {
			 return sessionFactory.withTransaction(s -> {

				 Set<Uni<Set<DocTypeField>>> docTypeField = new LinkedHashSet<>();

				 for (DocType docType : docTypes) {
					 docTypeField.add(s.fetch(docType.getDocTypeFields()));
				 }

				 return Uni
					 .combine()
					 .all()
					 .unis(docTypeField)
					 .collectFailures()
					 .combinedWith(e -> (List<Set<DocTypeField>>) e)
					 .flatMap(sets -> loadAndExpandDocTypeFields(s, sets))
					 .replaceWith(docTypes);
			 });
		 }
		 else {
			 return Uni.createFrom().item(List.of());
		 }

	}

	public Uni<Connection<DocTypeField>> findConnection(
		long parentId, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList) {


		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();

		CriteriaQuery<DocTypeField> query =
			criteriaBuilder.createQuery(getEntityClass());

		Path<DocTypeField> root = query.from(getEntityClass());

		Path<DocTypeField> parentField = root.get(DocTypeField_.parentDocTypeField);

		return findConnection(
			query, root,
			parentId > 0
				? criteriaBuilder.equal(parentField.get(DocTypeField_.id), parentId)
				: criteriaBuilder.isNull(parentField),
			getSearchFields(),
			after, before, first, last, searchText, sortByList
		);
	 }


	private Uni<? extends Set<DocTypeField>> loadAndExpandDocTypeFields(Mutiny.Session s, List<Set<DocTypeField>> list) {

		List<Uni<Void>> loadedDTFs = new LinkedList<>();

		for (Set<DocTypeField> typeFields : list) {
			loadedDTFs.add(loadDocTypeField(s, typeFields));
		}

		return Uni
			.combine()
			.all()
			.unis(loadedDTFs)
			.collectFailures()
			.discardItems()
			.chain(() -> {

				List<Uni<Set<DocTypeField>>> inner = new LinkedList<>();

				for (Set<DocTypeField> typeFields : list) {
					inner.add(expandDocTypeFields(s, typeFields));
				}

				return Uni
					.combine()
					.all()
					.unis(inner)
					.collectFailures()
					.combinedWith(e -> {
						List<Set<DocTypeField>> expandInner = (List<Set<DocTypeField>>) e;
						return expandInner.stream()
							.flatMap(Collection::stream)
							.collect(Collectors.toSet());
					});

			});

	}

	private Uni<Set<DocTypeField>> expandDocTypeFields(
		Mutiny.Session s, Collection<DocTypeField> docTypeFields) {

		if (docTypeFields == null || docTypeFields.isEmpty()) {
			return Uni.createFrom().item(Set.of());
		}

		List<Uni<Set<DocTypeField>>> subDocTypeFieldUnis = new LinkedList<>();

		for (DocTypeField docTypeField : docTypeFields) {

			subDocTypeFieldUnis.add(
				s.fetch(docTypeField.getSubDocTypeFields()));

		}

		return Uni
			.combine()
			.all()
			.unis(subDocTypeFieldUnis)
			.collectFailures()
			.combinedWith(e -> (List<Set<DocTypeField>>)e)
			.flatMap(sets -> loadAndExpandDocTypeFields(s, sets));
	}

	private Uni<Void> loadDocTypeField(Mutiny.Session s, Set<DocTypeField> typeFields) {

		List<Uni<?>> unis = new ArrayList<>();

		for (DocTypeField typeField : typeFields) {
			Analyzer analyzer = typeField.getAnalyzer();
			if (analyzer != null) {
				unis.add(s
					.fetch(typeField.getAnalyzer())
					.flatMap(_analyzerService::load));
			}
			if (typeField.getAclMappings() != null) {
				unis.add(s.fetch(typeField.getAclMappings()));
			}

		}

		if (unis.isEmpty()) {
			return Uni.createFrom().voidItem();
		}

		return Uni.combine().all().unis(unis).collectFailures().discardItems();
	}


	@Inject
	AnalyzerService _analyzerService;

}
