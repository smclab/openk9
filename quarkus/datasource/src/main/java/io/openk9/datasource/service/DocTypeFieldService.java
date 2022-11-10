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
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;


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

	public Uni<Tuple2<DocTypeField, Analyzer>> bindAnalyzer(long docTypeFieldId, long analyzerId) {
		return withTransaction((s, tr) -> findById(docTypeFieldId)
			.onItem()
			.ifNotNull()
			.transformToUni(docTypeField -> _analyzerService.findById(analyzerId)
				.onItem()
				.ifNotNull()
				.transformToUni(analyzer -> {
					docTypeField.setAnalyzer(analyzer);
					return persist(docTypeField).map(t -> Tuple2.of(t, analyzer));
				})));
	}

	public Uni<Tuple2<DocTypeField, Analyzer>> unbindAnalyzer(long docTypeFieldId) {
		return withTransaction((s, tr) -> findById(docTypeFieldId)
			.onItem()
			.ifNotNull()
			.transformToUni(docTypeField -> {
				docTypeField.setAnalyzer(null);
				return persist(docTypeField).map(t -> Tuple2.of(t, null));
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


	@Inject
	AnalyzerService _analyzerService;

}
