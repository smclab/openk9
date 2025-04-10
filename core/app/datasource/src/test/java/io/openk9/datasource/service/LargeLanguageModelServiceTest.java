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

import io.openk9.datasource.model.LargeLanguageModel;
import io.openk9.datasource.model.dto.base.LargeLanguageModelDTO;
import io.openk9.datasource.model.dto.base.ProviderModelDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class LargeLanguageModelServiceTest {

	private static final int CONTEXT_WINDOW = 1500;
	private static final String API_KEY = "EMST.asdfkaslf01432kl4l1";
	private static final String URL = "http://EMST.embeddingapi.local";

	private static final String ENTITY_NAME_PREFIX = "LargeLanguageModelServiceTest - ";
	private static final String EMPTY_STRING = "";
	private static final String LARGE_LANGUAGE_MODEL_ONE_NAME = ENTITY_NAME_PREFIX + "Large language model 1 ";
	private static final String JSON_CONFIG_EMPTY = "{}";
	private static final String PROVIDER = "provider";
	private static final String MODEL = "model";

	@Inject
	LargeLanguageModelService largeLanguageModelService;

	@Inject
	Mutiny.SessionFactory sessionFactory;


	@Test
	void should_create_embedding_model_one() {
		createLargeLanguageModelOne();

		var largeLanguageModelOne = getLargeLanguageModelOne();

		assertNotNull(largeLanguageModelOne);

		assertEquals(LARGE_LANGUAGE_MODEL_ONE_NAME, largeLanguageModelOne.getName());
		assertEquals(API_KEY, largeLanguageModelOne.getApiKey());
		assertEquals(URL, largeLanguageModelOne.getApiUrl());
		assertEquals(JSON_CONFIG_EMPTY, largeLanguageModelOne.getJsonConfig());
		assertEquals(PROVIDER, largeLanguageModelOne.getProviderModel().getProvider());
		assertEquals(MODEL, largeLanguageModelOne.getProviderModel().getModel());
		assertEquals(CONTEXT_WINDOW, largeLanguageModelOne.getContextWindow());
		assertTrue(largeLanguageModelOne.getRetrieveCitations());

		removeLargeLanguageModelOne();
	}

	@Test
	void should_create_embedding_model_one_with_missing_providerModel() {
		var dto = LargeLanguageModelDTO.builder()
			.name(LARGE_LANGUAGE_MODEL_ONE_NAME)
			.apiKey(API_KEY)
			.apiUrl(URL)
			.jsonConfig(JSON_CONFIG_EMPTY)
			.contextWindow(CONTEXT_WINDOW)
			.retrieveCitations(true)
			.build();

		createLargeLanguageModel(dto);

		var largeLanguageModelOne = getLargeLanguageModelOne();

		assertNotNull(largeLanguageModelOne);

		assertEquals(LARGE_LANGUAGE_MODEL_ONE_NAME, largeLanguageModelOne.getName());
		assertEquals(API_KEY, largeLanguageModelOne.getApiKey());
		assertEquals(URL, largeLanguageModelOne.getApiUrl());
		assertEquals(JSON_CONFIG_EMPTY, largeLanguageModelOne.getJsonConfig());
		assertEquals(EMPTY_STRING, largeLanguageModelOne.getProviderModel().getProvider());
		assertEquals(EMPTY_STRING, largeLanguageModelOne.getProviderModel().getModel());
		assertEquals(CONTEXT_WINDOW, largeLanguageModelOne.getContextWindow());
		assertTrue(largeLanguageModelOne.getRetrieveCitations());

		removeLargeLanguageModelOne();
	}

	@Test
	void should_create_embedding_model_one_with_missing_model_provider() {
		var dto = LargeLanguageModelDTO.builder()
			.name(LARGE_LANGUAGE_MODEL_ONE_NAME)
			.apiKey(API_KEY)
			.apiUrl(URL)
			.jsonConfig(JSON_CONFIG_EMPTY)
			.contextWindow(CONTEXT_WINDOW)
			.retrieveCitations(true)
			.providerModel(null)
			.build();

		createLargeLanguageModel(dto);

		var largeLanguageModelOne = getLargeLanguageModelOne();

		assertNotNull(largeLanguageModelOne);

		assertEquals(LARGE_LANGUAGE_MODEL_ONE_NAME, largeLanguageModelOne.getName());
		assertEquals(API_KEY, largeLanguageModelOne.getApiKey());
		assertEquals(URL, largeLanguageModelOne.getApiUrl());
		assertEquals(JSON_CONFIG_EMPTY, largeLanguageModelOne.getJsonConfig());
		assertEquals(EMPTY_STRING, largeLanguageModelOne.getProviderModel().getProvider());
		assertEquals(EMPTY_STRING, largeLanguageModelOne.getProviderModel().getModel());
		assertEquals(CONTEXT_WINDOW, largeLanguageModelOne.getContextWindow());
		assertTrue(largeLanguageModelOne.getRetrieveCitations());

		removeLargeLanguageModelOne();
	}

	private LargeLanguageModel createLargeLanguageModel(LargeLanguageModelDTO dto) {
		return sessionFactory.withTransaction(
				session -> largeLanguageModelService.create(session, dto)
			)
			.await()
			.indefinitely();
	}

	private LargeLanguageModel createLargeLanguageModelOne() {
		var dto = LargeLanguageModelDTO.builder()
			.name(LARGE_LANGUAGE_MODEL_ONE_NAME)
			.apiKey(API_KEY)
			.apiUrl(URL)
			.jsonConfig(JSON_CONFIG_EMPTY)
			.providerModel(
				ProviderModelDTO.builder()
					.provider(PROVIDER)
					.model(MODEL)
					.build()
			)
			.contextWindow(CONTEXT_WINDOW)
			.retrieveCitations(true)
			.build();

		return sessionFactory.withTransaction(
				session -> largeLanguageModelService.create(session, dto)
			)
			.await()
			.indefinitely();
	}

	private LargeLanguageModel getLargeLanguageModelOne() {
		return sessionFactory.withTransaction(
			session ->
				largeLanguageModelService.findByName(session, LARGE_LANGUAGE_MODEL_ONE_NAME)
		)
		.await()
		.indefinitely();
	}

	private void removeLargeLanguageModelOne() {
		var largeLanguageModel = getLargeLanguageModelOne();

		sessionFactory.withTransaction(
			session ->
				largeLanguageModelService.deleteById(session, largeLanguageModel.getId())
		)
		.await()
		.indefinitely();
	}
}
