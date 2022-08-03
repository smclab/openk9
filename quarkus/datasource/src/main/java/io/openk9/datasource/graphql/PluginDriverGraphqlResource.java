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

import io.openk9.datasource.graphql.util.relay.Connection;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.PluginDriverService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.validation.Response;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class PluginDriverGraphqlResource {

	@Query
	public Uni<Connection<PluginDriver>> getPluginDrivers(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return pluginDriverService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<PluginDriver> getPluginDriver(@Id long id) {
		return pluginDriverService.findById(id);
	}

	public Uni<Response<PluginDriver>> patchPluginDriver(@Id long id, PluginDriverDTO pluginDriverDTO) {
		return pluginDriverService.getValidator().patch(id, pluginDriverDTO);
	}

	public Uni<Response<PluginDriver>> updatePluginDriver(@Id long id, PluginDriverDTO pluginDriverDTO) {
		return pluginDriverService.getValidator().update(id, pluginDriverDTO);
	}

	public Uni<Response<PluginDriver>> createPluginDriver(PluginDriverDTO pluginDriverDTO) {
		return pluginDriverService.getValidator().create(pluginDriverDTO);
	}

	@Mutation
	public Uni<Response<PluginDriver>> pluginDriver(
		@Id Long id, PluginDriverDTO pluginDriverDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createPluginDriver(pluginDriverDTO);
		} else {
			return patch
				? patchPluginDriver(id, pluginDriverDTO)
				: updatePluginDriver(id, pluginDriverDTO);
		}

	}

	@Mutation
	public Uni<PluginDriver> deletePluginDriver(@Id long pluginDriverId) {
		return pluginDriverService.deleteById(pluginDriverId);
	}

	@Subscription
	public Multi<PluginDriver> pluginDriverCreated() {
		return pluginDriverService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<PluginDriver> pluginDriverDeleted() {
		return pluginDriverService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<PluginDriver> pluginDriverUpdated() {
		return pluginDriverService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	PluginDriverService pluginDriverService;

}