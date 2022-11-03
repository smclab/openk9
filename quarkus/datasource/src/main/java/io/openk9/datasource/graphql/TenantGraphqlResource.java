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
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.dto.TenantDTO;
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
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
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
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return tenantService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	public Uni<Connection<Datasource>> datasources(
		@Source Tenant tenant,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {

		return tenantService.getDatasourcesConnection(
			tenant.getId(), after, before, first, last, searchText, sortByList,
			notEqual);
	}

	public Uni<Connection<SuggestionCategory>> suggestionCategories(
		@Source Tenant tenant,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {

		return tenantService.getSuggestionCategoriesConnection(
			tenant.getId(), after, before, first, last, searchText, sortByList,
			notEqual);
	}

	public Uni<Connection<Tab>> tabs(
		@Source Tenant tenant,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@DefaultValue("false") boolean notEqual) {

		return tenantService.getTabs(
			tenant.getId(), after, before, first, last, searchText,
			sortByList, notEqual);
	}

	public Uni<QueryAnalysis> queryAnalysis(@Source Tenant tenant) {
		return tenantService.getQueryAnalysis(tenant.getId());
	}

	@Query
	public Uni<Tenant> getTenant(@Id long id) {
		return tenantService.findById(id);
	}

	public Uni<Response<Tenant>> patchTenant(@Id long id, TenantDTO tenantDTO) {
		return tenantService.getValidator().patch(id, tenantDTO);
	}

	public Uni<Response<Tenant>> updateTenant(@Id long id, TenantDTO tenantDTO) {
		return tenantService.getValidator().update(id, tenantDTO);
	}

	public Uni<Response<Tenant>> createTenant(TenantDTO tenantDTO) {
		return tenantService.getValidator().create(tenantDTO);
	}

	@Mutation
	public Uni<Tuple2<Tenant, Tab>> addTabToTenant(
		@Id long id, @Id long tabId) {
		return tenantService.addTabToTenant(id, tabId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, Tab>> removeTabFromTenant(
		@Id long id, @Id long tabId) {
		return tenantService.removeTabFromTenant(id, tabId);
	}

	@Mutation
	public Uni<Response<Tenant>> tenant(
		@Id Long id, TenantDTO tenantDTO, @DefaultValue("false") boolean patch) {

		if (id == null) {
			return createTenant(tenantDTO);
		} else {
			return patch
				? patchTenant(id, tenantDTO)
				: updateTenant(id, tenantDTO);
		}

	}

	@Mutation
	public Uni<Tenant> deleteTenant(@Id long tenantId) {
		return tenantService.deleteById(tenantId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, Datasource>> addDatasourceToTenant(@Id long tenantId, @Id long datasourceId) {
		return tenantService.addDatasource(tenantId, datasourceId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, Datasource>> removeDatasourceFromTenant(@Id long tenantId, @Id long datasourceId) {
		return tenantService.removeDatasource(tenantId, datasourceId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, SuggestionCategory>> addSuggestionCategoryToTenant(@Id long tenantId, @Id long suggestionCategoryId) {
		return tenantService.addSuggestionCategory(tenantId, suggestionCategoryId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, SuggestionCategory>> removeSuggestionCategoryFromTenant(@Id long tenantId, @Id long suggestionCategoryId) {
		return tenantService.removeSuggestionCategory(tenantId, suggestionCategoryId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, QueryAnalysis>> bindQueryAnalysisToTenant(
		@Id long tenantId, @Id long queryAnalysisId) {
		return tenantService.bindQueryAnalysis(tenantId, queryAnalysisId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, QueryAnalysis>> unbindQueryAnalysisFromTenant(
		@Id long tenantId) {
		return tenantService.unbindQueryAnalysis(tenantId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, SearchConfig>> bindSearchConfigToTenant(
		@Id long tenantId, @Id long searchConfigId) {
		return tenantService.bindSearchConfig(tenantId, searchConfigId);
	}

	@Mutation
	public Uni<Tuple2<Tenant, SearchConfig>> unbindSearchConfigFromTenant(
		@Id long tenantId) {
		return tenantService.unbindSearchConfig(tenantId);
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