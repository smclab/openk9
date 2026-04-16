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

import java.util.ArrayList;
import java.util.List;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.dto.base.K9EntityDTO;
import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;
import io.openk9.datasource.model.dto.request.BucketWithListsDTO;
import io.openk9.datasource.model.dto.request.CreateRAGConfigurationDTO;
import io.openk9.datasource.model.dto.request.SearchConfigWithQueryParsersDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.service.BaseK9EntityService;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.RAGConfigurationService;
import io.openk9.datasource.service.SearchConfigService;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

public class EntitiesUtils {

	// Create methods
	public static <ENTITY extends K9Entity, DTO extends K9EntityDTO,
		SERVICE extends BaseK9EntityService<ENTITY, DTO>> void createEntity(
		DTO dto,
		SERVICE service,
		Mutiny.SessionFactory sessionFactory) {

		sessionFactory.withTransaction(
				session -> service.create(dto)
			)
			.await()
			.indefinitely();
	}

	public static <ENTITY extends K9Entity, DTO extends K9EntityDTO,
		SERVICE extends BaseK9EntityService<ENTITY, DTO>> void createEntity(
		ENTITY entity,
		SERVICE service,
		Mutiny.SessionFactory sessionFactory) {

		sessionFactory.withTransaction(
				session -> service.create(entity)
			)
			.await()
			.indefinitely();
	}

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

	// Get methods
	public static <ENTITY extends K9Entity, DTO extends K9EntityDTO,
		SERVICE extends BaseK9EntityService<ENTITY, DTO>> List<ENTITY> getAllEntities(
			SERVICE service,
			Mutiny.SessionFactory sessionFactory) {

		return sessionFactory.withTransaction(session ->
				service.findAll()
			)
			.await()
			.indefinitely();
	}

	public static <ENTITY extends K9Entity, DTO extends K9EntityDTO,
		SERVICE extends BaseK9EntityService<ENTITY, DTO>> ENTITY getEntity(
			long id,
			SERVICE service,
			Mutiny.SessionFactory sessionFactory) {

		return sessionFactory.withTransaction(
				session -> service.findById(session, id)
			)
			.await()
			.indefinitely();
	}

	public static <ENTITY extends K9Entity, DTO extends K9EntityDTO,
		SERVICE extends BaseK9EntityService<ENTITY, DTO>> ENTITY getEntity(
			String name,
			SERVICE service,
			Mutiny.SessionFactory sessionFactory) {

		return sessionFactory.withTransaction(
			session -> service.findByName(session, name)
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

	// Remove methods
	public static <ENTITY extends K9Entity, DTO extends K9EntityDTO,
		SERVICE extends BaseK9EntityService<ENTITY, DTO>> void removeEntity(
			ENTITY entity,
			SERVICE service,
			Mutiny.SessionFactory sessionFactory) {

		removeEntity(entity.getId(), service, sessionFactory);
	}

	public static <ENTITY extends K9Entity, DTO extends K9EntityDTO,
		SERVICE extends BaseK9EntityService<ENTITY, DTO>> void removeEntity(
			String name,
			SERVICE service,
			Mutiny.SessionFactory sessionFactory) {

		var entity = getEntity(name, service, sessionFactory);

		removeEntity(entity.getId(), service, sessionFactory);
	}

	public static <ENTITY extends K9Entity, DTO extends K9EntityDTO,
		SERVICE extends BaseK9EntityService<ENTITY, DTO>> void removeEntity(
			long id,
			SERVICE service,
			Mutiny.SessionFactory sessionFactory) {

		sessionFactory.withTransaction(session -> service.deleteById(session, id))
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
}
