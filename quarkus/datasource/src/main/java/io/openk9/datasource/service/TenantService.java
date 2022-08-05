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
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.dto.TenantDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class TenantService extends BaseK9EntityService<Tenant, TenantDTO> {
	 TenantService(TenantMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<Connection<Datasource>> getDatasourcesConnection(
		long tenantId, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			tenantId, "datasources", Datasource.class,
			datasourceService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}

	public Uni<Page<Datasource>> getDatasources(List<Tenant> tenants, Pageable pageable) {
		return getDatasources(
			tenants.stream().map(K9Entity::getId).toArray(Long[]::new),
			pageable, Filter.DEFAULT);
	}

	public Uni<Page<Datasource>> getDatasources(long tenantId, Pageable pageable, Filter filter) {
		return getDatasources(new Long[] {tenantId}, pageable, filter);
	}

	public Uni<Page<Datasource>> getDatasources(
		long tenantId, Pageable pageable, String searchText) {
		return getDatasources(new Long[] {tenantId}, pageable, searchText);
	}

	public Uni<Page<Datasource>> getDatasources(long tenantId, Pageable pageable) {
		return getDatasources(new Long[] {tenantId}, pageable, Filter.DEFAULT);
	}

	public Uni<Page<Datasource>> getDatasources(Tenant tenant, Pageable pageable) {
		 return getDatasources(
			 new Long[] {tenant.getId()}, pageable, Filter.DEFAULT);
	}

	public Uni<Page<Datasource>> getDatasources(
		Long[] tenantIds, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			tenantIds, "datasources", Datasource.class,
			pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), searchText);
	}

	public Uni<Page<Datasource>> getDatasources(
		Long[] tenantIds, Pageable pageable, Filter filter) {

		 return findAllPaginatedJoin(
			 tenantIds, "datasources", Datasource.class,
			 pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			 pageable.getBeforeId(), filter);
	}

	public Uni<Connection<SuggestionCategory>> getSuggestionCategoriesConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {
		return findJoinConnection(
			id, "suggestionCategories", SuggestionCategory.class,
			suggestionCategoryService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}

	public Uni<Page<SuggestionCategory>> getSuggestionCategories(
		long tenantId, Pageable pageable) {
		return getSuggestionCategories(tenantId, pageable, Filter.DEFAULT);
	}

	public Uni<Page<SuggestionCategory>> getSuggestionCategories(
		long tenantId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[]{tenantId}, "suggestionCategories", SuggestionCategory.class,
			pageable.getLimit(), pageable.getSortBy().name(), pageable.getAfterId(),
			pageable.getBeforeId(), searchText);
	}

	public Uni<Page<SuggestionCategory>> getSuggestionCategories(
		long tenantId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[]{tenantId}, "suggestionCategories", SuggestionCategory.class,
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

	@Override
	public String[] getSearchFields() {
		return new String[] {"name", "virtualHost"};
	}

	@Inject
	DatasourceService datasourceService;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Inject
	Mutiny.Session session;

	@Override
	public Class<Tenant> getEntityClass() {
		return Tenant.class;
	}

}
