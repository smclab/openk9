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

package io.openk9.datasource;

import io.openk9.datasource.model.Autocorrection;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.dto.base.BucketDTO;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;
import io.openk9.datasource.model.dto.base.SuggestionCategoryDTO;
import io.openk9.datasource.model.dto.base.TabDTO;
import io.openk9.datasource.model.dto.request.BucketWithListsDTO;
import io.openk9.datasource.model.dto.request.CreateRAGConfigurationDTO;
import io.openk9.datasource.model.dto.request.SearchConfigWithQueryParsersDTO;
import io.openk9.datasource.service.AutocorrectionService;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.DatasourceConnectionObjects;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.RAGConfigurationService;
import io.openk9.datasource.service.SearchConfigService;
import io.openk9.datasource.service.SuggestionCategoryService;
import io.openk9.datasource.service.TabService;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.ArrayList;
import java.util.List;

public class EntitiesUtils {

	/**
	 * Cleans the state of the given {@link Bucket} by resetting its configurable values.
	 *
	 * <p>This method updates the specified {@code bucket} by:
	 * <ul>
	 *   <li>Setting default values for mandatory fields:</li>
	 *   <ul>
	 *     <li>Disables all refresh flags ({@code refreshOnDate}, {@code refreshOnQuery}, etc.).</li>
	 *     <li>Sets the {@code retrieveType} to {@link Bucket.RetrieveType#TEXT}.</li>
	 *   </ul>
	 *   <li>Implicitly unsetting or nullifying any optional configuration not explicitly included in the update DTO.</li>
	 * </ul>
	 *
	 * <p>This operation is synchronous and blocks until the update is complete.</p>
	 *
	 * @param bucketService The {@link BucketService} used to perform the update.
	 * @param bucket The {@link Bucket} whose configuration needs to be cleaned.
	 *
	 * @throws RuntimeException if the update fails or if the operation is interrupted.
	 *
	 * <h3>Error Handling</h3>
	 * Any exception raised during the update is propagated as a runtime exception.
	 */
	public static void cleanBucket(BucketService bucketService, Bucket bucket) {
		bucketService.update(bucket.getId(), BucketWithListsDTO.builder()
			.name(bucket.getName())
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.refreshOnTab(false)
			.refreshOnSuggestionCategory(false)
			.retrieveType(Bucket.RetrieveType.TEXT)
			.build()
		).await().indefinitely();
	}

	public static void createBucket(
		Mutiny.SessionFactory sessionFactory, BucketService bucketService, String name) {

		BucketDTO dto = BucketDTO.builder()
			.name(name)
			.refreshOnSuggestionCategory(false)
			.refreshOnTab(false)
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.retrieveType(Bucket.RetrieveType.TEXT)
			.build();

		createBucket(sessionFactory, bucketService, dto);
	}

