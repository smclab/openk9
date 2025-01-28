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

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;

import io.openk9.datasource.Initializer;
import io.openk9.datasource.graphql.dto.PipelineWithItemsDTO;
import io.openk9.datasource.model.EnrichPipeline;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class EnrichPipelineSortTest {

	@Inject
	EnrichPipelineService enrichPipelineService;

	@Inject
	EnrichItemService enrichItemService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@AfterEach
	void resetState() {

		resetPipelineWithThreeItems();

	}

	@Test
	void should_sort_with_different_order() {

		changeItemsOrderWithPatch();

	}

	@Test
	void should_removes_an_item_from_the_pipeline() {

		patchPipelineWithTwoItems();

	}

	@Test
	void should_update_enrich_items_in_pipeline() {

		updateEnrichPipelineWithOneItem();

	}

	@Test
	void should_not_change_items_with_patch() {

		patchWithoutPipeline();

		var enrichPipeline = getEnrichPipeline();

		assertEquals(3, enrichPipeline.getEnrichPipelineItems().size());

	}

	@Test
	void should_remove_items_with_update() {

		updateWithoutPipeline();

		var enrichPipeline = getEnrichPipeline();

		assertEquals(0, enrichPipeline.getEnrichPipelineItems().size());

	}

	private void changeItemsOrderWithPatch() {

		var enrichPipeline = getEnrichPipeline();

		var enrichPipelineItems =
			enrichPipeline.getEnrichPipelineItems().iterator();
		var one = enrichPipelineItems.next().getEnrichItem();
		var two = enrichPipelineItems.next().getEnrichItem();
		var three = enrichPipelineItems.next().getEnrichItem();

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(Initializer.INIT_DATASOURCE_PIPELINE)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(one.getId())
						.weight(2.0f)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(two.getId())
						.weight(1.0f)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(three.getId())
						.weight(3.0f)
						.build()
					).build(),
				true
			)
			.await()
			.indefinitely();

		var enrichPipelineUpdated = getEnrichPipeline();

		var firstItemUpdated = enrichPipelineUpdated.getEnrichPipelineItems().iterator().next();

		assertEquals(two.getId(), firstItemUpdated.getEnrichItem().getId());
		assertEquals(1.0f, firstItemUpdated.getWeight());
	}

	private EnrichPipeline getEnrichPipeline() {
		return sessionFactory.withTransaction((s, t) ->
				enrichPipelineService
					.findByName(s, Initializer.INIT_DATASOURCE_PIPELINE)
					.call(pipeline -> Mutiny.fetch(
						pipeline.getEnrichPipelineItems()))
			)
			.await()
			.indefinitely();
	}

	private void patchPipelineWithTwoItems() {

		var enrichPipeline = getEnrichPipeline();

		var enrichPipelineItems = enrichPipeline.getEnrichPipelineItems().iterator();
		var one = enrichPipelineItems.next().getEnrichItem();
		var two = enrichPipelineItems.next().getEnrichItem();
		var three = enrichPipelineItems.next().getEnrichItem();

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(Initializer.INIT_DATASOURCE_PIPELINE)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(one.getId())
						.weight(4.0f)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(three.getId())
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
		assertEquals(one.getId(), first.getKey().getEnrichItemId());
		assertEquals(4.0f, first.getWeight());

		var second = iterator.next();
		assertEquals(three.getId(), second.getKey().getEnrichItemId());
		assertEquals(5.0f, second.getWeight());

	}

	private void patchWithoutPipeline() {

		var enrichPipeline = getEnrichPipeline();

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(Initializer.INIT_DATASOURCE_PIPELINE)
					.build(),
				true
			)
			.await()
			.indefinitely();

	}

	private void resetPipelineWithThreeItems() {
		var enrichPipeline = getEnrichPipeline();

		var enrichItems = enrichItemService.findAll().await().indefinitely();

		var enrichPipelineItems = enrichItems.iterator();
		var one = enrichPipelineItems.next();
		var two = enrichPipelineItems.next();
		var three = enrichPipelineItems.next();

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(Initializer.INIT_DATASOURCE_PIPELINE)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(one.getId())
						.weight(1.0f)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(two.getId())
						.weight(2.0f)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(three.getId())
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
		assertEquals(one.getId(), first.getKey().getEnrichItemId());
		assertEquals(1.0f, first.getWeight());

		var second = iterator.next();
		assertEquals(two.getId(), second.getKey().getEnrichItemId());
		assertEquals(2.0f, second.getWeight());

		var third = iterator.next();
		assertEquals(three.getId(), third.getKey().getEnrichItemId());
		assertEquals(3.0f, third.getWeight());

	}

	private void updateEnrichPipelineWithOneItem() {

		var enrichPipeline = getEnrichPipeline();

		var enrichPipelineItems = getEnrichPipeline().getEnrichPipelineItems().iterator();
		var one = enrichPipelineItems.next().getEnrichItem();
		var two = enrichPipelineItems.next().getEnrichItem();
		var three = enrichPipelineItems.next().getEnrichItem();

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(Initializer.INIT_DATASOURCE_PIPELINE)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(three.getId())
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
		assertEquals(three.getId(), first.getKey().getEnrichItemId());
		assertEquals(5.0f, first.getWeight());
	}

	private void updateWithoutPipeline() {

		var enrichPipeline = getEnrichPipeline();

		enrichPipelineService.patchOrUpdateWithItems(
				enrichPipeline.getId(),
				PipelineWithItemsDTO.builder()
					.name(Initializer.INIT_DATASOURCE_PIPELINE)
					.build(),
				false
			)
			.await()
			.indefinitely();

	}

}
