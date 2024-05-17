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
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenSearchUtilsTest {

	private static final JsonObject expected =
		TestUtils.getResourceAsJsonObject("es/expected_mapping.json");

	private static final IngestionPayloadMapper mapper =
		Mappers.getMapper(IngestionPayloadMapper.class);

	@Test
	void should_create_dynamic_mapping_for_json() throws IOException {
		try (InputStream in = TestUtils.getResourceAsStream("plugindriver/sample.json")) {

			var ingestionPayload = Json.decodeValue(
				new String(in.readAllBytes()),
				IngestionPayload.class
			);

			var dynamicMapping = OpenSearchUtils.getDynamicMapping(ingestionPayload, mapper);

			assertEquals(expected, dynamicMapping);
		}

	}


}