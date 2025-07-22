/*
 * Copyright (C) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.Autocorrection;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.base.AutocorrectionDTO;
import io.openk9.datasource.service.AutocorrectionService;
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

import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class AutocorrectionGraphqlResource {

	@Inject
	AutocorrectionService autocorrectionService;

	@Mutation
	public Uni<Response<Autocorrection>> autocorrection(
			@Id Long id, AutocorrectionDTO autocorrectionDTO,
			@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createAutocorrection(autocorrectionDTO);
		}
		else {
			return patch
				? patchAutocorrection(id, autocorrectionDTO)
				: updateAutocorrection(id, autocorrectionDTO);
		}
	}

	@Mutation
	public Uni<Autocorrection> deleteAutocorrection(@Id long autocorrectionId) {
		return autocorrectionService.deleteById(autocorrectionId);
	}

	@Query
	public Uni<Autocorrection> getAutocorrection(@Id long id) {
		return autocorrectionService.findById(id);
	}

	public Uni<DocTypeField> getAutocorrectionDocTypeField(@Source Autocorrection autocorrection) {
		return autocorrectionService.getAutocorrectionDocTypeField(autocorrection.getId());
	}

	@Query
	public Uni<Connection<Autocorrection>> getAutocorrections(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return autocorrectionService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	private Uni<Response<Autocorrection>> createAutocorrection(AutocorrectionDTO autocorrectionDTO) {
		return autocorrectionService.getValidator().create(autocorrectionDTO);
	}

	private Uni<Response<Autocorrection>> patchAutocorrection(Long id, AutocorrectionDTO autocorrectionDTO) {
		return autocorrectionService.getValidator().patch(id, autocorrectionDTO);
	}

	private Uni<Response<Autocorrection>> updateAutocorrection(Long id, AutocorrectionDTO autocorrectionDTO) {
		return autocorrectionService.getValidator().update(id, autocorrectionDTO);
	}
}
