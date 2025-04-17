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

import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.AnnotatorMapper;
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.Annotator_;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.base.AnnotatorDTO;
import io.openk9.datasource.model.dto.request.AnnotatorWithDocTypeFieldDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

;

@ApplicationScoped
public class AnnotatorService extends BaseK9EntityService<Annotator, AnnotatorDTO> {
	 AnnotatorService(AnnotatorMapper mapper) {
		 this.mapper = mapper;
	}

	@Override
	public Class<Annotator> getEntityClass() {
		return Annotator.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {Annotator_.NAME, Annotator_.FUZINESS, Annotator_.TYPE, Annotator_.DESCRIPTION};
	}

	@Override
	public Uni<Annotator> create(AnnotatorDTO dto) {
		return sessionFactory.withTransaction((session, transaction) ->
			create(session, dto));
	}

	@Override
	public Uni<Annotator> create(Mutiny.Session session, AnnotatorDTO dto) {
		var annotator = createMapper(session, dto);

		return super.create(session, annotator);
	}

	public Uni<Connection<DocTypeField>> getDocTypeFieldsNotInAnnotator(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList) {
		return findJoinConnection(
			id, Annotator_.DOC_TYPE_FIELD, DocTypeField.class,
			docTypeFieldService.getSearchFields(), after, before, first, last,
			searchText, sortByList, true);
	}

	public Uni<Tuple2<Annotator, DocTypeField>> bindDocTypeField(
		long annotatorId, long docTypeFieldId) {
		return sessionFactory.withTransaction((s) -> findById(s, annotatorId)
			.onItem()
			.ifNotNull()
			.transformToUni(annotator -> docTypeFieldService.findById(s, docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> {
					annotator.setDocTypeField(docTypeField);
					return persist(s, annotator)
						.map(newAnnotator -> Tuple2.of(newAnnotator, docTypeField));
				})));
	}

	public Uni<Tuple2<Annotator, DocTypeField>> unbindDocTypeField(
		long annotatorId, long docTypeFieldId) {
		return sessionFactory.withTransaction((s) -> findById(s, annotatorId)
			.onItem()
			.ifNotNull()
			.transformToUni(annotator -> docTypeFieldService.findById(s, docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> {
					annotator.setDocTypeField(null);
					return persist(s, annotator)
						.map(newAnnotator -> Tuple2.of(newAnnotator, docTypeField));
				})));
	}

	@Override
	public Uni<Annotator> patch(long id, AnnotatorDTO dto) {
		return sessionFactory.withTransaction((session, transaction) ->
			patch(session, id, dto));
	}

	@Override
	protected Uni<Annotator> patch(Mutiny.Session session, long id, AnnotatorDTO dto) {
		return findThenMapAndPersist(
			session,
			id,
			dto,
			(annotator, annotatorDTO) -> patchMapper(
				session, annotator, annotatorDTO)
		);
	}

	@Override
	public Uni<Annotator> update(long id, AnnotatorDTO dto) {
		return sessionFactory.withTransaction((session, transaction) ->
			update(session, id, dto));
	}

	@Override
	public Uni<Annotator> update(Mutiny.Session session, long id, AnnotatorDTO dto) {
		return findThenMapAndPersist(
			session,
			id,
			dto,
			(annotator, annotatorDTO) -> updateMapper(session, annotator, annotatorDTO)
		);
	}

	private Annotator createMapper(Mutiny.Session session, AnnotatorDTO dto) {
		var annotator = mapper.create(dto);

		if (dto instanceof AnnotatorWithDocTypeFieldDTO withDocTypeFieldDTO) {

			// set docTypeField only when is present
			var docTypeFieldId = withDocTypeFieldDTO.getDocTypeFieldId();
			if (docTypeFieldId != null) {
				var docTypeField = session.getReference(DocTypeField.class, docTypeFieldId);
				annotator.setDocTypeField(docTypeField);
			}

		}

		return annotator;
	}

	private Annotator patchMapper(
		Mutiny.Session session, Annotator annotator,
		AnnotatorDTO annotatorDTO) {

		mapper.patch(annotator, annotatorDTO);

		if (annotatorDTO instanceof AnnotatorWithDocTypeFieldDTO withDocTypeFieldDTO) {

			// set docTypeField only when is present
			var docTypeFieldId = withDocTypeFieldDTO.getDocTypeFieldId();
			if (docTypeFieldId != null) {
				var docTypeField = session.getReference(DocTypeField.class, docTypeFieldId);
				annotator.setDocTypeField(docTypeField);
			}
		}

		return annotator;
	}

	private Annotator updateMapper(
		Mutiny.Session session, Annotator annotator,
		AnnotatorDTO annotatorDTO) {

		mapper.update(annotator, annotatorDTO);

		if (annotatorDTO instanceof AnnotatorWithDocTypeFieldDTO withDocTypeFieldDTO) {

			// always set docTypeField
			DocTypeField docTypeField = null;
			var docTypeFieldId = withDocTypeFieldDTO.getDocTypeFieldId();
			if (docTypeFieldId != null) {
				docTypeField = session.getReference(DocTypeField.class, docTypeFieldId);
			}

			annotator.setDocTypeField(docTypeField);
		}

		return annotator;
	}

	@Inject
	DocTypeFieldService docTypeFieldService;

}
