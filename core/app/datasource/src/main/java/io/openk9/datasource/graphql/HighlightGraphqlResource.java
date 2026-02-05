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

package io.openk9.datasource.graphql;

import io.openk9.common.util.web.Response;
import io.openk9.datasource.model.Highlight;
import io.openk9.datasource.model.dto.base.HighlightDTO;
import io.openk9.datasource.service.HighlightService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class HighlightGraphqlResource {

	@Inject
	HighlightService highlighterService;

	@Description("""
		Retrieves an Highlight configuration by its ID.
		""")
	@Query
	public Uni<Highlight> getHighlight(@Id long id) {
		return highlighterService.findById(id);
	}

	@Description("""
		Retrieves all Highlight configurations.
		""")
	@Query
	public Uni<List<Highlight>> getAllHighlights() {
		return highlighterService.findAll();
	}

	@Description("""
		Create a new Highlight configuration.
		""")
	@Mutation
	public Uni<Response<Highlight>> createHighlight(HighlightDTO highlightDTO) {
		return highlighterService.getValidator().create(highlightDTO);
	}

	@Description("""
		Update an existed Highlight configuration by its ID
		""")
	@Mutation
	public Uni<Response<Highlight>> updateHighlight(@Id long id,HighlightDTO highlightDTO) {
		return highlighterService.getValidator().update(id, highlightDTO);
	}

	@Description("""
		Delete an Highlight configuration by its ID.
		""")
	@Mutation
	public Uni<Highlight> deleteHighlight(@Id long id) {
		return highlighterService.deleteById(id);
	}

}
