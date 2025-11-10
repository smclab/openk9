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
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.dto.base.AutocorrectionDTO;
import io.openk9.datasource.model.dto.base.BucketDTO;
import io.openk9.datasource.model.dto.base.K9EntityDTO;
import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;
import io.openk9.datasource.model.dto.request.BucketWithListsDTO;
import io.openk9.datasource.model.dto.request.CreateRAGConfigurationDTO;
import io.openk9.datasource.model.dto.request.SearchConfigWithQueryParsersDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.service.AutocorrectionService;
import io.openk9.datasource.service.BaseK9EntityService;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.RAGConfigurationService;
import io.openk9.datasource.service.SearchConfigService;
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
	public static void cleanBucket(Bucket bucket, BucketService bucketService) {
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

	public static void createBucket(
		String name, BucketService bucketService, Mutiny.SessionFactory sessionFactory) {

		BucketDTO dto = BucketDTO.builder()
			.name(name)
			.refreshOnSuggestionCategory(false)
			.refreshOnTab(false)
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.retrieveType(Bucket.RetrieveType.TEXT)
			.build();

		createEntity(dto, bucketService, sessionFactory);
	}

	/**
	 * Creates and persists an {@link Autocorrection} entity with the given name.
	 * Other fields of the entity will be set to their default values or null.
	 *
	 * @param name The name for the new Autocorrection entity.
	 * @param service The {@link AutocorrectionService} used to interact with autocorrection-related operations.
	 * @param sessionFactory The Hibernate reactive {@link Mutiny.SessionFactory} for database interactions.
	 */
	public static void createDefaultAutocorrection(
			String name,
			Long docTypeFieldId,
			AutocorrectionService service,
			Mutiny.SessionFactory sessionFactory) {

		AutocorrectionDTO dto = AutocorrectionDTO.builder()
			.name(name)
			.autocorrectionDocTypeFieldId(docTypeFieldId)
			.build();

		createEntity(dto, service, sessionFactory);
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
			String name,
			SERVICE service,
			Mutiny.SessionFactory sessionFactory) {

		return sessionFactory.withTransaction(
			session -> service.findByName(session, name)
			)
			.await()
			.indefinitely();
	}

	// Custom retrieval methods for entities needing eager fetching
	public static Autocorrection getAutocorrection(
			String name,
			AutocorrectionService autocorrectionService,
			Mutiny.SessionFactory sessionFactory) {

		return sessionFactory.withTransaction(
				session -> autocorrectionService.findByName(session, name)
					.call(autocorrection ->
						Mutiny.fetch(autocorrection.getAutocorrectionDocTypeField())
					)
			)
			.await()
			.indefinitely();
	}

	public static Datasource getDatasource(
			String name,
			DatasourceService datasourceService,
			Mutiny.SessionFactory sessionFactory) {

		return sessionFactory.withTransaction(
				session -> datasourceService.findByName(session, name)
					.call(datasource ->
						Mutiny.fetch(datasource.getDataIndexes())
					)
			)
			.await()
			.indefinitely();
	}

	public static Long getSampleTextDocTypeFieldId(
			DocTypeFieldService docTypeFieldService,
			Mutiny.SessionFactory sessionFactory) {

		var docTypeFields = getAllEntities(docTypeFieldService, sessionFactory);

		return docTypeFields.stream()
			.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
			.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
			.map(K9Entity::getId)
			.findFirst()
			.orElse(0L);
	}

	public static SearchConfig getSearchConfig(
			String name,
			SearchConfigService searchConfigService,
			Mutiny.SessionFactory sessionFactory) {

		return sessionFactory.withTransaction(
				session -> searchConfigService.findByName(session, name)
					.call(searchConfig ->
						Mutiny.fetch(searchConfig.getQueryParserConfigs()))
			)
			.await()
			.indefinitely();
	}

	public static RAGConfiguration getRAGConfiguration(
			String name,
			RAGConfigurationService ragConfigurationService,
			Mutiny.SessionFactory sessionFactory) {

		return sessionFactory.withTransaction(
				session -> ragConfigurationService.findByName(session, name)
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

	// Custom remove methods for the SearchConfig entity
	public static void removeSearchConfig(
			SearchConfig searchConfig,
			SearchConfigService searchConfigService,
			Mutiny.SessionFactory sessionFactory) {

		removeSearchConfig(searchConfig.getId(), searchConfigService, sessionFactory);
	}

	public static void removeSearchConfig(
			String name,
			SearchConfigService searchConfigService,
			Mutiny.SessionFactory sessionFactory) {

		var searchConfig =
			getSearchConfig(name, searchConfigService, sessionFactory);

		removeSearchConfig(
			searchConfig.getId(), searchConfigService, sessionFactory);
	}

	public static void removeSearchConfig(
			long id,
			SearchConfigService searchConfigService,
			Mutiny.SessionFactory sessionFactory) {

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
