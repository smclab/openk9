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
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.service.util.Tuple2;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnrichPipelineServiceTest {

	public static final String ENRICH_PIPELINE_SERVICE_TEST_NAME = "enrich_pipeline_service_test";

	@Inject
	EnrichPipelineService enrichPipelineService;

	@Inject
	EnrichItemService enrichItemService;

	private List<EnrichItem> enrichItems;

	@Test
	@Order(1)
	void should_adds_enrich_items_and_create_pipeline() {

		var enrichItems = enrichItemService.findAll()
			.await().indefinitely();

		this.enrichItems = enrichItems;

		float weight = 1.0f;

		List<PipelineWithItemsDTO.ItemDTO> itemDTOS = new ArrayList<>();

		for (EnrichItem item : this.enrichItems) {

			PipelineWithItemsDTO.ItemDTO itemDTO =
				PipelineWithItemsDTO.ItemDTO.builder()
					.enrichItemId(item.getId())
					.weight(weight)
					.build();

			itemDTOS.add(itemDTO);
			weight += 1;

		}

		enrichPipelineService.createWithItems(PipelineWithItemsDTO.builder()
			.name(ENRICH_PIPELINE_SERVICE_TEST_NAME)
			.items(itemDTOS)
			.build()
		).await().indefinitely();

	}

	@Test
	@Order(2)
	void should_remove_item_from_pipeline() {

		var enrichPipeline = enrichPipelineService.findByName(
				"public",
				ENRICH_PIPELINE_SERVICE_TEST_NAME
			)
			.await().indefinitely();

		UniJoin.Builder<Tuple2<EnrichPipeline, EnrichItem>> uniJoin =
			Uni.join().builder();

		for (EnrichItem enrichItem : this.enrichItems) {

			var removeUni = enrichPipelineService.removeEnrichItem(
				enrichPipeline.getId(), enrichItem.getId());

			uniJoin.add(removeUni);

		}

		var removedItems = uniJoin.joinAll()
			.andFailFast()
			.await()
			.indefinitely();

		for (Tuple2<EnrichPipeline, EnrichItem> tuple2 : removedItems) {

			Assertions.assertNotNull(tuple2);

		}

		var enrichItems = enrichPipelineService.getEnrichItemsInEnrichPipeline(
			enrichPipeline.getId()).await().indefinitely();

		Assertions.assertEquals(0, enrichItems.size());

	}

	@Test
	@Order(3)
	void should_remove_enrich_pipeline() {

		var enrichPipeline = enrichPipelineService.findByName(
				"public",
				ENRICH_PIPELINE_SERVICE_TEST_NAME
			)
			.await().indefinitely();

		enrichPipelineService.deleteById(enrichPipeline.getId())
			.await().indefinitely();

		var removed = enrichPipelineService.findById(enrichPipeline.getId())
			.await().indefinitely();

		Assertions.assertNull(removed);

	}
}