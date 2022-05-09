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

package io.openk9.datasource.client.api;

import io.openk9.model.Datasource;
import io.openk9.model.SuggestionCategory;
import io.openk9.model.SuggestionCategoryField;
import io.openk9.model.SuggestionCategoryPayload;
import io.openk9.model.Tenant;
import io.openk9.model.TenantDatasource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DatasourceClient {

	Mono<TenantDatasource> findTenantAndDatasourceByDatasourceId(
		long datasourceId);

	Mono<Datasource> findDatasource(long datasourceId);

	Flux<Datasource> findDatasourceByTenantIdAndIsActive(long tenantId);

	Flux<Datasource> findDatasourceByTenantId(long tenantId);

	Flux<Tenant> findTenantByVirtualHost(String virtualHost);

	Mono<Tenant> findTenant(long tenantId);

	Mono<List<SuggestionCategory>> findSuggestionCategories();

	Mono<List<SuggestionCategory>> findSuggestionCategories(long tenantId);

	Mono<SuggestionCategory> findSuggestionCategory(long categoryId);

	Mono<List<SuggestionCategory>> findSuggestionCategoryByTenantIdAndCategoryId(
		long tenantId, long categoryId);

	Mono<List<SuggestionCategoryPayload>> findSuggestionCategoriesWithFields();

	Mono<SuggestionCategoryPayload> findSuggestionCategoryWithFieldsById(
		long categoryId);

	Mono<List<SuggestionCategoryField>> findSuggestionCategoryFields();

	Mono<List<SuggestionCategoryField>> findSuggestionCategoryFieldsByTenantId(
		long tenantId);

	Mono<List<SuggestionCategoryField>> findSuggestionCategoryFieldsByTenantIdAndCategoryId(
		long tenantId, long categoryId);

	Mono<List<SuggestionCategoryField>> findSuggestionCategoryFieldsByCategoryId(
		long categoryId);

	Mono<List<SuggestionCategoryField>> findSuggestionCategoryFieldsByCategoryIdEnabled(
		long categoryId);
}
