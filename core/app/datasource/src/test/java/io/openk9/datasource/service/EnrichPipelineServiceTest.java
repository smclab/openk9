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

import io.openk9.datasource.mapper.EnrichPipelineMapper;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.dto.EnrichPipelineDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@QuarkusTest
class EnrichPipelineServiceTest {

	@InjectSpy
	EnrichPipelineService enrichPipelineService;


	@Test
	@RunOnVertxContext
	void should_adds_enrich_items_and_create_pipeline(UniAsserter asserter) {

		var session = mock(Mutiny.Session.class);

		asserter.assertThat(
			() -> enrichPipelineService.create(session, CreateConnection.PIPELINE_WITH_ITEMS_DTO),
			response -> {

				then(enrichPipelineService)
					.should(times(1))
					.create(eq(session), any(EnrichPipelineDTO.class));

				then(session)
					.should(times(1))
					.persist(
						argThat((EnrichPipeline pipeline) -> {
							Assertions.assertEquals(
								CreateConnection.PIPELINE_NAME,
								pipeline.getName()
							);

							//							var items = pipeline
							//								.getEnrichPipelineItems()
							//								.toArray(new EnrichPipelineItem[]{});

							var items = pipeline.getEnrichPipelineItems();

							Assertions.assertEquals(2, items.size());

							//							Assertions.assertEquals(
							//								CreateConnection.FIRST_ITEM_ID,
							//								items[0].getEnrichItem().getId()
							//							);
							//							Assertions.assertEquals(
							//								CreateConnection.SECOND_ITEM_ID,
							//								items[1].getEnrichItem().getId()
							//							);

							return true;
						})
					);

			}
		);

	}


	@Mock
	public static class MockEnrichPipelineService extends EnrichPipelineService {

		MockEnrichPipelineService() {
			super(null);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends K9Entity> Uni<T> persist(Mutiny.Session s, T entity) {
			var enrichPipeline = new EnrichPipeline();
			enrichPipeline.setName(CreateConnection.PIPELINE_NAME);

			return Uni.createFrom().item((T) enrichPipeline);
		}

		@Inject
		void setMapper(EnrichPipelineMapper mapper) {
			this.mapper = mapper;
		}

	}
}