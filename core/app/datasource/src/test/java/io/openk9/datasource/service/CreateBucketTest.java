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

import io.openk9.datasource.graphql.dto.BucketWithListsDTO;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.util.K9Entity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class CreateBucketTest {

	private static final String CREATE_BUCKET_TEST_NAME = "CreateBucketTest";

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	BucketService bucketService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Test
	void should_create_bucket_with_lists() {

		var datasourceIds = datasourceService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var suggestionCategorieIds = suggestionCategoryService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var bucket = bucketService.create(BucketWithListsDTO.builder()
			.name(CREATE_BUCKET_TEST_NAME)
			.refreshOnDate(true)
			.refreshOnQuery(true)
			.refreshOnTab(true)
			.refreshOnSuggestionCategory(true)
			.retrieveType(Bucket.RetrieveType.MATCH)
			.datasourceIds(datasourceIds)
			.suggestionCategoryIds(suggestionCategorieIds)
			.build()
		).await().indefinitely();

		var bucketId = bucket.getId();

		var createdBucket = sessionFactory.withTransaction((s, t) ->
			bucketService.findById(s, bucketId)
				.call(entity -> Mutiny.fetch(entity.getDatasources()))
				.call(entity -> Mutiny.fetch(entity.getSuggestionCategories()))
		).await().indefinitely();

		assertEquals(CREATE_BUCKET_TEST_NAME, createdBucket.getName());
		assertEquals(datasourceIds.size(), createdBucket.getDatasources().size());

		var datasources = createdBucket.getDatasources().iterator();
		assertEquals(CreateConnection.DATASOURCE_NAME, datasources.next().getName());

		assertEquals(suggestionCategorieIds.size(), createdBucket.getSuggestionCategories().size());
	}

}

