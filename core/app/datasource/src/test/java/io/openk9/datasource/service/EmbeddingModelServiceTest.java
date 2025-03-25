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
import jakarta.inject.Inject;

import io.openk9.datasource.index.EmbeddingComponentTemplate;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;

@QuarkusTest
public class EmbeddingModelServiceTest {

	public static final String EMBEDDING_MODEL_NAME = "EMST.embeddingmodeltest";
	public static final int VECTOR_SIZE = 1330;
	public static final String API_KEY = "EMST.asdfkaslf01432kl4l1";
	public static final String URL = "http://EMST.embeddingapi.local";
	@Inject
	EmbeddingModelService embeddingModelService;
	@Inject
	OpenSearchClient openSearchClient;

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

}
