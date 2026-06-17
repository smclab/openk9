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
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.Highlight;
import io.openk9.datasource.model.dto.base.HighlightDTO;
import io.openk9.datasource.service.HighlightService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class HighlightGraphqlResource {

	@Inject
	HighlightService highlightService;

	@Description("Retrieves an Highlight by its ID")
	@Query
	public Uni<Highlight> getHighlight(@Id long id) {
		return highlightService.findById(id);
	}

	@Description("Retrieves all Highlights")
	@Query
	public Uni<List<Highlight>> getAllHighlights() {
		return highlightService.findAll();
	}

	public Uni<Set<DocTypeField>> fields(@Source Highlight highlight) {
		return highlightService.getFields(highlight.getId());
	}

	public Uni<Set<DocTypeField>> matchedFields(@Source Highlight highlight) {
		return highlightService.getMatchedFields(highlight.getId());
	}

	@Description("Create a new Highlight")
	@Mutation
	public Uni<Response<Highlight>> createHighlight(HighlightDTO highlightDTO) {
		return highlightService.getValidator().create(highlightDTO);
	}

	@Description("""
		Update or patch an Highlight entity based on the provided input.
		
		Arguments:
		- `id` (Long): The ID of the Highlight to update.
		- `highlightDTO` (HighlightDTO!): The input object with data to update.
		- `patch` (Boolean): Whether to perform a partial update. Defaults to false.
		
		Returns:
		- The Highlight entity updated.
		"""
	)
	@Mutation
	public Uni<Response<Highlight>> updateHighlight(
		@Id long id,
		HighlightDTO highlightDTO,
		@DefaultValue("false") boolean patch) {

		return patch
			? patchHighlight(id, highlightDTO)
			: updateHighlight(id, highlightDTO);
	}

	@Description("Delete an Highlight by its ID")
	@Mutation
	public Uni<Highlight> deleteHighlight(@Id long id) {
		return highlightService.deleteById(id);
	}

	private Uni<Response<Highlight>> updateHighlight(long id, HighlightDTO highlightDTO) {
		return highlightService.getValidator().update(id, highlightDTO);
	}

	private Uni<Response<Highlight>> patchHighlight(long id, HighlightDTO highlightDTO) {
		return highlightService.getValidator().patch(id, highlightDTO);
	}

}
