package io.openk9.datasource;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.dto.base.BucketDTO;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import io.openk9.datasource.model.dto.base.RAGConfigurationDTO;
import io.openk9.datasource.model.dto.base.SuggestionCategoryDTO;
import io.openk9.datasource.model.dto.base.TabDTO;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.DatasourceConnectionObjects;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.RAGConfigurationService;
import io.openk9.datasource.service.SuggestionCategoryService;
import io.openk9.datasource.service.TabService;
import org.hibernate.reactive.mutiny.Mutiny;

public class EntitiesUtils {

	public static void createBucket(
		Mutiny.SessionFactory sessionFactory, BucketService bucketService, String name) {

		BucketDTO dto = BucketDTO.builder()
			.name(name)
			.refreshOnSuggestionCategory(false)
			.refreshOnTab(false)
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.retrieveType(Bucket.RetrieveType.MATCH)
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
		Mutiny.SessionFactory sessionFactory, RAGConfigurationService ragConfigurationService,
		String name, RAGType type) {

		RAGConfigurationDTO dto = RAGConfigurationDTO.builder()
			.name(name)
			.type(type)
			.build();

		createRAGConfiguration(sessionFactory, ragConfigurationService, dto);
	}

	public static void createRAGConfiguration(
		Mutiny.SessionFactory sessionFactory, RAGConfigurationService ragConfigurationService,
		RAGConfigurationDTO dto) {

		sessionFactory.withTransaction(
				session -> ragConfigurationService.create(session, dto)
			)
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
