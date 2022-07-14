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

import io.openk9.datasource.graphql.util.Response;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.dto.TenantDTO;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.TenantService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class TenantGraphqlResource {

	@Query
	public Uni<Page<Tenant>> getTenants(Pageable pageable) {
		return tenantService.findAllPaginated(
			pageable == null ? Pageable.DEFAULT : pageable
		);
	}

	public Uni<Page<Datasource>> datasources(
		@Source Tenant tenant, Pageable pageable) {
		return tenantService.getDatasources(
			tenant.getId(),
			pageable == null ? Pageable.DEFAULT : pageable);
	}

	public Uni<Page<SuggestionCategory>> suggestionCategories(
		@Source Tenant tenant,
		Pageable pageable) {
		return tenantService.getSuggestionCategories(
			tenant.getId(),
			pageable == null ? Pageable.DEFAULT : pageable);
	}

	@Query
	public Uni<Tenant> getTenant(long id) {
		return tenantService.findById(id);
	}

	@Mutation
	public Uni<Tenant> patchTenant(long id, TenantDTO tenantDTO) {
		return tenantService.patch(id, tenantDTO);
	}

	@Mutation
	public Uni<Tenant> updateTenant(long id, TenantDTO tenantDTO) {
		return tenantService.update(id, tenantDTO);
	}

	@Mutation
	public Uni<Tenant> createTenant(TenantDTO tenantDTO) {
		return tenantService.persist(tenantDTO);
	}

	@Mutation
	public Uni<Tenant> deleteTenant(long tenantId) {
		return tenantService.deleteById(tenantId);
	}

	@Mutation
	public Uni<Response> addDatasourceToTenant(long tenantId, long datasourceId) {
		return tenantService.addDatasource(tenantId, datasourceId).replaceWith(
			() -> Response.of("Datasource added to tenant"));
	}

	@Mutation
	public Uni<Response> removeDatasourceFromTenant(long tenantId, long datasourceId) {
		return tenantService.removeDatasource(tenantId, datasourceId).replaceWith(
			() -> Response.of("Datasource removed from tenant"));
	}

	@Mutation
	public Uni<Response> addSuggestionCategoryToTenant(long tenantId, long suggestionCategoryId) {
		return tenantService.addSuggestionCategory(tenantId, suggestionCategoryId).replaceWith(
			() -> Response.of("Suggestion category added to tenant"));
	}

	@Mutation
	public Uni<Response> removeSuggestionCategoryFromTenant(long tenantId, long suggestionCategoryId) {
		return tenantService.removeSuggestionCategory(tenantId, suggestionCategoryId).replaceWith(
			() -> Response.of("Suggestion category removed from tenant"));
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