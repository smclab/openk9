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

import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.model.EnrichPipelineItemKey;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;

@QuarkusTest
class EnrichPipelineServiceTest {

	@Inject
	EnrichPipelineService enrichPipelineService;


	@Test
	@RunOnVertxContext
	void should_adds_enrich_items_and_create_pipeline(UniAsserter asserter) {

		var session = mock(Mutiny.Session.class);

		asserter.assertThat(
			() -> enrichPipelineService.createWithItems(
				session, CreateConnection.PIPELINE_WITH_ITEMS_DTO),
			response -> then(session).should(atLeastOnce())
				.persist(argThat((EnrichPipeline pipeline) -> {
						Assertions.assertEquals(
							CreateConnection.PIPELINE_NAME,
							pipeline.getName()
						);

						var items = pipeline.getEnrichPipelineItems();

						Assertions.assertEquals(2, items.size());

						var orderedItemIds = items
							.stream()
							.map(EnrichPipelineItem::getKey)
							.map(EnrichPipelineItemKey::getEnrichItemId)
							.toList();

						Assertions.assertEquals(
							orderedItemIds,
							List.of(
								CreateConnection.FIRST_ITEM_ID,
								CreateConnection.SECOND_ITEM_ID
							)
						);

						return true;
					})
				)
		);

	}

}