	public static void createBucket(
		Mutiny.SessionFactory sessionFactory, BucketService bucketService, BucketDTO dto) {

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.create(dto)
			)
			.await()
			.indefinitely();
	}

	public static void createDatasource(
		Mutiny.SessionFactory sessionFactory, DatasourceService datasourceService,
		String name) {

		DatasourceDTO dto = DatasourceDTO.builder()
			.name(name)
			.scheduling(DatasourceConnectionObjects.SCHEDULING)
			.schedulable(false)
			.reindexing(DatasourceConnectionObjects.REINDEXING)
			.reindexable(false)
			.build();

		createDatasource(sessionFactory, datasourceService, dto);
	}

	public static void createDatasource(
		Mutiny.SessionFactory sessionFactory, DatasourceService datasourceService,
		DatasourceDTO dto) {

		sessionFactory.withTransaction(
				(s,transaction) ->
					datasourceService.create(dto)
			)
			.await()
			.indefinitely();
	}

	public static void createRAGConfiguration(
			RAGConfigurationService ragConfigurationService,
			String name, RAGType type) {

		CreateRAGConfigurationDTO dto = CreateRAGConfigurationDTO.builder()
			.name(name)
			.type(type)
			.build();

		createRAGConfiguration(ragConfigurationService, dto);
	}

	public static void createRAGConfiguration(
			RAGConfigurationService ragConfigurationService,
			CreateRAGConfigurationDTO dto) {

		ragConfigurationService.create(dto)
			.await()
			.indefinitely();
	}

	/**
	 * Creates and persists a {@link SearchConfig} with the given name and associated {@link QueryParserConfigDTO}s.
	 *
	 * <p>Builds a {@link SearchConfigWithQueryParsersDTO} with default values and invokes the
	 * {@code create} method of {@link SearchConfigService}, waiting for the result synchronously.</p>
	 *
	 * @param searchConfigService the service used to persist the search configuration
	 * @param name the name of the search configuration
	 * @param queryParserConfigDTOList the list of query parser configurations to associate
	 */
	public static void createSearchConfig(
			SearchConfigService searchConfigService,
			String name,
			List<QueryParserConfigDTO> queryParserConfigDTOList) {

		SearchConfigWithQueryParsersDTO dto = SearchConfigWithQueryParsersDTO.builder()
			.name(name)
			.minScore(0F)
			.minScoreSuggestions(false)
			.minScoreSearch(false)
			.queryParsers(queryParserConfigDTOList)
			.build();

		searchConfigService.create(dto)
			.await()
			.indefinitely();
	}

	public static void createSuggestionCategory(
		Mutiny.SessionFactory sessionFactory, SuggestionCategoryService suggestionCategoryService,
		String name) {

		SuggestionCategoryDTO dto = SuggestionCategoryDTO.builder()
			.name(name)
			.priority(0f)
			.multiSelect(false)
			.build();

		createSuggestionCategory(sessionFactory, suggestionCategoryService, dto);
	}

	public static void createSuggestionCategory(
		Mutiny.SessionFactory sessionFactory, SuggestionCategoryService suggestionCategoryService,
		SuggestionCategoryDTO dto) {

		sessionFactory.withTransaction(
				session -> suggestionCategoryService.create(session, dto)
			)
			.await()
			.indefinitely();
	}

	public static void createTab(
		Mutiny.SessionFactory sessionFactory, TabService tabService,
		String name) {

		TabDTO dto = TabDTO.builder()
			.name(name)
			.priority(0)
			.build();

		createTab(sessionFactory, tabService, dto);
	}

	public static void createTab(
		Mutiny.SessionFactory sessionFactory, TabService tabService,
		TabDTO dto) {

		sessionFactory.withTransaction(
				session -> tabService.create(session, dto)
			)
			.await()
			.indefinitely();
	}

	public static Autocorrection getAutocorrection(
			Mutiny.SessionFactory sessionFactory, AutocorrectionService autocorrectionService,
			String name) {

		return sessionFactory.withTransaction(
				session -> autocorrectionService.findByName(session, name)
			)
			.await()
			.indefinitely();
	}

	public static Bucket getBucket(
		Mutiny.SessionFactory sessionFactory, BucketService bucketService, String name) {

		return sessionFactory.withTransaction(
				session -> bucketService.findByName(session, name)
			)
			.await()
			.indefinitely();
	}

	public static Datasource getDatasource(
		Mutiny.SessionFactory sessionFactory, DatasourceService datasourceService,
		String name) {

		return sessionFactory.withTransaction(
				session -> datasourceService.findByName(session, name)
			)
			.await()
			.indefinitely();
	}

	public static RAGConfiguration getRAGConfiguration(
		Mutiny.SessionFactory sessionFactory, RAGConfigurationService ragConfigurationService,
		String name) {

		return sessionFactory.withTransaction(
				session -> ragConfigurationService.findByName(session, name)
			)
			.await()
			.indefinitely();
	}

	public static SearchConfig getSearchConfig(
		Mutiny.SessionFactory sessionFactory, SearchConfigService searchConfigService,
		String name) {

		return sessionFactory.withTransaction(
				session -> searchConfigService.findByName(session, name)
				.call(searchConfig ->
					Mutiny.fetch(searchConfig.getQueryParserConfigs()))
			)
			.await()
			.indefinitely();
	}

	public static SuggestionCategory getSuggestionCategory(
		Mutiny.SessionFactory sessionFactory, SuggestionCategoryService suggestionCategoryService,
		String name) {

		return sessionFactory.withTransaction(
				session -> suggestionCategoryService.findByName(session, name)
			)
			.await()
			.indefinitely();
	}

	public static Tab getTab(
		Mutiny.SessionFactory sessionFactory, TabService tabService,
		String name) {

		return sessionFactory.withTransaction(
				session -> tabService.findByName(session, name)
			)
			.await()
			.indefinitely();
	}

	public static void removeAutocorrection(
			Mutiny.SessionFactory sessionFactory, AutocorrectionService autocorrectionService,
			Autocorrection autocorrection) {

		removeAutocorrection(sessionFactory, autocorrectionService, autocorrection.getId());
	}

	public static void removeAutocorrection(
			Mutiny.SessionFactory sessionFactory, AutocorrectionService autocorrectionService,
			String name) {

		var autocorrection =
			EntitiesUtils.getAutocorrection(sessionFactory, autocorrectionService, name);

		removeAutocorrection(sessionFactory, autocorrectionService, autocorrection.getId());
	}

	public static void removeAutocorrection(
			Mutiny.SessionFactory sessionFactory, AutocorrectionService autocorrectionService,
			long id) {

		sessionFactory.withTransaction(
				session ->
					autocorrectionService.deleteById(session, id)
			)
			.await()
			.indefinitely();
	}

	public static void removeBucket(
		Mutiny.SessionFactory sessionFactory, BucketService bucketService, Bucket bucket) {

		removeBucket(sessionFactory, bucketService, bucket.getId());
	}

	public static void removeBucket(
		Mutiny.SessionFactory sessionFactory, BucketService bucketService, String name) {

		var bucket = EntitiesUtils.getBucket(sessionFactory, bucketService, name);

		removeBucket(sessionFactory, bucketService, bucket.getId());
	}

	public static void removeBucket(
		Mutiny.SessionFactory sessionFactory, BucketService bucketService, long id) {

		sessionFactory.withTransaction(
				session ->
					bucketService.deleteById(session, id)
			)
			.await()
			.indefinitely();
	}

	public static void removeDatasource(
		Mutiny.SessionFactory sessionFactory, DatasourceService datasourceService,
		Datasource datasource) {

		removeDatasource(sessionFactory, datasourceService, datasource.getId());
	}

	public static void removeDatasource(
		Mutiny.SessionFactory sessionFactory, DatasourceService datasourceService, String name) {

		var datasource = getDatasource(sessionFactory, datasourceService, name);

		removeDatasource(sessionFactory, datasourceService, datasource.getId());
	}

	public static void removeDatasource(
		Mutiny.SessionFactory sessionFactory, DatasourceService datasourceService, long id) {

		sessionFactory.withTransaction(
				session -> datasourceService.deleteById(session, id)
			)
			.await()
			.indefinitely();
	}

	public static void removeQueryParserConfig(
		SearchConfigService searchConfigService,
		long id, long queryParserId) {

			searchConfigService.removeQueryParserConfig(id, queryParserId)
				.await()
				.indefinitely();
	}

	public static void removeRAGConfiguration(
		Mutiny.SessionFactory sessionFactory, RAGConfigurationService ragConfigurationService,
		RAGConfiguration ragConfiguration) {

		removeRAGConfiguration(sessionFactory, ragConfigurationService, ragConfiguration.getId());
	}

	public static void removeRAGConfiguration(
		Mutiny.SessionFactory sessionFactory, RAGConfigurationService ragConfigurationService,
		String name) {

		var ragConfiguration = getRAGConfiguration(sessionFactory, ragConfigurationService, name);

		removeRAGConfiguration(sessionFactory, ragConfigurationService, ragConfiguration.getId());
	}

	public static void removeRAGConfiguration(
		Mutiny.SessionFactory sessionFactory, RAGConfigurationService ragConfigurationService,
		long id) {

		sessionFactory.withTransaction(
				session -> ragConfigurationService.deleteById(session, id)
			)
			.await()
			.indefinitely();
	}

	public static void removeSearchConfig(
		Mutiny.SessionFactory sessionFactory,
		SearchConfigService searchConfigService,
		SearchConfig searchConfig) {

		removeSearchConfig(
			sessionFactory, searchConfigService, searchConfig.getId());
	}

	public static void removeSearchConfig(
		Mutiny.SessionFactory sessionFactory,
		SearchConfigService searchConfigService,
		String name) {

		var searchConfig =
			getSearchConfig(sessionFactory, searchConfigService, name);

		removeSearchConfig(
			sessionFactory, searchConfigService, searchConfig.getId());
	}

	public static void removeSearchConfig(
		Mutiny.SessionFactory sessionFactory,
		SearchConfigService searchConfigService,
		long id) {

		sessionFactory.withTransaction(
				session -> {
					List<Uni<SearchConfig>> unis = new ArrayList<>();
					unis.add(searchConfigService.removeAllQueryParserConfig(session, id));
					unis.add(searchConfigService.deleteById(session, id));

					return Uni.combine().all().unis(unis)
						.usingConcurrencyOf(1)
						.with(ignored -> Uni.createFrom().voidItem());
				})
			.await()
			.indefinitely();
	}

	public static void removeSuggestionCategory(
		Mutiny.SessionFactory sessionFactory,
		SuggestionCategoryService suggestionCategoryService,
		SuggestionCategory suggestionCategory) {

		removeSuggestionCategory(
			sessionFactory, suggestionCategoryService, suggestionCategory.getId());
	}

	public static void removeSuggestionCategory(
		Mutiny.SessionFactory sessionFactory,
		SuggestionCategoryService suggestionCategoryService,
		String name) {

		var suggestionCategory =
			getSuggestionCategory(sessionFactory, suggestionCategoryService, name);

		removeSuggestionCategory(
			sessionFactory, suggestionCategoryService, suggestionCategory.getId());
	}

	public static void removeSuggestionCategory(
		Mutiny.SessionFactory sessionFactory,
		SuggestionCategoryService suggestionCategoryService,
		long id) {

		sessionFactory.withTransaction(
				session -> suggestionCategoryService.deleteById(session, id)
			)
			.await()
			.indefinitely();
	}

	public static void removeTab(
		Mutiny.SessionFactory sessionFactory, TabService tabService, Tab tab) {

		removeTab(sessionFactory, tabService, tab.getId());
	}

	public static void removeTab(
		Mutiny.SessionFactory sessionFactory, TabService tabService, String name) {

		var tab = getTab(sessionFactory, tabService, name);

		removeTab(sessionFactory, tabService, tab.getId());
	}

	public static void removeTab(
		Mutiny.SessionFactory sessionFactory, TabService tabService, long id) {

		sessionFactory.withTransaction(
				session -> tabService.deleteById(session, id)
			)
			.await()
			.indefinitely();
	}
}
