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

package io.openk9.datasource.service;

import io.openk9.datasource.graphql.util.relay.Connection;
import io.openk9.datasource.mapper.TenantMapper;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.Tenant_;
import io.openk9.datasource.model.dto.TenantDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import javassist.NotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Set;

@ApplicationScoped
public class TenantService extends BaseK9EntityService<Tenant, TenantDTO> {
	 TenantService(TenantMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<QueryAnalysis> getQueryAnalysis(long tenantId) {
		return withTransaction(s -> findById(tenantId)
			.flatMap(tenant -> Mutiny2.fetch(s, tenant.getQueryAnalysis())));
	}

	public Uni<Connection<Datasource>> getDatasourcesConnection(
		long tenantId, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			tenantId, Tenant_.DATASOURCES, Datasource.class,
			datasourceService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}

	public Uni<Page<Datasource>> getDatasources(long tenantId, Pageable pageable, Filter filter) {
		return getDatasources(new Long[] {tenantId}, pageable, filter);
	}

	public Uni<Page<Datasource>> getDatasources(
		long tenantId, Pageable pageable, String searchText) {
		return getDatasources(new Long[] {tenantId}, pageable, searchText);
	}

	public Uni<Page<Datasource>> getDatasources(
		Long[] tenantIds, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			tenantIds, Tenant_.DATASOURCES, Datasource.class,
			pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), searchText);
	}

	public Uni<Page<Datasource>> getDatasources(
		Long[] tenantIds, Pageable pageable, Filter filter) {

		 return findAllPaginatedJoin(
			 tenantIds, Tenant_.DATASOURCES, Datasource.class,
			 pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			 pageable.getBeforeId(), filter);
	}

	public Uni<Connection<Tab>> getTabs(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, Tenant_.TABS, Tab.class,
			tabService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}


	public Uni<Connection<SuggestionCategory>> getSuggestionCategoriesConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {
		return findJoinConnection(
			id, Tenant_.SUGGESTION_CATEGORIES, SuggestionCategory.class,
			suggestionCategoryService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}

	public Uni<Page<SuggestionCategory>> getSuggestionCategories(
		long tenantId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[]{tenantId}, Tenant_.SUGGESTION_CATEGORIES, SuggestionCategory.class,
			pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), searchText);
	}

	public Uni<Page<SuggestionCategory>> getSuggestionCategories(
		long tenantId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[]{tenantId}, Tenant_.SUGGESTION_CATEGORIES, SuggestionCategory.class,
			pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), filter);
	}

	public Uni<Tuple2<Tenant, Datasource>> removeDatasource(long tenantId, long datasourceId) {
		return withTransaction((s, tr) -> findById(tenantId)
			.onItem()
			.ifNotNull()
			.transformToUni(tenant -> datasourceService.findById(datasourceId)
				.onItem()
				.ifNotNull()
				.transformToUni(datasource -> Mutiny2.fetch(s, datasource.getTenants()).flatMap(tenants -> {
					if (tenants.remove(tenant)) {
						datasource.setTenants(tenants);
						return persist(datasource).map((newD) -> Tuple2.of(tenant, newD));
					}
					return Uni.createFrom().nullItem();
				}))));
	}

	public Uni<Tuple2<Tenant, Datasource>> addDatasource(long tenantId, long datasourceId) {

		return withTransaction((s, tr) -> findById(tenantId)
			.onItem()
			.ifNotNull()
			.transformToUni(tenant -> datasourceService.findById(datasourceId)
				.onItem()
				.ifNotNull()
				.transformToUni(datasource -> Mutiny2.fetch(s, datasource.getTenants()).flatMap(tenants -> {
					if (tenants.add(tenant)) {
						datasource.setTenants(tenants);
						return persist(datasource).map(newD -> Tuple2.of(tenant, newD));
					}
					return Uni.createFrom().nullItem();
				}))));

	}

	public Uni<Tuple2<Tenant, Tab>> addTabToTenant(
		long id, long tabId) {

		return withTransaction((s, tr) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(tenant -> tabService.findById(tabId)
				.onItem()
				.ifNotNull()
				.transformToUni(tab ->
					Mutiny2.fetch(s, tenant.getTabs())
						.onItem()
						.ifNotNull()
						.transformToUni(tabs -> {

							if (tabs.add(tab)) {

								tenant.setTabs(tabs);

								return persist(tenant)
									.map(newSC -> Tuple2.of(newSC, tab));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<Tenant, Tab>> removeTabFromTenant(
		long id, long tabId) {
		return withTransaction((s, tr) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(tenant -> Mutiny2.fetch(s, tenant.getTabs())
				.onItem()
				.ifNotNull()
				.transformToUni(tabs -> {

					if (tenant.removeTab(tabs, tabId)) {

						return persist(tenant)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<Tenant, SuggestionCategory>> addSuggestionCategory(long tenantId, long suggestionCategoryId) {
		return withTransaction((s, tr) -> findById(tenantId)
			.onItem()
			.ifNotNull()
			.transformToUni(tenant -> suggestionCategoryService.findById(suggestionCategoryId)
				.onItem()
				.ifNotNull()
				.transformToUni(suggestionCategory ->
					Mutiny2.fetch(s, tenant.getSuggestionCategories())
						.onItem()
						.ifNotNull()
						.transformToUni(suggestionCategories -> {

							if (tenant.addSuggestionCategory(
								suggestionCategories, suggestionCategory)) {

								return persist(tenant)
									.map(newSC -> Tuple2.of(newSC, null));
							}

							return Uni.createFrom().nullItem();

						})
				)
			));
	}

	public Uni<Tuple2<Tenant, SuggestionCategory>> removeSuggestionCategory(long tenantId, long suggestionCategoryId) {
		return withTransaction((s, tr) -> findById(tenantId)
			.onItem()
			.ifNotNull()
			.transformToUni(tenant -> Mutiny2.fetch(s, tenant.getSuggestionCategories())
				.onItem()
				.ifNotNull()
				.transformToUni(suggestionCategories -> {

					if (tenant.removeSuggestionCategory(
						suggestionCategories, suggestionCategoryId)) {

						return persist(tenant)
							.map(newSC -> Tuple2.of(newSC, null));
					}

					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<Tenant, QueryAnalysis>> bindQueryAnalysis(long tenantId, long queryAnalysisId) {
		return withTransaction((s, tr) -> findById(tenantId)
			.onItem()
			.ifNotNull()
			.transformToUni(tenant -> queryAnalysisService.findById(queryAnalysisId)
				.onItem()
				.ifNotNull()
				.transformToUni(queryAnalysis -> {
					tenant.setQueryAnalysis(queryAnalysis);
					return persist(tenant).map(t -> Tuple2.of(t, queryAnalysis));
				})));
	}

	public Uni<Tuple2<Tenant, QueryAnalysis>> unbindQueryAnalysis(long tenantId) {
		return withTransaction((s, tr) -> findById(tenantId)
			.onItem()
			.ifNotNull()
			.transformToUni(tenant -> {
				tenant.setQueryAnalysis(null);
				return persist(tenant).map(t -> Tuple2.of(t, null));
			}));
	}


	public Uni<Tuple2<Tenant, SearchConfig>> bindSearchConfig(long tenantId, long searchConfigId) {
		return withTransaction((s, tr) -> findById(tenantId)
			.onItem()
			.ifNotNull()
			.transformToUni(tenant -> searchConfigService.findById(searchConfigId)
				.onItem()
				.ifNotNull()
				.transformToUni(searchConfig -> {
					tenant.setSearchConfig(searchConfig);
					return persist(tenant).map(t -> Tuple2.of(t, searchConfig));
				})));
	}

	public Uni<Tuple2<Tenant, SearchConfig>> unbindSearchConfig(long tenantId) {
		return withTransaction((s, tr) -> findById(tenantId)
			.onItem()
			.ifNotNull()
			.transformToUni(tenant -> {
				tenant.setSearchConfig(null);
				return persist(tenant).map(t -> Tuple2.of(t, null));
			}));
	}

	public Uni<Tenant> enableTenant(long id) {
		return withTransaction(s -> s
			.find(Tenant.class, id)
			.flatMap(tenant -> {

				if (tenant == null) {
					return Uni
						.createFrom()
						.failure(new NotFoundException("Tenant not found for id " + id));
				}

				TenantBinding tenantBinding = tenant.getTenantBinding();

				if (tenantBinding == null) {
					CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

					CriteriaQuery<TenantBinding> query =
						criteriaBuilder.createQuery(TenantBinding.class);

					query.from(TenantBinding.class);

					return s
						.createQuery(query)
						.getSingleResultOrNull()
						.flatMap(tb -> {

							if (tb == null) {
								return Uni
									.createFrom()
									.failure(new NotFoundException("Tenant binding not found create one first"));
							}

							tenant.setTenantBinding(tb);
							tb.setTenant(tenant);

							return s
								.persist(tb)
								.map(t -> tenant)
								.call(s::flush);

						});

				}

				return Uni.createFrom().item(tenant);

			}));
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {Tenant_.NAME, Tenant_.VIRTUAL_HOST};
	}

	@Inject
	DatasourceService datasourceService;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Inject
	QueryAnalysisService queryAnalysisService;

	@Inject
	SearchConfigService searchConfigService;

	@Inject
	TabService tabService;

	@Override
	public Class<Tenant> getEntityClass() {
		return Tenant.class;
	}

}
