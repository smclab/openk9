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
import io.openk9.datasource.mapper.AnnotatorMapper;
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.Annotator_;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.AnnotatorDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

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

	public Uni<Set<Annotator.AnnotatorExtraParam>> getExtraParams(Annotator annotator) {
		return sessionFactory.withTransaction((s, t) -> s
				.merge(annotator)
				.flatMap(merged -> s.fetch(merged.getExtraParams())))
			.map(Annotator::getExtraParamsSet);
	}

	public Uni<Annotator> addExtraParam(long id, String key, String value) {
		return getSessionFactory()
			.withTransaction(s ->
				findById(s, id)
					.flatMap(annotator -> fetchExtraParams(s, annotator))
					.flatMap(annotator -> {
						annotator.addExtraParam(key, value);
						return persist(s, annotator);
					})
			);
	}

	public Uni<Annotator> removeExtraParam(int id, String key) {
		return getSessionFactory()
			.withTransaction(s ->
				findById(s, id)
					.flatMap(annotator -> fetchExtraParams(s, annotator))
					.flatMap(annotator -> {
						annotator.removeExtraParam(key);
						return persist(s, annotator);
					})
			);
	}

	private static Uni<Annotator> fetchExtraParams(Mutiny.Session s, Annotator annotator) {
		return s
			.fetch(annotator.getExtraParams())
			.flatMap(extraParams -> {
				annotator.setExtraParams(extraParams);
				return Uni.createFrom().item(annotator);
			});
	}

	@Inject
	DocTypeFieldService docTypeFieldService;

}
