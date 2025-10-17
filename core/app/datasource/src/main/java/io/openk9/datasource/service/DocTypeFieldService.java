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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.openk9.datasource.model.Autocorrection;
import io.openk9.datasource.model.FieldType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.DocTypeFieldMapper;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.request.DocTypeFieldWithAnalyzerDTO;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class DocTypeFieldService extends BaseK9EntityService<DocTypeField, DocTypeFieldDTO> {
	 DocTypeFieldService(DocTypeFieldMapper mapper) {
		 this.mapper = mapper;
	}

	@Override
	public Uni<DocTypeField> create(DocTypeFieldDTO dto) {
		return sessionFactory.withTransaction((session, transaction) ->
			create(session, dto));
	}

	@Override
	public Uni<DocTypeField> create(Mutiny.Session s, DocTypeFieldDTO dto) {
		var entity = createTransient(s, dto);

		return super.create(s, entity);
	}

	/**
	 * Creates a transient DocTypeField entity from the provided DTO without persisting it to the database.
	 * <p>
	 * This method handles the mapping of a DocTypeFieldDTO to a DocTypeField entity. If the DTO is an instance of
	 * DocTypeFieldWithAnalyzerDTO and contains a non-null analyzerId, it additionally sets the associated Analyzer
	 * by retrieving a reference to it from the session.
	 * <p>
	 * The created entity is not persisted to the database and remains in a transient state, making this method
	 * suitable for use within transaction contexts like the {@link DocTypeService#addDocTypeField(long, DocTypeFieldDTO)} method, where the DocTypeField is
	 * attached to a DocType entity that handles the persistence.
	 *
	 * @param session The Mutiny.Session used for retrieving entity references
	 * @param dto     The data transfer object containing the field information. If this is an instance of
	 *                DocTypeFieldWithAnalyzerDTO with a non-null analyzerId, the analyzer reference will be set
	 * @return A newly created transient DocTypeField entity with all properties set according to the DTO
	 * @see DocTypeFieldDTO
	 * @see DocTypeFieldWithAnalyzerDTO
	 * @see Analyzer
	 */
	public DocTypeField createTransient(Mutiny.Session session, DocTypeFieldDTO dto) {
		var docTypeField = mapper.create(dto);

		if (dto instanceof DocTypeFieldWithAnalyzerDTO withAnalyzerDTO) {

			// only when analyzerId is not null
			var analyzerId = withAnalyzerDTO.getAnalyzerId();
			if (analyzerId != null) {
				var analyzer = session.getReference(Analyzer.class, analyzerId);
				docTypeField.setAnalyzer(analyzer);
			}
		}

		return docTypeField;
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
		return sessionFactory.withTransaction(s -> s
			.merge(docTypeField)
			.flatMap(merged -> s.fetch(merged.getParentDocTypeField()))
		);
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

	public Uni<Collection<DocType>> expandDocTypes(
		Mutiny.Session session, Collection<DocType> docTypes) {

		 if (docTypes != null && !docTypes.isEmpty()) {
			 Set<Uni<Set<DocTypeField>>> docTypeField = new LinkedHashSet<>();

			 for (DocType docType : docTypes) {
				 docTypeField.add(session.fetch(docType.getDocTypeFields()));
			 }

			 return Uni.combine()
				 .all()
				 .unis(docTypeField)
				 .usingConcurrencyOf(1)
				 .collectFailures()
				 .with(e -> (List<Set<DocTypeField>>) e)
				 .flatMap(sets -> loadAndExpandDocTypeFields(session, sets))
				 .replaceWith(docTypes);
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

	/**
	 * Retrieves a list of unbound DocTypeField entities that are boundable for autocorrection.
	 *
	 * <p>This method finds all DocTypeField that satisfy the following criteria:
	 * <ul>
	 *   <li>Are not already bound to the specified autocorrection</li>
	 *   <li>Have a field type that supports text-based autocorrection (TEXT, KEYWORD, CONSTANT_KEYWORD, or ANNOTATED_TEXT)</li>
	 * </ul>
	 *
	 * @param autocorrectionId the ID of the autocorrection used to filter already-bound fields
	 * @return a {@link Uni} that emits a list of unbound DocTypeField entities matching the criteria.
	 *         The list will be empty if no unbound fields are found.
	 *
	 * @see DocTypeField
	 * @see Autocorrection
	 * @see FieldType
	 */
	public Uni<List<DocTypeField>> findUnboundDocTypeFieldByAutocorrection(long autocorrectionId) {
		return sessionFactory.withTransaction(s -> {
			var queryString =
				"""
					SELECT dtf FROM DocTypeField dtf
					WHERE NOT EXISTS (
						SELECT 1
						FROM Autocorrection a
						WHERE a.autocorrectionDocTypeField.id = dtf.id
						AND a.id = (:autocorrectionId)
					)
					AND (
						dtf.fieldType = (:text) OR
						dtf.fieldType = (:constantKeyword) OR
						dtf.fieldType = (:annotatedText) OR
						dtf.fieldType = (:keyword)
					)
					""";

			return s.createQuery(queryString, DocTypeField.class)
				.setParameter("autocorrectionId", autocorrectionId)
				.setParameter("text", FieldType.TEXT)
				.setParameter("constantKeyword", FieldType.CONSTANT_KEYWORD)
				.setParameter("annotatedText", FieldType.ANNOTATED_TEXT)
				.setParameter("keyword", FieldType.KEYWORD)
				.getResultList();
		});
	}

	@Override
	public Uni<DocTypeField> patch(long id, DocTypeFieldDTO dto) {
		return sessionFactory.withTransaction((session, transaction) ->
			patch(session, id, dto));
	}

	@Override
	protected Uni<DocTypeField> patch(Mutiny.Session s, long id, DocTypeFieldDTO dto) {
		return findThenMapAndPersist(
			s, id, dto, (docTypeField, docTypeFieldDTO) ->
				patchMapper(s, docTypeField, docTypeFieldDTO)
		);
	}

	@Override
	public Uni<DocTypeField> update(long id, DocTypeFieldDTO dto) {
		return sessionFactory.withTransaction((session, transaction) ->
			update(session, id, dto));
	}

	@Override
	public Uni<DocTypeField> update(Mutiny.Session s, long id, DocTypeFieldDTO dto) {
		return findThenMapAndPersist(
			s, id, dto, (docTypeField, docTypeFieldDTO) ->
			updateMapper(s, docTypeField, docTypeFieldDTO));
	}

	private Uni<? extends Set<DocTypeField>> loadAndExpandDocTypeFields(Mutiny.Session s, List<Set<DocTypeField>> list) {

		List<Uni<Void>> loadedDTFs = new LinkedList<>();

		for (Set<DocTypeField> typeFields : list) {
			loadedDTFs.add(loadDocTypeField(s, typeFields));
		}

		return Uni.combine()
			.all()
			.unis(loadedDTFs)
			.usingConcurrencyOf(1)
			.collectFailures()
			.discardItems()
			.chain(() -> {

				List<Uni<Set<DocTypeField>>> inner = new LinkedList<>();

				for (Set<DocTypeField> typeFields : list) {
					inner.add(expandDocTypeFields(s, typeFields));
				}

				return Uni.combine()
					.all()
					.unis(inner)
					.usingConcurrencyOf(1)
					.collectFailures()
					.with(e -> {
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

		return Uni.combine()
			.all()
			.unis(subDocTypeFieldUnis)
			.usingConcurrencyOf(1)
			.collectFailures()
			.with(e -> (List<Set<DocTypeField>>) e)
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

		return Uni.combine()
			.all()
			.unis(unis)
			.usingConcurrencyOf(1)
			.collectFailures()
			.discardItems();
	}


	private DocTypeField patchMapper(
		Mutiny.Session session,
		DocTypeField docTypeField,
		DocTypeFieldDTO docTypeFieldDTO) {

		mapper.patch(docTypeField, docTypeFieldDTO);

		if (docTypeFieldDTO instanceof DocTypeFieldWithAnalyzerDTO withAnalyzerDTO) {

			// only when analyzerId is not null
			var analyzerId = withAnalyzerDTO.getAnalyzerId();
			if (analyzerId != null) {
				var analyzer = session.getReference(Analyzer.class, analyzerId);
				docTypeField.setAnalyzer(analyzer);
			}
		}

		return docTypeField;
	}

	private DocTypeField updateMapper(
		Mutiny.Session session,
		DocTypeField docTypeField,
		DocTypeFieldDTO docTypeFieldDTO) {

		mapper.update(docTypeField, docTypeFieldDTO);

		if (docTypeFieldDTO instanceof DocTypeFieldWithAnalyzerDTO withAnalyzerDTO) {

			// always set analyzer
			var analyzerId = withAnalyzerDTO.getAnalyzerId();
			Analyzer analyzer = null;
			if (analyzerId != null) {
				analyzer = session.getReference(Analyzer.class, analyzerId);
			}

			docTypeField.setAnalyzer(analyzer);
		}

		return docTypeField;
	}

	@Inject
	AnalyzerService _analyzerService;

}
