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
import io.openk9.datasource.model.EnrichPipeline;
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

		patchPipelineWithThreeItems();

		changeItemsOrderWithPatch();

	}

	@Test
	void should_removes_an_item_from_the_pipeline() {

		patchPipelineWithThreeItems();

		patchPipelineWithTwoItems();

	}

	@Test
	void should_update_enrich_items_in_pipeline() {

		patchPipelineWithThreeItems();

		updateEnrichPipelineWithOneItem();

	}

	@Test
	void should_not_change_items_with_patch() {

		patchPipelineWithThreeItems();

		patchWithoutPipeline();

		var enrichPipeline = getEnrichPipeline();

		assertEquals(3, enrichPipeline.getEnrichPipelineItems().size());

	}

	@Test
	void should_remove_items_with_update() {

		patchPipelineWithThreeItems();

		updateWithoutPipeline();

		var enrichPipeline = getEnrichPipeline();

		assertEquals(0, enrichPipeline.getEnrichPipelineItems().size());

	}

	private void changeItemsOrderWithPatch() {

		var enrichPipeline = getEnrichPipeline();

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(CreateConnection.PIPELINE_NAME)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(2L)
						.weight(2.0f)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(3L)
						.weight(1.0f)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(4L)
						.weight(3.0f)
						.build()
					).build(),
				true
			)
			.await()
			.indefinitely();

		var enrichPipelineUpdated = getEnrichPipeline();

		var firstItemUpdated = enrichPipelineUpdated.getEnrichPipelineItems().iterator().next();

		assertEquals(3L, firstItemUpdated.getEnrichItem().getId());
		assertEquals(1.0f, firstItemUpdated.getWeight());
	}

	private EnrichPipeline getEnrichPipeline() {
		return sessionFactory.withTransaction((s, t) ->
				enrichPipelineService
					.findByName(s, CreateConnection.PIPELINE_NAME)
					.call(pipeline -> Mutiny.fetch(
						pipeline.getEnrichPipelineItems()))
			)
			.await()
			.indefinitely();
	}

	private void patchPipelineWithThreeItems() {
		var enrichPipeline = getEnrichPipeline();

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(CreateConnection.PIPELINE_NAME)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(2L)
						.weight(1.0f)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(3L)
						.weight(2.0f)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(4L)
						.weight(3.0f)
						.build()
					).build(),
				true
			)
			.await()
			.indefinitely();

		var enrichPipelineUpdated = getEnrichPipeline();
		var items = enrichPipelineUpdated.getEnrichPipelineItems();
		var iterator = items.iterator();

		assertEquals(3, items.size());

		var first = iterator.next();
		assertEquals(2L, first.getKey().getEnrichItemId());
		assertEquals(1.0f, first.getWeight());

		var second = iterator.next();
		assertEquals(3L, second.getKey().getEnrichItemId());
		assertEquals(2.0f, second.getWeight());

		var third = iterator.next();
		assertEquals(4L, third.getKey().getEnrichItemId());
		assertEquals(3.0f, third.getWeight());

	}

	private void patchPipelineWithTwoItems() {

		var enrichPipeline = getEnrichPipeline();

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(CreateConnection.PIPELINE_NAME)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(2L)
						.weight(4.0f)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(4L)
						.weight(5.0f)
						.build()
					).build(),
				true
			)
			.await()
			.indefinitely();

		var enrichPipelineUpdated = getEnrichPipeline();

		var items = enrichPipelineUpdated.getEnrichPipelineItems();
		var iterator = items.iterator();

		assertEquals(2, items.size());

		var first = iterator.next();
		assertEquals(2L, first.getKey().getEnrichItemId());
		assertEquals(4.0f, first.getWeight());

		var second = iterator.next();
		assertEquals(4L, second.getKey().getEnrichItemId());
		assertEquals(5.0f, second.getWeight());

	}

	private void patchWithoutPipeline() {

		var enrichPipeline = getEnrichPipeline();

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(CreateConnection.PIPELINE_NAME)
					.build(),
				true
			)
			.await()
			.indefinitely();

	}

	private void updateWithoutPipeline() {

		var enrichPipeline = getEnrichPipeline();

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(CreateConnection.PIPELINE_NAME)
					.build(),
				false
			)
			.await()
			.indefinitely();

	}

	private void updateEnrichPipelineWithOneItem() {

		var enrichPipeline = getEnrichPipeline();

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(CreateConnection.PIPELINE_NAME)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(4L)
						.weight(5.0f)
						.build()
					).build(),
				false
			)
			.await()
			.indefinitely();

		var enrichPipelineUpdated = getEnrichPipeline();

		var items = enrichPipelineUpdated.getEnrichPipelineItems();
		var iterator = items.iterator();

		assertEquals(1, items.size());

		var first = iterator.next();
		assertEquals(4L, first.getKey().getEnrichItemId());
		assertEquals(5.0f, first.getWeight());
	}

}
