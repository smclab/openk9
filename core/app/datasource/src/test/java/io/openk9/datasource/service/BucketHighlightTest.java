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

import java.util.Set;
import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.Highlight;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.base.HighlightDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
public class BucketHighlightTest {

	private static final String ENTITY_NAME_PREFIX = "BucketHighlightTest - ";
	private static final String BUCKET_NAME = ENTITY_NAME_PREFIX + "Bucket";
	private static final String HIGHLIGHT_NAME = ENTITY_NAME_PREFIX + "Highlight";
	private static final String FIELD_NAME = ENTITY_NAME_PREFIX + "field";

	@Inject
	BucketService bucketService;

	@Inject
	HighlightService highlightService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		createDocTypeField();

		highlightService.create(
			HighlightDTO.builder()
				.name(HIGHLIGHT_NAME)
				.type(Highlight.HighlightType.UNIFIED)
				.fieldIds(Set.of(getDocTypeFieldId()))
				.build()
		).await().indefinitely();

		EntitiesUtils.createBucket(
			BUCKET_NAME, bucketService, sessionFactory);
	}

	@Test
	void should_bind_highlight_to_bucket() {
		var bucket =
			EntitiesUtils.getEntity(
				BUCKET_NAME, bucketService, sessionFactory);
		var highlight =
			EntitiesUtils.getEntity(
				HIGHLIGHT_NAME, highlightService, sessionFactory);

		var result = bucketService.bindHighlight(bucket.getId(), highlight.getId())
			.await().indefinitely();

		assertEquals(highlight.getId(), result.right.getId());

		// the Bucket now exposes the linked Highlight
		var bound = bucketService.getHighlight(bucket.getId())
			.await().indefinitely();

		assertNotNull(bound);
		assertEquals(highlight.getId(), bound.getId());
	}

	@Test
	void should_unbind_highlight_from_bucket() {
		var bucket =
			EntitiesUtils.getEntity(
				BUCKET_NAME, bucketService, sessionFactory);
		var highlight =
			EntitiesUtils.getEntity(
				HIGHLIGHT_NAME, highlightService, sessionFactory);

		bucketService.bindHighlight(bucket.getId(), highlight.getId())
			.await().indefinitely();

		var result = bucketService.unbindHighlight(bucket.getId())
			.await().indefinitely();

		assertNull(result.right);

		// the Bucket no longer exposes a Highlight
		var bound = bucketService.getHighlight(bucket.getId())
			.await().indefinitely();

		assertNull(bound);
	}

	@Test
	void should_not_bind_when_highlight_does_not_exist() {
		var bucket =
			EntitiesUtils.getEntity(
				BUCKET_NAME, bucketService, sessionFactory);

		var result = bucketService
			.bindHighlight(bucket.getId(), 0L)
			.await().indefinitely();

		assertNull(result);

		var bound = bucketService.getHighlight(bucket.getId())
			.await().indefinitely();

		assertNull(bound);
	}

	@AfterEach
	void tearDown() {
		EntitiesUtils.removeEntity(
			BUCKET_NAME, bucketService, sessionFactory);

		EntitiesUtils.removeEntity(
			HIGHLIGHT_NAME, highlightService, sessionFactory);

		EntitiesUtils.removeEntity(
			FIELD_NAME, docTypeFieldService, sessionFactory);
	}

	private Long getDocTypeFieldId() {
		return EntitiesUtils.getEntity(
			BucketHighlightTest.FIELD_NAME, docTypeFieldService, sessionFactory)
			.getId();
	}

	private void createDocTypeField() {
		var docTypeField = DocTypeFieldDTO.builder()
			.name(BucketHighlightTest.FIELD_NAME)
			.fieldName(BucketHighlightTest.FIELD_NAME)
			.searchable(true)
			.sortable(true)
			.fieldType(FieldType.BINARY)
			.build();

		EntitiesUtils.createEntity(
			docTypeField, docTypeFieldService, sessionFactory);
	}
}
