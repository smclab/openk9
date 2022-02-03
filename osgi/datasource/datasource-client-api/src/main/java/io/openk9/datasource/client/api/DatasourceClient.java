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

	Mono<SuggestionCategory> findSuggestionCategory(long categoryId);

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
