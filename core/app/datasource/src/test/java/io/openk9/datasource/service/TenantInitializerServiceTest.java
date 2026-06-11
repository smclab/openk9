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

import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.init.QueryParserConfigs;
import io.openk9.datasource.model.init.SearchConfig;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class TenantInitializerServiceTest {

	private static final String TENANT_ID = "public";

	@Inject
	TenantInitializerService tenantInitializerService;

	@Inject
	SearchConfigService searchConfigService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	void should_be_idempotent_on_repeated_createDefault() {
		// the test boot already runs createDefault(TENANT_ID) via Initializer,
		// so this call is at least the second invocation on the same schema

		// 1. Re-invoke createDefault more than once
		// on the already-initialized tenant.
		assertDoesNotThrow(() -> tenantInitializerService
			.createDefault(TENANT_ID)
			.await().indefinitely());
		assertDoesNotThrow(() -> tenantInitializerService
			.createDefault(TENANT_ID)
			.await().indefinitely());

		// 2. The default SearchConfig must keep exactly the expected
		// number of associated QueryParserConfigs, with no duplicates.
		var defaultSearchConfig = EntitiesUtils.getSearchConfig(
			SearchConfig.INSTANCE.getName(),
			searchConfigService,
			sessionFactory
		);

		assertEquals(
			QueryParserConfigs.DTOs.size(),
			defaultSearchConfig.getQueryParserConfigs().size()
		);

		assertEquals(
			QueryParserConfigs.DTOs.size(),
			EntitiesUtils.countQueryParserConfigsForSearchConfig(
				defaultSearchConfig.getId(),
				sessionFactory
			)
		);

		// 3. No orphan query_parser_config rows must exist:
		// the fix must not regress on the orphan-removal guarantee.
		assertEquals(
			0L,
			EntitiesUtils.countOrphanQueryParserConfigs(sessionFactory)
		);
	}

}
