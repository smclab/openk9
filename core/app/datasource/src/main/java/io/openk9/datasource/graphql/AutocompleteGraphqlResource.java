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

import java.util.List;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.Autocomplete;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.base.AutocompleteDTO;
import io.openk9.datasource.service.AutocompleteService;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class AutocompleteGraphqlResource {

	@Inject
	AutocompleteService autocompleteService;


	@Description("""
		Creates or updates an Autocomplete configuration.
		
		This mutation handles both creation and updates/patch of Autocomplete configurations based on the provided ID.
		If no ID is provided, a new Autocomplete is created. If an ID is provided, the existing Autocomplete
		is either fully updated or partially patched based on the patch parameter.
		
		Arguments:
		- `id` (ID): The ID of the Autocomplete to update. If null, creates a new Autocomplete.
		- `AutocompleteDTO` (autocompleteDTO!): The Autocomplete data to create or update.
		- `patch` (Boolean): If true, performs a partial update (patch). If false, performs a full update. Defaults to false.
		
		Returns:
		- A Response containing the created or updated Autocomplete configuration.
		""")
	@Mutation
	public Uni<Response<Autocomplete>> autocomplete(
		@Id Long id, AutocompleteDTO autocompleteDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createAutocomplete(autocompleteDTO);
		}
		else {
			return patch
				? patchAutocomplete(id, autocompleteDTO)
				: updateAutocomplete(id, autocompleteDTO);
		}
	}

	@Description("""
		Deletes an Autocomplete configuration by its ID.
		
		Arguments:
		- `id` (ID!): The ID of the Autocomplete to delete.
		
		Returns:
		- The deleted Autocomplete configuration.
		""")
	@Mutation
	public Uni<Autocomplete> deleteAutocomplete(@Id long id) {
		return autocompleteService.deleteById(id);
	}

	@Description("""
		Retrieves all the DocTypeFields associated with an Autocomplete configuration.
		
		This field resolver fetches all the document type fields linked to the given Autocomplete.
		
		Returns:
		- The Set with all the associated DocTypeField, or null if not found.
		""")
	public Uni<Connection<DocTypeField>> fields(
			@Source Autocomplete autocomplete,
			@Description("fetching only nodes after this node (exclusive)") String after,
			@Description("fetching only nodes before this node (exclusive)") String before,
			@Description("fetching only the first certain number of nodes") Integer first,
			@Description("fetching only the last certain number of nodes") Integer last,
			String searchText, Set<SortBy> sortByList,
			@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {

		return autocompleteService.getDocTypeFieldConnection(
			autocomplete.getId(), after, before, first, last, searchText, sortByList, notEqual);
	}

	@Description("""
		Retrieves an Autocomplete configuration by its ID.
		
		Arguments:
		- `id` (ID!): The ID of the Autocomplete to retrieve.
		
		Returns:
		- The requested Autocomplete configuration, or null if not found.
		""")
	@Query
	public Uni<Autocomplete> getAutocomplete(@Id long id) {
		return autocompleteService.findById(id);
	}

	@Description("""
		Retrieves a paginated connection of Autocomplete configurations with optional filtering and sorting.
		
		This query supports cursor-based pagination following the Relay specification, allowing forward
		and backward traversal through the result set. Results can be filtered by search text and sorted
		by multiple criteria.
		
		Arguments:
		- `after` (String): Cursor for forward pagination - fetches nodes after this cursor (exclusive).
		- `before` (String): Cursor for backward pagination - fetches nodes before this cursor (exclusive).
		- `first` (Integer): Limits the number of nodes returned from the start.
		- `last` (Integer): Limits the number of nodes returned from the end.
		- `searchText` (String): Optional text to filter Autocomplete configurations.
		- `sortByList` (Set<SortBy>): Optional set of sorting criteria.
		
		Returns:
		- A Connection containing the requested Autocomplete configurations with pagination info.
		""")
	@Query
	public Uni<Connection<Autocomplete>> getAutocompletes(
			@Description("fetching only nodes after this node (exclusive)") String after,
			@Description("fetching only nodes before this node (exclusive)") String before,
			@Description("fetching only the first certain number of nodes") Integer first,
			@Description("fetching only the last certain number of nodes") Integer last,
			String searchText, Set<SortBy> sortByList) {

		return autocompleteService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Description("""
		Retrieves all Autocomplete configurations that are not bound to a specific Bucket.
		
		This query returns Autocomplete configurations that are available to be bound to the specified Bucket.
		
		Arguments:
		- `bucketId` (ID!): The ID of the Bucket to check for unbound Autocompletes.
		
		Returns:
		- A list of unbound Autocomplete configurations available for the specified Bucket.
		""")
	@Query
	public Uni<List<Autocomplete>> getUnboundAutocompleteByBucket(long bucketId) {
		return autocompleteService.findUnboundAutocompleteByBucket(bucketId);
	}

	private Uni<Response<Autocomplete>> createAutocomplete(
		AutocompleteDTO autocompleteDTO) {
		return autocompleteService.getValidator().create(autocompleteDTO);
	}

	private Uni<Response<Autocomplete>> patchAutocomplete(
		Long id, AutocompleteDTO autocompleteDTO) {
		return autocompleteService.getValidator().patch(id, autocompleteDTO);
	}

	private Uni<Response<Autocomplete>> updateAutocomplete(
		Long id, AutocompleteDTO autocompleteDTO) {
		return autocompleteService.getValidator().update(id, autocompleteDTO);
	}
}
