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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import jakarta.inject.Inject;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.dto.base.BucketDTO;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import io.openk9.datasource.model.dto.base.SuggestionCategoryDTO;
import io.openk9.datasource.model.dto.base.TabDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class UnboundBucketTest {

	private static final String ENTITY_NAME_PREFIX = "UnboundBucketTest - ";

	private static final String BUCKET_DEFAULT_NAME = ENTITY_NAME_PREFIX + "Default Bucket";
	private static final String BUCKET_ONE_NAME =
		ENTITY_NAME_PREFIX + "Bucket with Datasource 1";
	private static final String BUCKET_UNBOUND_NAME = ENTITY_NAME_PREFIX + "Unbound Bucket";
	private static final String DATASOURCE_ONE_NAME = ENTITY_NAME_PREFIX + "Datasource 1";
	private static final String DATASOURCE_TWO_NAME = ENTITY_NAME_PREFIX + "Datasource 2";
	private static final String DATASOURCE_THREE_NAME = ENTITY_NAME_PREFIX + "Datasource 3";
	private static final String DATASOURCE_FOUR_NAME = ENTITY_NAME_PREFIX + "Datasource 4";
	private static final String SUGGESTION_CATEGORY_ONE_NAME =
		ENTITY_NAME_PREFIX + "Suggestion category 1";
	private static final String SUGGESTION_CATEGORY_TWO_NAME =
		ENTITY_NAME_PREFIX + "Suggestion category 2";
	private static final String SUGGESTION_CATEGORY_THREE_NAME =
		ENTITY_NAME_PREFIX + "Suggestion category 3";
	private static final String SUGGESTION_CATEGORY_FOUR_NAME =
		ENTITY_NAME_PREFIX + "Suggestion category 4";
	private static final String TAB_ONE_NAME = ENTITY_NAME_PREFIX + "Tab 1";
	private static final String TAB_TWO_NAME = ENTITY_NAME_PREFIX + "Tab 2";
	private static final String TAB_THREE_NAME = ENTITY_NAME_PREFIX + "Tab 3";

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

	@BeforeEach
	void setup() {
		createBucketDefault();
		createBucketUnbound();
		createBucketOne();

		createDatasourceOne();
		createDatasourceTwo();
		createDatasourceThree();
		createDatasourceFour();

		createSuggestionCategoryOne();
		createSuggestionCategoryTwo();
		createSuggestionCategoryThree();
		createSuggestionCategoryFour();

		createTabOne();
		createTabTwo();
		createTabThree();

		//bind entities to buckets
		bindBucketOneToDatasourceOne();
		bindBucketOneToSuggestionCategoryOne();
		bindBucketOneToTabOne();

		var bucketOne = getBucketOne();
		assertEquals(1, bucketOne.getDatasources().size());
		assertEquals(1, bucketOne.getSuggestionCategories().size());
		assertEquals(1, bucketOne.getTabs().size());

		//bind bucket default
		bindBucketDefaultToDatasourceTwo();
		bindBucketDefaultToSuggestionCategoryTwo();
		bindBucketDefaultToTabTwo();

		bindBucketDefaultToDatasourceThree();
		bindBucketDefaultToSuggestionCategoryThree();
		bindBucketDefaultToTabThree();

		var bucketDefault = getBucketDefault();
		assertEquals(2, bucketDefault.getDatasources().size());
		assertEquals(2, bucketDefault.getSuggestionCategories().size());
		assertEquals(2, bucketDefault.getTabs().size());
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

		assertEquals(allBucketCount() - 1, unboundBuckets.size());

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

		assertEquals(allBucketCount(), unboundBuckets.size());
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

		assertEquals(allBucketCount() - 1, unboundBuckets.size());

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

		assertEquals(allBucketCount(), unboundBuckets.size());
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

		assertEquals(allBucketCount() - 1, unboundBuckets.size());
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

		assertEquals(allBucketCount(), unboundBuckets.size());
	}

	@Test
	void should_retrieve_all_bucket_from_not_associated_suggestionCategory() {

		var unboundBuckets = getUnboundBucketBySuggestionCategoryFour();

		assertEquals(allBucketCount(), unboundBuckets.size());
	}

	@Test
	void should_retrieve_all_bucket_from_not_associated_datasource() {
		var unboundBuckets = getUnboundBucketByDatasourceFour();

		assertEquals(allBucketCount(), unboundBuckets.size());
	}

	@AfterEach
	void tearDown() {

		unbindBucketOneToSuggestionCategoryOne();
		unbindBucketDefaultToSuggestionCategoryTwo();
		unbindBucketDefaultToSuggestionCategoryThree();

		removeDatasourceOne();
		removeDatasourceTwo();
		removeDatasourceThree();
		removeDatasourceFour();

		removeSuggestionCategoryOne();
		removeSuggestionCategoryTwo();
		removeSuggestionCategoryThree();
		removeSuggestionCategoryFour();

		removeBucketDefault();
		removeBucketOne();
		removeBucketUnbound();

		removeTabOne();
		removeTabTwo();
		removeTabThree();

	}
	private Long allBucketCount() {

		return sessionFactory.withTransaction(
			(s, transaction) ->
				bucketService.count()
		)
			.await()
			.indefinitely();
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

	private void createBucketDefault() {
		BucketDTO dto = BucketDTO.builder()
			.name(BUCKET_DEFAULT_NAME)
			.refreshOnSuggestionCategory(false)
			.refreshOnTab(false)
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.retrieveType(Bucket.RetrieveType.TEXT)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createBucketOne() {
		BucketDTO dto = BucketDTO.builder()
			.name(BUCKET_ONE_NAME)
			.refreshOnSuggestionCategory(false)
			.refreshOnTab(false)
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.retrieveType(Bucket.RetrieveType.TEXT)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.create(dto)
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
			.retrieveType(Bucket.RetrieveType.TEXT)
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
			.scheduling(DatasourceConnectionObjects.SCHEDULING)
			.schedulable(false)
			.reindexing(DatasourceConnectionObjects.REINDEXING)
			.reindexable(false)
			.build();

		sessionFactory.withTransaction(
				(s,transaction) ->
					datasourceService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createDatasourceThree() {
		DatasourceDTO dto = DatasourceDTO.builder()
			.name(DATASOURCE_THREE_NAME)
			.scheduling(DatasourceConnectionObjects.SCHEDULING)
			.schedulable(false)
			.reindexing(DatasourceConnectionObjects.REINDEXING)
			.reindexable(false)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createDatasourceTwo() {
		DatasourceDTO dto = DatasourceDTO.builder()
			.name(DATASOURCE_TWO_NAME)
			.scheduling(DatasourceConnectionObjects.SCHEDULING)
			.schedulable(false)
			.reindexing(DatasourceConnectionObjects.REINDEXING)
			.reindexable(false)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createDatasourceFour() {
		DatasourceDTO dto = DatasourceDTO.builder()
			.name(DATASOURCE_FOUR_NAME)
			.scheduling(DatasourceConnectionObjects.SCHEDULING)
			.schedulable(false)
			.reindexing(DatasourceConnectionObjects.REINDEXING)
			.reindexable(false)
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

	private void createSuggestionCategoryFour() {
		SuggestionCategoryDTO dto = SuggestionCategoryDTO.builder()
			.name(SUGGESTION_CATEGORY_FOUR_NAME)
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

	private List<Bucket> getBucketAll() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.findAll()
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

	private Bucket getBucketUnbound() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.findByName(s, BUCKET_UNBOUND_NAME)
						.call(bucket -> Mutiny.fetch(bucket.getDatasources()))
						.call(bucket -> Mutiny.fetch(bucket.getSuggestionCategories()))
						.call(bucket -> Mutiny.fetch(bucket.getTabs()))
			)
			.await()
			.indefinitely();
	}

	private List<Datasource> getDatasourceAll() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.findAll()
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

	private Datasource getDatasourceFour() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.findByName(s, DATASOURCE_FOUR_NAME)
			)
			.await()
			.indefinitely();
	}

	private List<SuggestionCategory> getSuggestionCategoryAll() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.findAll()
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

	private SuggestionCategory getSuggestionCategoryFour() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.findByName(s, SUGGESTION_CATEGORY_FOUR_NAME)
			)
			.await()
			.indefinitely();
	}

	private List<Tab> getTabAll() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					tabService.findAll()
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

	private List<Bucket> getUnboundBucketByDatasourceFour() {
		var datasourceId = getDatasourceFour().getId();

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

	private List<Bucket> getUnboundBucketBySuggestionCategoryFour() {
		var suggestionCategoryId = getSuggestionCategoryFour().getId();

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

	private void removeBucketDefault() {
		var bucketId = getBucketDefault().getId();

		sessionFactory.withTransaction(
			(s, transaction) ->
				bucketService.deleteById(bucketId)
		)
		.await()
		.indefinitely();
	}

	private void removeBucketOne() {
		var bucketId = getBucketOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.deleteById(bucketId)
			)
			.await()
			.indefinitely();
	}

	private void removeBucketUnbound() {
		var bucketId = getBucketUnbound().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.deleteById(bucketId)
			)
			.await()
			.indefinitely();
	}

	private void removeDatasourceOne() {
		var datasourceId = getDatasourceOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.deleteById(datasourceId)
			)
			.await()
			.indefinitely();
	}

	private void removeDatasourceTwo() {
		var datasourceId = getDatasourceTwo().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.deleteById(datasourceId)
			)
			.await()
			.indefinitely();
	}

	private void removeDatasourceThree() {
		var datasourceId = getDatasourceThree().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.deleteById(datasourceId)
			)
			.await()
			.indefinitely();
	}

	private void removeDatasourceFour() {
		var datasourceId = getDatasourceFour().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.deleteById(datasourceId)
			)
			.await()
			.indefinitely();
	}

	private void unbindBucketDefaultToSuggestionCategoryThree() {
		var bucketId = getBucketDefault().getId();
		var suggestionCategoryId = getSuggestionCategoryThree().getId();

		sessionFactory.withTransaction((s, t) ->
			bucketService.removeSuggestionCategory(bucketId, suggestionCategoryId)
		).await().indefinitely();

	}

	private void unbindBucketDefaultToSuggestionCategoryTwo() {
		var bucketId = getBucketDefault().getId();
		var suggestionCategoryId = getSuggestionCategoryTwo().getId();

		sessionFactory.withTransaction((s, t) ->
			bucketService.removeSuggestionCategory(bucketId, suggestionCategoryId)
		).await().indefinitely();
	}

	private void unbindBucketOneToSuggestionCategoryOne() {
		var bucketId = getBucketOne().getId();
		var suggestionCategoryId = getSuggestionCategoryOne().getId();

		sessionFactory.withTransaction((s, t) ->
			bucketService.removeSuggestionCategory(bucketId, suggestionCategoryId)
		).await().indefinitely();

	}


	private void removeSuggestionCategoryOne() {
		var suggestionCategoryId = getSuggestionCategoryOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.deleteById(suggestionCategoryId)
			)
			.await()
			.indefinitely();
	}

	private void removeSuggestionCategoryTwo() {
		var suggestionCategoryId = getSuggestionCategoryTwo().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.deleteById(suggestionCategoryId)
			)
			.await()
			.indefinitely();
	}

	private void removeSuggestionCategoryThree() {
		var suggestionCategoryId = getSuggestionCategoryThree().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.deleteById(suggestionCategoryId)
			)
			.await()
			.indefinitely();
	}

	private void removeSuggestionCategoryFour() {
		var suggestionCategoryId = getSuggestionCategoryFour().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					suggestionCategoryService.deleteById(suggestionCategoryId)
			)
			.await()
			.indefinitely();
	}

	private void removeTabOne() {
		var tabId = getTabOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tabService.deleteById(tabId)
			)
			.await()
			.indefinitely();
	}

	private void removeTabTwo() {
		var tabId = getTabTwo().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tabService.deleteById(tabId)
			)
			.await()
			.indefinitely();
	}

	private void removeTabThree() {
		var tabId = getTabThree().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tabService.deleteById(tabId)
			)
			.await()
			.indefinitely();
	}
}
