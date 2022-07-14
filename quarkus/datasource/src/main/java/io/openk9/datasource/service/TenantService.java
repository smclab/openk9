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

import io.openk9.datasource.mapper.TenantMapper;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.dto.TenantDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TenantService extends BaseK9EntityService<Tenant, TenantDTO> {
	 TenantService(TenantMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<Page<Datasource>> getDatasources(List<Tenant> tenants, Pageable pageable) {
		return getDatasources(
			tenants.stream().mapToLong(K9Entity::getId).toArray(),
			pageable);
	}

	public Uni<Page<Datasource>> getDatasources(long tenantId, Pageable pageable) {
		return getDatasources(
			new long[] {tenantId}, pageable);
	}

	public Uni<Page<Datasource>> getDatasources(Tenant tenant, Pageable pageable) {
		 return getDatasources(
			 new long[] {tenant.getId()}, pageable);
	}

	public Uni<Page<Datasource>> getDatasources(long[] tenantId, Pageable pageable) {

		Map<String, Object> params = new HashMap<>();

		boolean isOne = tenantId.length == 1;

		params.put("tenantId", isOne ? tenantId[0] : tenantId);

		String query =
			"select d " +
			"from Tenant tenant " +
			"join tenant.datasources d " +
			(isOne ? "where tenant.id = :tenantId " : "where tenant.id in (:tenantId) ");

		query = createPageableQuery(pageable, params, query, "d");

		Sort sort = createSort("d", pageable.getSortBy().name());

		PanacheQuery<Datasource> panacheQuery =
			Tenant
				.find(query, sort, params)
				.page(0, pageable.getLimit());

		Uni<Long> countQuery =
			Tenant.count(
				"from Tenant tenant join tenant.datasources datasource where tenant.id = ?1",
				tenantId);

		return createPage(
			pageable.getLimit(), panacheQuery, countQuery);
	}

	public Uni<Page<SuggestionCategory>> getSuggestionCategories(
		Tenant tenant, Pageable pageable) {
		return getSuggestionCategories(tenant.getId(), pageable);
	}

	public Uni<Page<SuggestionCategory>> getSuggestionCategories(
		long tenantId, Pageable pageable) {

		Map<String, Object> params = new HashMap<>();

		params.put("tenantId", tenantId);

		String query =
			"select sc " +
			"from Tenant tenant " +
			"join tenant.suggestionCategories sc " +
			"where tenant.id = :tenantId ";

		query = createPageableQuery(pageable, params, query, "sc");

		Sort sort = createSort("sc", pageable.getSortBy().name());

		PanacheQuery<SuggestionCategory> panacheQuery =
			Tenant
				.find(query, sort, params)
				.page(0, pageable.getLimit());

		Uni<Long> countQuery =
			Tenant.count(
				"from Tenant tenant join tenant.suggestionCategories datasource where tenant.id = ?1",
				tenantId);

		return createPage(
			pageable.getLimit(), panacheQuery, countQuery);
	}

	public Uni<Void> removeDatasource(long tenantId, long datasourceId) {
		return _findTenantAndDatasource(tenantId, datasourceId)
			.flatMap(tuple -> {
				Tenant tenant = tuple.getItem1();
				Datasource datasource = tuple.getItem2();
				tenant.removeDatasource(datasource);
				return persist(tenant);
			})
			.replaceWithVoid();
	}

	public Uni<Void> addDatasource(long tenantId, long datasourceId) {

		Uni<Tuple2<Tenant, Datasource>> tuple2Uni =
			_findTenantAndDatasource(tenantId, datasourceId);

		return tuple2Uni
			.flatMap(t -> {

				t.getItem1().addDatasource(t.getItem2());

				return persist(t.getItem1());

			})
			.replaceWithVoid();

	}

	public Uni<Void> addSuggestionCategory(long tenantId, long suggestionCategoryId) {
		return _findTenantAndSuggestionCategory(tenantId, suggestionCategoryId)
			.flatMap(t -> {
				Tenant tenant = t.getItem1();
				tenant.addSuggestionCategory(t.getItem2());
				return persist(tenant);
			})
			.replaceWithVoid();
	}

	public Uni<Void> removeSuggestionCategory(long tenantId, long suggestionCategoryId) {
		return _findTenantAndSuggestionCategory(tenantId, suggestionCategoryId)
			.flatMap(t -> {
				Tenant tenant = t.getItem1();
				tenant.removeSuggestionCategory(t.getItem2());
				return persist(tenant);
			})
			.replaceWithVoid();
	}

	private Uni<Tuple2<Tenant, SuggestionCategory>> _findTenantAndSuggestionCategory(
		long tenantId, long suggestionCategoryId) {

		Uni<Tenant> tenantUni = findById(tenantId);
		Uni<SuggestionCategory> suggestionCategoryServiceById =
			suggestionCategoryService.findById(suggestionCategoryId);

		return Uni
			.combine()
			.all()
			.unis(tenantUni, suggestionCategoryServiceById)
			.asTuple()
			.call(t -> {

				if (t.getItem1() == null) {
					return Uni.createFrom().failure(() -> new IllegalStateException("Tenant not found for id " + tenantId));
				}

				if (t.getItem2() == null) {
					return Uni.createFrom().failure(() -> new IllegalStateException("SuggestionCategory not found for id " + suggestionCategoryId));
				}

				return Uni.createFrom().item(t);

			});

	}

	private Uni<Tuple2<Tenant, Datasource>> _findTenantAndDatasource(
		long tenantId, long datasourceId) {

		Uni<Tenant> tenantUni = findById(tenantId);
		Uni<Datasource> datasourceUni = datasourceService.findById(datasourceId);

		return Uni
			.combine()
			.all()
			.unis(tenantUni, datasourceUni)
			.asTuple()
			.call(t -> {

				if (t.getItem1() == null) {
					return Uni.createFrom().failure(() -> new IllegalStateException("Tenant not found for id " + tenantId));
				}

				if (t.getItem2() == null) {
					return Uni.createFrom().failure(() -> new IllegalStateException("Datasource not found for id " + datasourceId));
				}

				return Uni.createFrom().item(t);

			});
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
