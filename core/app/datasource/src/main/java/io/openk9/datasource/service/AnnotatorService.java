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
import io.openk9.datasource.mapper.AnnotatorMapper;
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.Annotator_;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.AnnotatorDTO;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

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
		return withTransaction((s) -> findById(annotatorId)
			.onItem()
			.ifNotNull()
			.transformToUni(annotator -> docTypeFieldService.findById(docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> {
					annotator.setDocTypeField(docTypeField);
					return persist(annotator)
						.map(newAnnotator -> Tuple2.of(newAnnotator, docTypeField));
				})));
	}

	public Uni<Tuple2<Annotator, DocTypeField>> unbindDocTypeField(
		long annotatorId, long docTypeFieldId) {
		return withTransaction((s) -> findById(annotatorId)
			.onItem()
			.ifNotNull()
			.transformToUni(annotator -> docTypeFieldService.findById(docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> {
					annotator.setDocTypeField(null);
					return persist(annotator)
						.map(newAnnotator -> Tuple2.of(newAnnotator, docTypeField));
				})));
	}

	@Inject
	private DocTypeFieldService docTypeFieldService;

}
