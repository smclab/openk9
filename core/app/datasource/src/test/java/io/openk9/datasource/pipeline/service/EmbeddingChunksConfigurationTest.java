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

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.Initializer;
import io.openk9.datasource.service.EmbeddingModelService;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Covers the embedding of a document on a tenant with no active embedding
 * model: the failure has to name the missing model, whatever the exception
 * that carries it.
 */
@QuarkusTest
class EmbeddingChunksConfigurationTest {

	private static final String TENANT_ID = "public";

	@Inject
	EmbeddingModelService embeddingModelService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@AfterEach
	void tearDown() {
		var embeddingModel = EntitiesUtils.getEntity(
			Initializer.EMBEDDING_MODEL_DEFAULT_PRIMARY,
			embeddingModelService,
			sessionFactory
		);

		embeddingModelService.enable(embeddingModel.getId())
			.await().indefinitely();
	}

	@Test
	@DisplayName("Should name the missing model when no embedding model is active")
	void should_fail_with_a_clear_message_without_active_embedding_model() {
		// no embedding model is active on the tenant
		sessionFactory.withTransaction((s, t) -> s
				.createNativeQuery(
					"UPDATE tenant_binding SET embedding_model_id = NULL")
				.executeUpdate())
			.await().indefinitely();

		// the embedding of a document is requested on a new schedule
		var payload = "{}".getBytes(StandardCharsets.UTF_8);
		var failure = Assertions.assertThrows(
			Throwable.class,
			() -> EmbeddingService.getEmbeddedPayload(
				TENANT_ID, UUID.randomUUID().toString(), payload)
				.toCompletableFuture().join()
		);

		// and it says that the tenant has no active embedding model
		Assertions.assertTrue(
			String.valueOf(failure.getMessage())
				.contains("no active embedding model"),
			String.valueOf(failure)
		);
	}

}
