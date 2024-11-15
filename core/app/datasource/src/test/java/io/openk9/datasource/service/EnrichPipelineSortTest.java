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

import io.openk9.datasource.graphql.dto.PipelineWithItemsDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class EnrichPipelineSortTest {

	@Inject
	EnrichPipelineService enrichPipelineService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	void should_sort_with_different_order() {

		var enrichPipeline = sessionFactory.withTransaction((s, t) ->
				enrichPipelineService
					.findByName(s, CreateConnection.PIPELINE_NAME)
					.call(pipeline -> Mutiny.fetch(
						pipeline.getEnrichPipelineItems()))
			)
			.await()
			.indefinitely();

		var firstItem = enrichPipeline.getEnrichPipelineItems().iterator().next();

		assertEquals(CreateConnection.PIPELINE_NAME, enrichPipeline.getName());
		assertEquals(2L, firstItem.getEnrichItem().getId());
		assertEquals(1.0f, firstItem.getWeight());

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(CreateConnection.PIPELINE_NAME)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(2L)
						.weight(3L)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(3L)
						.weight(4L)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(4L)
						.weight(5L)
						.build()
					).build(),
				true
			)
			.await()
			.indefinitely();

		var enrichPipelineUpdated = sessionFactory.withTransaction((s, t) ->
			enrichPipelineService
				.findByName(s, CreateConnection.PIPELINE_NAME)
				.call(pipeline -> Mutiny.fetch(
					pipeline.getEnrichPipelineItems()))
		).await().indefinitely();

		var firstItemUpdated = enrichPipelineUpdated.getEnrichPipelineItems().iterator().next();

		assertEquals(2L, firstItemUpdated.getEnrichItem().getId());
		assertEquals(3.0f, firstItemUpdated.getWeight());

	}

}
