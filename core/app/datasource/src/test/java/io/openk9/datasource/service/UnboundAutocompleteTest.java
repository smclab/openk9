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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.Autocomplete;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.dto.base.AutocompleteDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class UnboundAutocompleteTest {

	private static final String ENTITY_NAME_PREFIX = "UnboundAutocompleteTest - ";

	private static final String AUTOCOMPLETE_ONE = ENTITY_NAME_PREFIX + "Autocomplete 1";
	private static final String AUTOCOMPLETE_TWO = ENTITY_NAME_PREFIX + "Autocomplete 2";
	private static final String AUTOCOMPLETE_THREE = ENTITY_NAME_PREFIX + "Autocomplete 3";
	private static final String BUCKET_ONE = ENTITY_NAME_PREFIX + "Bucket 1";
	private static final String BUCKET_TWO = ENTITY_NAME_PREFIX + "Bucket 2";
	private static final String DOC_TYPE_FIELD_NAME_ONE = ENTITY_NAME_PREFIX + "Doc type field 1";
	private static final String DOC_TYPE_FIELD_NAME_TWO = ENTITY_NAME_PREFIX + "Doc type field 2";

	private int allAutocompleteCount = 0;

	@Inject
	AutocompleteService autocompleteService;

	@Inject
	BucketService bucketService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	Mutiny.SessionFactory sf;

	@BeforeEach
	void setup() {
		// Create bucketOne and bucketTwo
		EntitiesUtils.createBucket(BUCKET_ONE, bucketService, sf);
		EntitiesUtils.createBucket(BUCKET_TWO, bucketService, sf);

		// Create DocTypeField one and two as child of the first sample field of type TEXT
		var allDocTypeFields = EntitiesUtils.getAllEntities(docTypeFieldService, sf);

		var firstSampleTextField = allDocTypeFields.stream()
			.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
			.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
			.findFirst();

		var docTypeFieldId = 0L;

		if (firstSampleTextField.isPresent()) {
			docTypeFieldId = firstSampleTextField.get().getId();
		}

		DocTypeFieldDTO fieldDtoOne = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME_ONE)
			.fieldName("sample.searchasyoutypeone")
			.fieldType(FieldType.SEARCH_AS_YOU_TYPE)
			.build();
		DocTypeFieldDTO fieldDtoTwo = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME_TWO)
			.fieldName("sample.searchasyoutypetwo")
			.fieldType(FieldType.SEARCH_AS_YOU_TYPE)
			.build();

		EntitiesUtils.createSubField(docTypeFieldId, fieldDtoOne, docTypeFieldService);
		EntitiesUtils.createSubField(docTypeFieldId, fieldDtoTwo, docTypeFieldService);

		var fieldIds =
			EntitiesUtils.getAllSearchAsYouTypeDocTypeFieldWithParent(docTypeFieldService, sf)
				.stream()
				.map(DocTypeField::getId)
				.collect(Collectors.toSet());

		// Create Autocomplete one, two and three
		AutocompleteDTO autocompleteDTOOne = AutocompleteDTO.builder()
			.name(AUTOCOMPLETE_ONE)
			.fieldIds(fieldIds)
			.build();
		AutocompleteDTO autocompleteDTOTwo = AutocompleteDTO.builder()
			.name(AUTOCOMPLETE_TWO)
			.fieldIds(fieldIds)
			.build();
		AutocompleteDTO autocompleteDTOThree = AutocompleteDTO.builder()
			.name(AUTOCOMPLETE_THREE)
			.fieldIds(fieldIds)
			.build();

		EntitiesUtils.createEntity(autocompleteDTOOne, autocompleteService, sf);
		EntitiesUtils.createEntity(autocompleteDTOTwo, autocompleteService, sf);
		EntitiesUtils.createEntity(autocompleteDTOThree, autocompleteService, sf);

		allAutocompleteCount = 3;
	}

	@Test
	void should_bind_and_unbind_autocomplete_to_bucket_one() {
		var bucket = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		var autocompleteOne =
			EntitiesUtils.getAutocomplete(AUTOCOMPLETE_ONE, autocompleteService, sf);
		var autocompleteTwo =
			EntitiesUtils.getAutocomplete(AUTOCOMPLETE_TWO, autocompleteService, sf);

		// Initial check
		assertNull(bucket.getAutocomplete());

		// bind autocomplete
		bindAutocompleteToBucket(bucket, autocompleteOne);

		bucket = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		assertEquals(autocompleteOne.getId(), bucket.getAutocomplete().getId());

		// override bind autocomplete
		bindAutocompleteToBucket(bucket, autocompleteTwo);

		bucket = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		assertEquals(autocompleteTwo.getId(), bucket.getAutocomplete().getId());

		// unbind autocomplete
		unbindAutocompleteToBucket(bucket);

		bucket = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		assertNull(bucket.getAutocorrection());
	}

	@Test
	void should_retrieve_all_autocompletes_from_empty_bucket() {

		var bucket = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		var autocompleteOne =
			EntitiesUtils.getAutocomplete(AUTOCOMPLETE_ONE, autocompleteService, sf);
		var autocompleteTwo =
			EntitiesUtils.getAutocomplete(AUTOCOMPLETE_TWO, autocompleteService, sf);

		List<Autocomplete> unboundAutocompletes = getUnboundAutocompletes(bucket.getId());

		assertFalse(unboundAutocompletes.isEmpty());
		assertEquals(allAutocompleteCount, unboundAutocompletes.size());
		assertTrue(unboundAutocompletes.contains(autocompleteOne));
		assertTrue(unboundAutocompletes.contains(autocompleteTwo));
	}

	@Test
	void should_retrieve_all_autocompletes_except_the_one_associated_with_bucket_one() {

		var bucketOne = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		var bucketTwo = EntitiesUtils.getEntity(BUCKET_TWO, bucketService, sf);
		var autocompleteOne =
			EntitiesUtils.getAutocomplete(AUTOCOMPLETE_ONE, autocompleteService, sf);
		var autocompleteTwo =
			EntitiesUtils.getAutocomplete(AUTOCOMPLETE_TWO, autocompleteService, sf);
		var autocompleteThree =
			EntitiesUtils.getAutocomplete(AUTOCOMPLETE_THREE, autocompleteService, sf);

		bindAutocompleteToBucket(bucketOne, autocompleteOne);

		// Associates the autocompleteTwo with another Bucket.
		bindAutocompleteToBucket(bucketTwo, autocompleteTwo);

		List<Autocomplete> unboundAutocompletes =
			getUnboundAutocompletes(bucketOne.getId());

		assertFalse(unboundAutocompletes.isEmpty());
		assertEquals(allAutocompleteCount - 1, unboundAutocompletes.size());
		assertFalse(unboundAutocompletes.contains(autocompleteOne));
		assertTrue(unboundAutocompletes.contains(autocompleteTwo));
		assertTrue(unboundAutocompletes.contains(autocompleteThree));
	}

	@Test
	void should_retrieve_all_autocomplete_from_missing_bucket() {

		var bucketOne = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sf);
		var bucketTwo = EntitiesUtils.getEntity(BUCKET_TWO, bucketService, sf);
		var autocompleteOne =
			EntitiesUtils.getAutocomplete(AUTOCOMPLETE_ONE, autocompleteService, sf);
		var autocompleteTwo =
			EntitiesUtils.getAutocomplete(AUTOCOMPLETE_TWO, autocompleteService, sf);
		var autocompleteThree =
			EntitiesUtils.getAutocomplete(AUTOCOMPLETE_THREE, autocompleteService, sf);

		bindAutocompleteToBucket(bucketOne, autocompleteOne);

		// Associates the autocompleteTwo with another Bucket.
		bindAutocompleteToBucket(bucketTwo, autocompleteTwo);

		List<Autocomplete> unboundAutocompletes = getUnboundAutocompletes(0L);

		assertFalse(unboundAutocompletes.isEmpty());
		assertEquals(allAutocompleteCount, unboundAutocompletes.size());
		assertTrue(unboundAutocompletes.contains(autocompleteOne));
		assertTrue(unboundAutocompletes.contains(autocompleteTwo));
		assertTrue(unboundAutocompletes.contains(autocompleteThree));
	}
	@AfterEach
	void tearDown() {
		EntitiesUtils.removeEntity(BUCKET_ONE, bucketService, sf);
		EntitiesUtils.removeEntity(BUCKET_TWO, bucketService, sf);

		EntitiesUtils.removeEntity(AUTOCOMPLETE_ONE, autocompleteService, sf);
		EntitiesUtils.removeEntity(AUTOCOMPLETE_TWO, autocompleteService, sf);
		EntitiesUtils.removeEntity(AUTOCOMPLETE_THREE, autocompleteService, sf);

		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_NAME_ONE, docTypeFieldService, sf);
		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_NAME_TWO, docTypeFieldService, sf);
	}

	private void bindAutocompleteToBucket(Bucket bucket, Autocomplete autocomplete) {
		bucketService.bindAutocomplete(bucket.getId(), autocomplete.getId())
			.await()
			.indefinitely();
	}

	private void unbindAutocompleteToBucket(Bucket bucket) {
		bucketService.unbindAutocomplete(bucket.getId())
			.await()
			.indefinitely();
	}

	private List<Autocomplete> getUnboundAutocompletes(Long bucketId) {
		return autocompleteService.findUnboundAutocompleteByBucket(bucketId)
			.await()
			.indefinitely();
	}
}
