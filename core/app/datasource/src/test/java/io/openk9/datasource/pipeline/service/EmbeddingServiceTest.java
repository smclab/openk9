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

package io.openk9.datasource.pipeline.service;

import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.dto.EmbeddingModelDTO;
import io.openk9.datasource.model.dto.ModelTypeDTO;
import io.openk9.datasource.service.EmbeddingModelService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class EmbeddingServiceTest {

	private static final String ENTITY_NAME_PREFIX = "EmbeddingModelGraphqlTest - ";
	private static final String EMBEDDING_MODEL_ONE_NAME = ENTITY_NAME_PREFIX + "Embedding model 1 ";
	private static final String JSON_CONFIG_EMPTY = "{}";
	private static final String TYPE = "type";
	private static final String MODEL = "model";

	@Inject
	EmbeddingModelService embeddingModelService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	void should_fetch_current_embedding_model() {

		var current = sessionFactory.withTransaction(session -> session
				.createNamedQuery(EmbeddingModel.FETCH_CURRENT, EmbeddingModel.class)
				.getSingleResultOrNull()
			)
			.await().indefinitely();

		assertNotNull(current);
	}

	@Test
	void should_create_embedding_model_one_with_type_model_jsonConfig_fields() {
		var embeddingModelCreated = createEmbeddingModelOne();
		System.out.printf("embeddingModelCreated: %s", embeddingModelCreated.toString());

		var embeddingModelOne = getEmbeddingModelOne();
		System.out.printf("embeddingModelOne: %s", embeddingModelOne.toString());

		assertNotNull(embeddingModelOne);

		assertEquals(EMBEDDING_MODEL_ONE_NAME, embeddingModelOne.getName());
		assertEquals(TYPE, embeddingModelOne.getModelType().getType());
		assertEquals(MODEL, embeddingModelOne.getModelType().getModel());
		assertEquals(JSON_CONFIG_EMPTY, embeddingModelOne.getJsonConfig());

		removeEmbeddingModelOne();
	}

	private EmbeddingModel createEmbeddingModelOne() {
		var dto = EmbeddingModelDTO
			.builder()
			.name(EMBEDDING_MODEL_ONE_NAME)
			.apiUrl("embedding-model.local")
			.apiKey("secret-key")
			.vectorSize(1500)
			.jsonConfig(JSON_CONFIG_EMPTY)
			.modelType(
				ModelTypeDTO
					.builder()
					.type(TYPE)
					.model(MODEL)
					.build()
			)
			.build();

		return embeddingModelService.create(dto)
			.await()
			.indefinitely();
	}

	private EmbeddingModel getEmbeddingModelOne() {
		return sessionFactory.withTransaction(
				s -> embeddingModelService.findByName(s, EMBEDDING_MODEL_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private void removeEmbeddingModelOne() {
		var embeddingModelOne = getEmbeddingModelOne();

		sessionFactory.withTransaction(
				session -> embeddingModelService.deleteById(embeddingModelOne.getId())
			)
			.await()
			.indefinitely();
	}
}