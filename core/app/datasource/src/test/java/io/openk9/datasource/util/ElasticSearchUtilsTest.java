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

package io.openk9.datasource.util;

import io.openk9.datasource.TestUtils;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElasticSearchUtilsTest {

	private static final JsonObject expected =
		TestUtils.getResourceAsJsonObject("es/expected_mapping.json");

	@Test
	void should_create_dynamic_mapping_for_json() throws IOException {
		try (InputStream in = TestUtils.getResourceAsStream("plugindriver/sample.json")) {

			var dynamicMapping = ElasticSearchUtils.getDynamicMapping(in.readAllBytes());

			assertEquals(expected, dynamicMapping);
		}

	}


}