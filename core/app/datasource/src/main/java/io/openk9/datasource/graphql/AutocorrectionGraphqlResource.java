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

import java.util.List;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class AutocorrectionGraphqlResource {

	@Inject
	AutocorrectionService autocorrectionService;


	@Description("""
		Creates or updates an Autocorrection configuration.
		
		This mutation handles both creation and updates/patch of Autocorrection configurations based on the provided ID.
		If no ID is provided, a new Autocorrection is created. If an ID is provided, the existing Autocorrection
		is either fully updated or partially patched based on the patch parameter.
		
		Arguments:
		- `id` (ID): The ID of the Autocorrection to update. If null, creates a new Autocorrection.
		- `autocorrectionDTO` (AutocorrectionDTO!): The Autocorrection data to create or update.
		- `patch` (Boolean): If true, performs a partial update (patch). If false, performs a full update. Defaults to false.
		
		Returns:
		- A Response containing the created or updated Autocorrection configuration.
		""")
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

	@Description("""
		Deletes an Autocorrection configuration by its ID.
		
		Arguments:
		- `id` (ID!): The ID of the Autocorrection to delete.
		
		Returns:
		- The deleted Autocorrection configuration.
		""")
	@Mutation
	public Uni<Autocorrection> deleteAutocorrection(@Id long id) {
		return autocorrectionService.deleteById(id);
	}

	@Description("""
		Retrieves an Autocorrection configuration by its ID.
		
		Arguments:
		- `id` (ID!): The ID of the Autocorrection to retrieve.
		
		Returns:
		- The requested Autocorrection configuration, or null if not found.
		""")
	@Query
	public Uni<Autocorrection> getAutocorrection(@Id long id) {
		return autocorrectionService.findById(id);
	}

	@Description("""
		Retrieves the DocTypeField associated with an Autocorrection configuration.
		
		This field resolver fetches the document type field linked to the given Autocorrection.
		
		Returns:
		- The associated DocTypeField, or null if not found.
		""")
	public Uni<DocTypeField> getAutocorrectionDocTypeField(@Source Autocorrection autocorrection) {
		return autocorrectionService.getAutocorrectionDocTypeField(autocorrection.getId());
	}

	@Description("""
		Retrieves a paginated connection of Autocorrection configurations with optional filtering and sorting.
		
		This query supports cursor-based pagination following the Relay specification, allowing forward
		and backward traversal through the result set. Results can be filtered by search text and sorted
		by multiple criteria.
		
		Arguments:
		- `after` (String): Cursor for forward pagination - fetches nodes after this cursor (exclusive).
		- `before` (String): Cursor for backward pagination - fetches nodes before this cursor (exclusive).
		- `first` (Integer): Limits the number of nodes returned from the start.
		- `last` (Integer): Limits the number of nodes returned from the end.
		- `searchText` (String): Optional text to filter Autocorrection configurations.
		- `sortByList` (Set<SortBy>): Optional set of sorting criteria.
		
		Returns:
		- A Connection containing the requested Autocorrection configurations with pagination info.
		""")
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

	@Description("""
		Retrieves all Autocorrection configurations that are not bound to a specific Bucket.
		
		This query returns Autocorrection configurations that are available to be bound to the specified Bucket.
		
		Arguments:
		- `bucketId` (ID!): The ID of the Bucket to check for unbound Autocorrections.
		
		Returns:
		- A list of unbound Autocorrection configurations available for the specified Bucket.
		""")
	@Query
	public Uni<List<Autocorrection>> getUnboundAutocorrectionByBucket(long bucketId) {
		return autocorrectionService.findUnboundAutocorrectionByBucket(bucketId);
	}

	private Uni<Response<Autocorrection>> createAutocorrection(
			AutocorrectionDTO autocorrectionDTO) {
		return autocorrectionService.getValidator().create(autocorrectionDTO);
	}

	private Uni<Response<Autocorrection>> patchAutocorrection(
			Long id, AutocorrectionDTO autocorrectionDTO) {
		return autocorrectionService.getValidator().patch(id, autocorrectionDTO);
	}

	private Uni<Response<Autocorrection>> updateAutocorrection(
			Long id, AutocorrectionDTO autocorrectionDTO) {
		return autocorrectionService.getValidator().update(id, autocorrectionDTO);
	}
}
