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

import graphql.relay.Connection;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.dto.TenantDTO;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.TenantService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.validation.Response;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class TenantGraphqlResource {

	@Query
	public Uni<Connection<Tenant>> getTenants(
		String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList) {
		return tenantService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	public Uni<Page<Datasource>> datasources(
		@Source Tenant tenant, Pageable pageable, String searchText) {
		return tenantService.getDatasources(
			tenant.getId(),
			pageable == null ? Pageable.DEFAULT : pageable, searchText);
	}

	public Uni<Page<SuggestionCategory>> suggestionCategories(
		@Source Tenant tenant, Pageable pageable, String searchText) {
		return tenantService.getSuggestionCategories(
			tenant.getId(),
			pageable == null ? Pageable.DEFAULT : pageable, searchText);
	}

	@Query
	public Uni<Tenant> getTenant(long id) {
		return tenantService.findById(id);
	}

	public Uni<Response<Tenant>> patchTenant(long id, TenantDTO tenantDTO) {
		return tenantService.getValidator().patch(id, tenantDTO);
	}

	public Uni<Response<Tenant>> updateTenant(long id, TenantDTO tenantDTO) {
		return tenantService.getValidator().update(id, tenantDTO);
	}

	public Uni<Response<Tenant>> createTenant(TenantDTO tenantDTO) {
		return tenantService.getValidator().create(tenantDTO);
	}

	@Mutation
	public Uni<Response<Tenant>> tenant(
		Long id, TenantDTO tenantDTO, @DefaultValue("false") boolean patch) {

		if (id == null) {
			return createTenant(tenantDTO);
		} else {
			return patch
				? patchTenant(id, tenantDTO)
				: updateTenant(id, tenantDTO);
		}

	}

	@Mutation
	public Uni<Tenant> deleteTenant(long tenantId) {
		return tenantService.deleteById(tenantId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, Datasource>> addDatasourceToTenant(long tenantId, long datasourceId) {
		return tenantService.addDatasource(tenantId, datasourceId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, Datasource>> removeDatasourceFromTenant(long tenantId, long datasourceId) {
		return tenantService.removeDatasource(tenantId, datasourceId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, SuggestionCategory>> addSuggestionCategoryToTenant(long tenantId, long suggestionCategoryId) {
		return tenantService.addSuggestionCategory(tenantId, suggestionCategoryId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, SuggestionCategory>> removeSuggestionCategoryFromTenant(long tenantId, long suggestionCategoryId) {
		return tenantService.removeSuggestionCategory(tenantId, suggestionCategoryId);
	}

	@Subscription
	public Multi<Tenant> tenantCreated() {
		return tenantService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Tenant> tenantDeleted() {
		return tenantService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Tenant> tenantUpdated() {
		return tenantService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	TenantService tenantService;

}