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

import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.dto.base.TabDTO;
import io.openk9.datasource.model.dto.base.TokenTabDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class UnboundTabTest {

	@Inject
	TokenTabService tokenTabService;
	@Inject
	TabService tabService;

	@Test
	void should_get_unbound_tab_by_tokenTab() {

		var boundTab = tabService.create(TabDTO.builder()
				.name("UnboundTabTest_Bound_tab")
				.priority(1)
				.build()
		).await().indefinitely();

		var unboundTab = tabService.create(TabDTO.builder()
				.name("UnboundTabTest_Unbound_tab")
				.priority(2)
				.build()
		).await().indefinitely();

		var tokenTab = tokenTabService.create(TokenTabDTO.builder()
				.name("UnboundTabTest_tokenTab")
				.tokenType(TokenTab.TokenType.TEXT)
				.value("test")
				.filter(false)
				.build()
		).await().indefinitely();

		tabService.addTokenTabToTab(boundTab.getId(), tokenTab.getId())
				.await().indefinitely();

		var tabs = tabService.findUnboundTabsByTokenTab(tokenTab.getId())
				.await().indefinitely();

		Assertions.assertTrue(tabs.contains(unboundTab));

		Assertions.assertFalse(tabs.contains(boundTab));

		tabService.removeTokenTabToTab(boundTab.getId(), tokenTab.getId())
				.await().indefinitely();

		tokenTabService.deleteById(tokenTab.getId())
				.await().indefinitely();

		tabService.deleteById(unboundTab.getId())
				.await().indefinitely();

		tabService.deleteById(boundTab.getId())
				.await().indefinitely();

	}

}
