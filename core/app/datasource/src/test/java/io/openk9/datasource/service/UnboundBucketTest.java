package io.openk9.datasource.service;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.dto.BucketDTO;
import io.openk9.datasource.model.dto.DatasourceDTO;
import io.openk9.datasource.model.dto.SuggestionCategoryDTO;
import io.openk9.datasource.model.dto.TabDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UnboundBucketTest {

	public static final String BUCKET_DEFAULT_NAME = "Default Bucket";
	public static final String BUCKET_ONE_NAME =
		"Bucket with Datasource 1";
	public static final String BUCKET_UNBOUND_NAME = "Unbound Bucket";
	public static final String DATASOURCE_ONE_NAME = "Datasource 1";
	public static final String DATASOURCE_TWO_NAME = "Datasource 2";
	public static final String DATASOURCE_THREE_NAME = "Datasource 3";
	public static final String SUGGESTION_CATEGORY_ONE_NAME = "Suggestion category 1";
	public static final String SUGGESTION_CATEGORY_TWO_NAME = "Suggestion category 2";
	public static final String SUGGESTION_CATEGORY_THREE_NAME = "Suggestion category 3";
	public static final String TAB_ONE_NAME = "Tab 1";
	public static final String TAB_TWO_NAME = "Tab 2";
	public static final String TAB_THREE_NAME = "Tab 3";

	@Inject
	BucketService bucketService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Inject
	TabService tabService;

	@BeforeAll
	void initDatasource() {
		createBucketUnbound();
		createBucketWithTestOneDatasource();

		createDatasourceOne();
		createDatasourceTwo();
		createDatasourceThree();

		createSuggestionCategoryOne();
		createSuggestionCategoryTwo();
		createSuggestionCategoryThree();

		createTabOne();
		createTabTwo();
		createTabThree();

		//bind entities to buckets
		bindBucketOneToDatasourceOne();
		bindBucketOneToSuggestionCategoryOne();
		bindBucketOneToTabOne();

		//bind bucket default
		bindBucketDefaultToDatasourceTwo();
		bindBucketDefaultToSuggestionCategoryTwo();
		bindBucketDefaultToTabTwo();

		bindBucketDefaultToDatasourceThree();
		bindBucketDefaultToSuggestionCategoryThree();
		bindBucketDefaultToTabThree();
	}

	@Test
	void should_retrieve_unbound_bucket_from_datasource_two() {

		var unboundBuckets = getUnboundBucketByDatasourceTwo();

		assertFalse(unboundBuckets.isEmpty());

		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket ->
				BUCKET_ONE_NAME.equalsIgnoreCase(bucket.getName())));
		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket -> BUCKET_UNBOUND_NAME.equalsIgnoreCase(bucket.getName())));

		//Must not have default Bucket
		assertFalse(unboundBuckets.stream()
			.anyMatch(bucket -> BUCKET_DEFAULT_NAME.equalsIgnoreCase(bucket.getName())));

		assertEquals(2, unboundBuckets.size());

	}

	@Test
	void should_retrieve_all_bucket_from_missing_datasource() {
		var unboundBuckets = getUnboundBucketByMissingDatasource();

		assertFalse(unboundBuckets.isEmpty());

		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket ->
				BUCKET_ONE_NAME.equalsIgnoreCase(bucket.getName())));
		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket -> BUCKET_UNBOUND_NAME.equalsIgnoreCase(bucket.getName())));
		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket -> BUCKET_DEFAULT_NAME.equalsIgnoreCase(bucket.getName())));

		assertEquals(3, unboundBuckets.size());
	}

	@Test
	void should_retrieve_unbound_bucket_from_suggestion_category_two() {

		var unboundBuckets = getUnboundBucketBySuggestionCategoryTwo();

		assertFalse(unboundBuckets.isEmpty());

		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket ->
				BUCKET_ONE_NAME.equalsIgnoreCase(bucket.getName())));
		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket -> BUCKET_UNBOUND_NAME.equalsIgnoreCase(bucket.getName())));

		//Must not have default Bucket
		assertFalse(unboundBuckets.stream()
			.anyMatch(bucket -> BUCKET_DEFAULT_NAME.equalsIgnoreCase(bucket.getName())));

		assertEquals(2, unboundBuckets.size());

	}

	@Test
	void should_retrieve_all_bucket_from_missing_suggestion_category() {
		var unboundBuckets = getUnboundBucketByMissingSuggestionCategory();

		assertFalse(unboundBuckets.isEmpty());

		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket ->
				BUCKET_ONE_NAME.equalsIgnoreCase(bucket.getName())));
		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket -> BUCKET_UNBOUND_NAME.equalsIgnoreCase(bucket.getName())));
		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket -> BUCKET_DEFAULT_NAME.equalsIgnoreCase(bucket.getName())));

		assertEquals(3, unboundBuckets.size());
	}

	@Test
	void should_retrieve_unbound_bucket_from_tab_two() {
		var bucketDefault = getBucketDefault();
		assertFalse(bucketDefault.getTabs().isEmpty());

		var unboundBuckets = getUnboundBucketByTabTwo();

		assertFalse(unboundBuckets.isEmpty());

		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket ->
				BUCKET_ONE_NAME.equalsIgnoreCase(bucket.getName())));
		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket -> BUCKET_UNBOUND_NAME.equalsIgnoreCase(bucket.getName())));

		//Must not have default Bucket
		assertFalse(unboundBuckets.stream()
			.anyMatch(bucket -> BUCKET_DEFAULT_NAME.equalsIgnoreCase(bucket.getName())));

		assertEquals(2, unboundBuckets.size());
	}

	@Test
	void should_retrieve_all_bucket_from_missing_tab() {
		var unboundBuckets = getUnboundBucketByMissingTab();

		assertFalse(unboundBuckets.isEmpty());

		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket ->
				BUCKET_ONE_NAME.equalsIgnoreCase(bucket.getName())));
		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket -> BUCKET_UNBOUND_NAME.equalsIgnoreCase(bucket.getName())));
		assertTrue(unboundBuckets.stream()
			.anyMatch(bucket -> BUCKET_DEFAULT_NAME.equalsIgnoreCase(bucket.getName())));

		assertEquals(3, unboundBuckets.size());
	}

	private void bindBucketDefaultToDatasourceTwo() {

		var bucket = getBucketDefault();
		var datasource = getDatasourceTwo();

		sessionFactory.withTransaction(
			(s, transaction) ->
				bucketService.addDatasource(bucket.getId(), datasource.getId())
		)
			.await()
			.indefinitely();
	}

	private void bindBucketDefaultToDatasourceThree() {

		var bucket = getBucketDefault();
		var datasource = getDatasourceThree();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.addDatasource(bucket.getId(), datasource.getId())
			)
			.await()
			.indefinitely();
	}

	private void bindBucketDefaultToSuggestionCategoryTwo() {

		var bucket = getBucketDefault();
		var suggestionCategory = getSuggestionCategoryTwo();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.addSuggestionCategory(bucket.getId(), suggestionCategory.getId())
			)
			.await()
			.indefinitely();
	}

	private void bindBucketDefaultToSuggestionCategoryThree() {

		var bucket = getBucketDefault();
		var suggestionCategory = getSuggestionCategoryThree();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.addSuggestionCategory(bucket.getId(), suggestionCategory.getId())
			)
			.await()
			.indefinitely();
	}

	private void bindBucketDefaultToTabTwo() {

		var bucket = getBucketDefault();
		var tab = getTabTwo();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.addTabToBucket(bucket.getId(), tab.getId())
			)
			.await()
			.indefinitely();
	}

	private void bindBucketDefaultToTabThree() {

		var bucket = getBucketDefault();
		var tab = getTabThree();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.addTabToBucket(bucket.getId(), tab.getId())
			)
			.await()
			.indefinitely();
	}

	private void bindBucketOneToDatasourceOne() {

		var bucket = getBucketOne();
		var datasource = getDatasourceOne();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.addDatasource(bucket.getId(), datasource.getId())
			)
			.await()
			.indefinitely();
	}

	private void bindBucketOneToSuggestionCategoryOne() {

		var bucket = getBucketOne();
		var suggestionCategory = getSuggestionCategoryOne();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.addSuggestionCategory(bucket.getId(), suggestionCategory.getId())
			)
			.await()
			.indefinitely();
	}

	private void bindBucketOneToTabOne() {

		var bucket = getBucketOne();
		var tab = getTabOne();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.addTabToBucket(bucket.getId(), tab.getId())
			)
			.await()
			.indefinitely();
	}

	private void createBucketUnbound() {
		BucketDTO dto = BucketDTO.builder()
			.name(BUCKET_UNBOUND_NAME)
			.refreshOnSuggestionCategory(false)
			.refreshOnTab(false)
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.retrieveType(Bucket.RetrieveType.MATCH)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createBucketWithTestOneDatasource() {
		BucketDTO dto = BucketDTO.builder()
			.name(BUCKET_ONE_NAME)
			.refreshOnSuggestionCategory(false)
			.refreshOnTab(false)
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.retrieveType(Bucket.RetrieveType.MATCH)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createDatasourceOne() {
		DatasourceDTO dto = DatasourceDTO.builder()
			.name(DATASOURCE_ONE_NAME)
			.reindexRate(0)
			.schedulable(false)
			.scheduling("0 0 * ? * * *")
			.build();

		sessionFactory.withTransaction(
				(s,transaction) ->
					datasourceService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createDatasourceTwo() {
		DatasourceDTO dto = DatasourceDTO.builder()
			.name(DATASOURCE_TWO_NAME)
			.reindexRate(0)
			.schedulable(false)
			.scheduling("0 0 * ? * * *")
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createDatasourceThree() {
		DatasourceDTO dto = DatasourceDTO.builder()
			.name(DATASOURCE_THREE_NAME)
			.reindexRate(0)
			.schedulable(false)
			.scheduling("0 0 * ? * * *")
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createSuggestionCategoryOne() {
		SuggestionCategoryDTO dto = SuggestionCategoryDTO.builder()
			.name(SUGGESTION_CATEGORY_ONE_NAME)
			.priority(0f)
			.multiSelect(false)
			.build();

		sessionFactory.withTransaction(
				(s,transaction) ->
					suggestionCategoryService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createSuggestionCategoryTwo() {
		SuggestionCategoryDTO dto = SuggestionCategoryDTO.builder()
			.name(SUGGESTION_CATEGORY_TWO_NAME)
			.priority(0f)
			.multiSelect(false)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createSuggestionCategoryThree() {
		SuggestionCategoryDTO dto = SuggestionCategoryDTO.builder()
			.name(SUGGESTION_CATEGORY_THREE_NAME)
			.priority(0f)
			.multiSelect(false)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createTabOne() {
		TabDTO dto = TabDTO.builder()
			.name(TAB_ONE_NAME)
			.priority(0)
			.build();

		sessionFactory.withTransaction(
				(s,transaction) ->
					tabService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createTabTwo() {
		TabDTO dto = TabDTO.builder()
			.name(TAB_TWO_NAME)
			.priority(0)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tabService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createTabThree() {
		TabDTO dto = TabDTO.builder()
			.name(TAB_THREE_NAME)
			.priority(0)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tabService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private Bucket getBucketDefault() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.findByName(s, BUCKET_DEFAULT_NAME)
						.call(bucket -> Mutiny.fetch(bucket.getDatasources()))
						.call(bucket -> Mutiny.fetch(bucket.getSuggestionCategories()))
						.call(bucket -> Mutiny.fetch(bucket.getTabs()))
			)
			.await()
			.indefinitely();
	}

	private Bucket getBucketOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.findByName(s, BUCKET_ONE_NAME)
						.call(bucket -> Mutiny.fetch(bucket.getDatasources()))
						.call(bucket -> Mutiny.fetch(bucket.getSuggestionCategories()))
						.call(bucket -> Mutiny.fetch(bucket.getTabs()))
			)
			.await()
			.indefinitely();
	}

	private Datasource getDatasourceOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.findByName(s, DATASOURCE_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private Datasource getDatasourceTwo() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.findByName(s, DATASOURCE_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private Datasource getDatasourceThree() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.findByName(s, DATASOURCE_THREE_NAME)
			)
			.await()
			.indefinitely();
	}

	private SuggestionCategory getSuggestionCategoryOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.findByName(s, SUGGESTION_CATEGORY_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private SuggestionCategory getSuggestionCategoryTwo() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.findByName(s, SUGGESTION_CATEGORY_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private SuggestionCategory getSuggestionCategoryThree() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.findByName(s, SUGGESTION_CATEGORY_THREE_NAME)
			)
			.await()
			.indefinitely();
	}

	private Tab getTabOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					tabService.findByName(s, TAB_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private Tab getTabTwo() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					tabService.findByName(s, TAB_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private Tab getTabThree() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					tabService.findByName(s, TAB_THREE_NAME)
			)
			.await()
			.indefinitely();
	}

	private List<Bucket> getUnboundBucketByDatasourceTwo() {
		var datasourceId = getDatasourceTwo().getId();

		return bucketService.findUnboundBucketsByDatasource(datasourceId)
			.await()
			.indefinitely();
	}

	private List<Bucket> getUnboundBucketBySuggestionCategoryTwo() {
		var suggestionCategoryId = getSuggestionCategoryTwo().getId();

		return bucketService.findUnboundBucketsBySuggestionCategory(suggestionCategoryId)
			.await()
			.indefinitely();
	}

	private List<Bucket> getUnboundBucketByTabTwo() {
		var tabId = getTabTwo().getId();

		return bucketService.findUnboundBucketsByTab(tabId)
			.await()
			.indefinitely();
	}

	private List<Bucket> getUnboundBucketByMissingDatasource() {
		return bucketService.findUnboundBucketsByDatasource(0L)
			.await()
			.indefinitely();
	}

	private List<Bucket> getUnboundBucketByMissingSuggestionCategory() {
		return bucketService.findUnboundBucketsBySuggestionCategory(0L)
				.await()
				.indefinitely();
	}

	private List<Bucket> getUnboundBucketByMissingTab() {
		return bucketService.findUnboundBucketsByTab(0L)
			.await()
			.indefinitely();
	}

}
