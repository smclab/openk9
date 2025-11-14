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

import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.ResourceUri;
import io.openk9.datasource.model.dto.base.EnrichItemDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class EnrichItemServiceTest {

	private static final Logger LOGGER = Logger.getLogger(EnrichItemServiceTest.class);

	private static final String ENRICH_ITEM_NAME = "Enricher for testing";
	private static final String BASE_URI = "http://openk9.io";
	private static final String PATH = "/test";

	@Inject
	EnrichItemService enrichItemService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	void should_get_enrich_item_one() {
		createEnrichItemOne();
		var enrichItemOne = getEnrichItemOne();

		assertNotNull(enrichItemOne);
		LOGGER.info(enrichItemOne);

	}

	private EnrichItem createEnrichItemOne() {
		var dto = EnrichItemDTO
			.builder()
			.name(ENRICH_ITEM_NAME)
			.type(EnrichItem.EnrichItemType.HTTP_ASYNC)
			.resourceUri(ResourceUri.builder()
				.baseUri(BASE_URI)
				.path(PATH)
				.build())
			.build();

		return enrichItemService.create(dto)
			.await()
			.indefinitely();
	}

	private EnrichItem getEnrichItemOne() {
		return sessionFactory.withTransaction(
				s -> enrichItemService.findByName(s, ENRICH_ITEM_NAME)
			)
			.await()
			.indefinitely();
	}

}
