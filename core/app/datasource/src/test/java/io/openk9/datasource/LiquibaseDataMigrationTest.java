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

import jakarta.inject.Inject;

import io.openk9.datasource.service.BucketService;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LiquibaseDataMigrationTest {

	@Inject
	BucketService bucketService;

	// test for changeSet with id 1739804793-2
	@Test
	void should_return_sample_suggestion_from_sample_bucket() {
		var suggestionCategories = bucketService.findByName("public", "Sample Bucket")
			.flatMap(bucket ->
				bucketService.getSuggestionCategories(bucket.getId())
			)
			.await()
			.indefinitely();

		Assertions.assertEquals(1, suggestionCategories.size());

		var suggestionCategory = suggestionCategories.iterator().next();

		Assertions.assertEquals(
			"Sample Suggestion category", suggestionCategory.getName());
	}
}
