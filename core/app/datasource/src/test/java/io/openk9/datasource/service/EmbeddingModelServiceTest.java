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

import java.io.IOException;

import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.dto.base.ProviderModelDTO;
import jakarta.inject.Inject;

import io.openk9.datasource.index.EmbeddingComponentTemplate;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class EmbeddingModelServiceTest {

	public static final String EMBEDDING_MODEL_NAME = "EMST.embeddingmodeltest";
	public static final int VECTOR_SIZE = 1330;
	public static final String API_KEY = "EMST.asdfkaslf01432kl4l1";
	public static final String URL = "http://EMST.embeddingapi.local";

	private static final String ENTITY_NAME_PREFIX = "EmbeddingModelGraphqlTest - ";
	private static final String EMBEDDING_MODEL_ONE_NAME = ENTITY_NAME_PREFIX + "Embedding model 1 ";
	private static final String JSON_CONFIG_EMPTY = "{}";
	private static final String PROVIDER = "provider";
	private static final String MODEL = "model";

	@Inject
	EmbeddingModelService embeddingModelService;

	@Inject
	OpenSearchClient openSearchClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	@RunOnVertxContext
	void should_create_embedding_model_service_with_component_template(UniAsserter asserter) {
		var req = new EmbeddingComponentTemplate(
			"public",
			EMBEDDING_MODEL_NAME,
			VECTOR_SIZE
		);

		asserter.assertThat(
			() -> embeddingModelService
				.create(EmbeddingModelDTO.builder()
					.name(EMBEDDING_MODEL_NAME)
					.vectorSize(VECTOR_SIZE)
					.apiKey(API_KEY)
					.apiUrl(URL)
					.build()),
			embeddingModel -> {
				var cluster = openSearchClient.cluster();
				try {
					var res = cluster.getComponentTemplate(ct -> ct
						.name(req.getName()));

					var componentTemplate = res.componentTemplates().getFirst();

					Assertions.assertEquals(
						req.getName(),
						componentTemplate.name()
					);

				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		);
	}

	@Test
	void should_create_embedding_model_one_with_provider_model_jsonConfig_fields() {
		createEmbeddingModelOne();

		var embeddingModelOne = getEmbeddingModelOne();

		assertNotNull(embeddingModelOne);

		assertEquals(EMBEDDING_MODEL_ONE_NAME, embeddingModelOne.getName());
		assertEquals(PROVIDER, embeddingModelOne.getProviderModel().getProvider());
		assertEquals(MODEL, embeddingModelOne.getProviderModel().getModel());
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
			.providerModel(
				ProviderModelDTO
					.builder()
					.provider(PROVIDER)
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
