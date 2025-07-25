/*
 * Copyright (C) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.Autocorrection;
import io.openk9.datasource.model.Bucket;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class UnboundAutocorrectionTest {

	private static final String ENTITY_NAME_PREFIX = "UnboundAutocorrectionTest - ";

	private static final String BUCKET_ONE = ENTITY_NAME_PREFIX + "Bucket 1";
	private static final String BUCKET_TWO = ENTITY_NAME_PREFIX + "Bucket 2";
	private static final String AUTOCORRECTION_ONE = ENTITY_NAME_PREFIX + "Autocorrection 1";
	private static final String AUTOCORRECTION_TWO = ENTITY_NAME_PREFIX + "Autocorrection 2";
	private static final String AUTOCORRECTION_THREE = ENTITY_NAME_PREFIX + "Autocorrection 3";

	private int allAutocorrectionCount = 0;

	@Inject
	AutocorrectionService autocorrectionService;

	@Inject
	BucketService bucketService;

	@Inject
	Mutiny.SessionFactory sf;

	@BeforeEach
	void setup() {
		// Create bucketOne and bucketTwo
		EntitiesUtils.createBucket(sf, bucketService, BUCKET_ONE);
		EntitiesUtils.createBucket(sf, bucketService, BUCKET_TWO);

		// Create Autocorrection one, two and three
		EntitiesUtils.createAutocorrection(sf, autocorrectionService, AUTOCORRECTION_ONE);
		EntitiesUtils.createAutocorrection(sf, autocorrectionService, AUTOCORRECTION_TWO);
		EntitiesUtils.createAutocorrection(sf, autocorrectionService, AUTOCORRECTION_THREE);

		allAutocorrectionCount = 3;
	}

	@Test
	void should_bind_and_unbind_autocorrection_to_bucket_one() {
		var bucket = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		var autocorrectionOne =
			EntitiesUtils.getAutocorrection(AUTOCORRECTION_ONE, autocorrectionService, sf);
		var autocorrectionTwo =
			EntitiesUtils.getAutocorrection(AUTOCORRECTION_TWO, autocorrectionService, sf);

		// Initial check
		assertNull(bucket.getAutocorrection());

		// bind autocorrection
		bindAutocorrectionToBucket(bucket, autocorrectionOne);

		bucket = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		assertEquals(autocorrectionOne.getId(), bucket.getAutocorrection().getId());

		// override bind autocorrection
		bindAutocorrectionToBucket(bucket, autocorrectionTwo);

		bucket = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		assertEquals(autocorrectionTwo.getId(), bucket.getAutocorrection().getId());

		// unbind autocorrection
		unbindAutocorrectionToBucket(bucket);

		bucket = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		assertNull(bucket.getAutocorrection());
	}

	@Test
	void should_retrieve_all_autocorrections_from_empty_bucket() {

		var bucket = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		var autocorrectionOne =
			EntitiesUtils.getAutocorrection(AUTOCORRECTION_ONE, autocorrectionService, sf);
		var autocorrectionTwo =
			EntitiesUtils.getAutocorrection(AUTOCORRECTION_TWO, autocorrectionService, sf);

		List<Autocorrection> unboundAutocorrections = getUnboundAutocorrections(bucket.getId());

		assertFalse(unboundAutocorrections.isEmpty());
		assertEquals(allAutocorrectionCount, unboundAutocorrections.size());
		assertTrue(unboundAutocorrections.contains(autocorrectionOne));
		assertTrue(unboundAutocorrections.contains(autocorrectionTwo));
	}

	@Test
	void should_retrieve_all_autocorrections_except_the_one_associated_with_bucket_one() {

		var bucketOne = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		var bucketTwo = EntitiesUtils.getEntity(BUCKET_TWO, bucketService, sf);
		var autocorrectionOne =
			EntitiesUtils.getAutocorrection(AUTOCORRECTION_ONE, autocorrectionService, sf);
		var autocorrectionTwo =
			EntitiesUtils.getAutocorrection(AUTOCORRECTION_TWO, autocorrectionService, sf);
		var autocorrectionThree =
			EntitiesUtils.getAutocorrection(AUTOCORRECTION_THREE, autocorrectionService, sf);

		bindAutocorrectionToBucket(bucketOne, autocorrectionOne);

		// Associates a autocorrectionTwo with another Bucket.
		bindAutocorrectionToBucket(bucketTwo, autocorrectionTwo);

		List<Autocorrection> unboundAutocorrections =
			getUnboundAutocorrections(bucketOne.getId());

		assertFalse(unboundAutocorrections.isEmpty());
		assertEquals(allAutocorrectionCount - 1, unboundAutocorrections.size());
		assertFalse(unboundAutocorrections.contains(autocorrectionOne));
		assertTrue(unboundAutocorrections.contains(autocorrectionTwo));
		assertTrue(unboundAutocorrections.contains(autocorrectionThree));
	}

	@Test
	void should_retrieve_all_autocorrection_from_missing_bucket() {

		var bucketOne = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		var bucketTwo = EntitiesUtils.getEntity(BUCKET_TWO, bucketService, sf);
		var autocorrectionOne =
			EntitiesUtils.getAutocorrection(AUTOCORRECTION_ONE, autocorrectionService, sf);
		var autocorrectionTwo =
			EntitiesUtils.getAutocorrection(AUTOCORRECTION_TWO, autocorrectionService, sf);
		var autocorrectionThree =
			EntitiesUtils.getAutocorrection(AUTOCORRECTION_THREE, autocorrectionService, sf);

		bindAutocorrectionToBucket(bucketOne, autocorrectionOne);

		// Associates a autocorrectionTwo with another Bucket.
		bindAutocorrectionToBucket(bucketTwo, autocorrectionTwo);

		List<Autocorrection> unboundAutocorrections = getUnboundAutocorrections(0L);

		assertFalse(unboundAutocorrections.isEmpty());
		assertEquals(allAutocorrectionCount, unboundAutocorrections.size());
		assertTrue(unboundAutocorrections.contains(autocorrectionOne));
		assertTrue(unboundAutocorrections.contains(autocorrectionTwo));
		assertTrue(unboundAutocorrections.contains(autocorrectionThree));
	}
	@AfterEach
	void tearDown() {
		EntitiesUtils.removeEntity(BUCKET_ONE, bucketService, sf);
		EntitiesUtils.removeEntity(BUCKET_TWO, bucketService, sf);

		EntitiesUtils.removeEntity(AUTOCORRECTION_ONE, autocorrectionService, sf);
		EntitiesUtils.removeEntity(AUTOCORRECTION_TWO, autocorrectionService, sf);
		EntitiesUtils.removeEntity(AUTOCORRECTION_THREE, autocorrectionService, sf);
	}

	private void bindAutocorrectionToBucket(Bucket bucket, Autocorrection autocorrection) {
		bucketService.bindAutocorrection(bucket.getId(), autocorrection.getId())
			.await()
			.indefinitely();
	}

	private void unbindAutocorrectionToBucket(Bucket bucket) {
		bucketService.unbindAutocorrection(bucket.getId())
			.await()
			.indefinitely();
	}

	private List<Autocorrection> getUnboundAutocorrections(Long bucketId) {
		return autocorrectionService.findUnboundAutocorrectionByBucket(bucketId)
			.await()
			.indefinitely();
	}
}
