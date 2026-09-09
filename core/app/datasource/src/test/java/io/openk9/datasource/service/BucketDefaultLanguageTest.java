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

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.dto.request.BucketWithListsDTO;
import io.openk9.datasource.model.init.Languages;
import io.openk9.datasource.model.util.K9Entity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The default language of a Bucket must always be one of the languages associated to it,
 * otherwise the Bucket ends up searching in a language no client can ask for explicitly.
 */
@QuarkusTest
public class BucketDefaultLanguageTest {

	private static final String ENTITY_NAME_PREFIX = "BucketDefaultLanguageTest - ";

	private static final String BUCKET_NAME = ENTITY_NAME_PREFIX + "Bucket";

	@Inject
	BucketService bucketService;

	@Inject
	LanguageService languageService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		createBucket(Set.of(getItalian().getId()), null);
	}

	@AfterEach
	void tearDown() {
		EntitiesUtils.cleanBucket(getBucket(), bucketService);
		EntitiesUtils.removeEntity(BUCKET_NAME, bucketService, sessionFactory);
	}

	@Test
	void should_bind_an_available_language_as_default() {
		var bucket = getBucket();
		var italian = getItalian();

		bucketService.bindLanguage(bucket.getId(), italian.getId())
			.await().indefinitely();

		var bound = getBucket();

		assertNotNull(bound.getDefaultLanguage());
		assertEquals(italian.getId(), bound.getDefaultLanguage().getId());
	}

	@Test
	void should_not_bind_an_unavailable_language_as_default() {
		var bucket = getBucket();
		var english = getEnglish();

		assertThrows(
			ValidationException.class,
			() -> bucketService.bindLanguage(bucket.getId(), english.getId())
				.await().indefinitely()
		);

		assertNull(getBucket().getDefaultLanguage());
	}

	@Test
	void should_update_with_a_default_language_added_by_the_same_request() {
		var bucket = getBucket();
		var italian = getItalian();
		var english = getEnglish();

		bucketService.update(bucket.getId(), bucketDto(
				Set.of(italian.getId(), english.getId()), english.getId())
			)
			.await().indefinitely();

		var updated = getBucket();

		assertEquals(2, updated.getAvailableLanguages().size());
		assertEquals(english.getId(), updated.getDefaultLanguage().getId());
	}

	@Test
	void should_not_update_with_a_default_language_outside_the_final_language_set() {
		var bucket = getBucket();
		var italian = getItalian();
		var english = getEnglish();

		assertThrows(
			ValidationException.class,
			() -> bucketService.update(
					bucket.getId(), bucketDto(Set.of(italian.getId()), english.getId()))
				.await().indefinitely()
		);

		var unchanged = getBucket();

		assertNull(unchanged.getDefaultLanguage());
		assertEquals(1, unchanged.getAvailableLanguages().size());
	}

	@Test
	void should_patch_a_default_language_against_the_languages_already_associated() {
		var bucket = getBucket();
		var italian = getItalian();

		bucketService.patch(bucket.getId(), BucketWithListsDTO.builder()
				.name(BUCKET_NAME)
				.refreshOnDate(false)
				.refreshOnQuery(false)
				.refreshOnTab(false)
				.refreshOnSuggestionCategory(false)
				.retrieveType(Bucket.RetrieveType.TEXT)
				.defaultLanguageId(italian.getId())
				.build()
			)
			.await().indefinitely();

		assertEquals(italian.getId(), getBucket().getDefaultLanguage().getId());
	}

	@Test
	void should_not_patch_a_default_language_not_associated_to_the_bucket() {
		var bucket = getBucket();
		var english = getEnglish();

		assertThrows(
			ValidationException.class,
			() -> bucketService.patch(bucket.getId(), BucketWithListsDTO.builder()
					.name(BUCKET_NAME)
					.refreshOnDate(false)
					.refreshOnQuery(false)
					.refreshOnTab(false)
					.refreshOnSuggestionCategory(false)
					.retrieveType(Bucket.RetrieveType.TEXT)
					.defaultLanguageId(english.getId())
					.build()
				)
				.await().indefinitely()
		);

		assertNull(getBucket().getDefaultLanguage());
	}

	@Test
	void should_keep_the_default_language_when_a_patch_sends_the_same_languages() {
		var bucket = getBucket();
		var italian = getItalian();
		var english = getEnglish();
		var languageIds = Set.of(italian.getId(), english.getId());

		bucketService.update(bucket.getId(), bucketDto(languageIds, italian.getId()))
			.await().indefinitely();

		bucketService.patch(bucket.getId(), bucketDto(languageIds, null))
			.await().indefinitely();

		var patched = getBucket();

		assertEquals(italian.getId(), patched.getDefaultLanguage().getId());
		assertEquals(2, patched.getAvailableLanguages().size());
	}

	@Test
	void should_clear_the_default_language_when_its_association_is_removed() {
		var bucket = getBucket();
		var italian = getItalian();
		var english = getEnglish();

		bucketService.update(
				bucket.getId(),
				bucketDto(Set.of(italian.getId(), english.getId()), italian.getId())
			)
			.await().indefinitely();

		bucketService.removeLanguage(bucket.getId(), italian.getId())
			.await().indefinitely();

		var withoutItalian = getBucket();

		assertNull(withoutItalian.getDefaultLanguage());
		assertEquals(1, withoutItalian.getAvailableLanguages().size());
		assertEquals(
			english.getId(),
			withoutItalian.getAvailableLanguages().iterator().next().getId()
		);
	}

	@Test
	void should_keep_the_default_language_when_another_association_is_removed() {
		var bucket = getBucket();
		var italian = getItalian();
		var english = getEnglish();

		bucketService.update(
				bucket.getId(),
				bucketDto(Set.of(italian.getId(), english.getId()), italian.getId())
			)
			.await().indefinitely();

		bucketService.removeLanguage(bucket.getId(), english.getId())
			.await().indefinitely();

		var withoutEnglish = getBucket();

		assertEquals(italian.getId(), withoutEnglish.getDefaultLanguage().getId());
	}

	@Test
	void should_unbind_the_default_language_without_touching_the_associations() {
		var bucket = getBucket();
		var italian = getItalian();

		bucketService.bindLanguage(bucket.getId(), italian.getId())
			.await().indefinitely();

		bucketService.unbindLanguage(bucket.getId())
			.await().indefinitely();

		var unbound = getBucket();

		assertNull(unbound.getDefaultLanguage());
		assertTrue(unbound.getAvailableLanguages()
			.stream()
			.map(K9Entity::getId)
			.anyMatch(id -> id.equals(italian.getId())));
	}

	private static BucketWithListsDTO bucketDto(Set<Long> languageIds, Long defaultLanguageId) {
		return BucketWithListsDTO.builder()
			.name(BUCKET_NAME)
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.refreshOnTab(false)
			.refreshOnSuggestionCategory(false)
			.retrieveType(Bucket.RetrieveType.TEXT)
			.languageIds(languageIds)
			.defaultLanguageId(defaultLanguageId)
			.build();
	}

	private void createBucket(Set<Long> languageIds, Long defaultLanguageId) {
		EntitiesUtils.createEntity(
			bucketDto(languageIds, defaultLanguageId), bucketService, sessionFactory);
	}

	private Bucket getBucket() {
		return sessionFactory.withTransaction((s, t) ->
				bucketService.findByName(s, BUCKET_NAME)
					.call(entity -> Mutiny.fetch(entity.getAvailableLanguages()))
					.call(entity -> Mutiny.fetch(entity.getDefaultLanguage()))
			)
			.await().indefinitely();
	}

	private Language getEnglish() {
		return getLanguage(Languages.ENGLISH.getName());
	}

	private Language getItalian() {
		return getLanguage(Languages.ITALIAN.getName());
	}

	private Language getLanguage(String name) {
		return sessionFactory.withTransaction((s, t) -> languageService.findByName(s, name))
			.await().indefinitely();
	}

}
