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

import io.openk9.datasource.graphql.util.SortType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.resource.util.K9Column;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.service.PluginDriverService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class PluginDriverGraphqlResource {

	@Query
	public Uni<Page<PluginDriver>> getPluginDrivers(
		@Name("limit") @DefaultValue("20") int limit,
		@Name("offset") @DefaultValue("0") int offset,
		@Name("sortBy") @DefaultValue("createDate") K9Column sortBy,
		@Name("sortType") @DefaultValue("ASC") SortType sortType) {
		return pluginDriverService.findAllPaginated(
			limit, offset, sortBy.name(), sortType);
	}

	@Query
	public Uni<PluginDriver> getPluginDriver(long id) {
		return pluginDriverService.findById(id);
	}

	@Mutation
	public Uni<PluginDriver> patchPluginDriver(long id, PluginDriverDTO pluginDriverDTO) {
		return pluginDriverService.patch(id, pluginDriverDTO);
	}

	@Mutation
	public Uni<PluginDriver> updatePluginDriver(long id, PluginDriverDTO pluginDriverDTO) {
		return pluginDriverService.update(id, pluginDriverDTO);
	}

	@Mutation
	public Uni<PluginDriver> createPluginDriver(PluginDriverDTO pluginDriverDTO) {
		return pluginDriverService.persist(pluginDriverDTO);
	}

	@Mutation
	public Uni<PluginDriver> deletePluginDriver(long pluginDriverId) {
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