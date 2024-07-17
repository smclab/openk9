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

package io.openk9.datasource.pipeline.service;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import io.openk9.datasource.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmbeddingServiceTest {

	@Test
	void getMetadataMap() {

		var datapayload = TestUtils
			.getResourceAsJsonObject("embedding/datapayload.json")
			.toBuffer()
			.getBytes();

		var documentContext = JsonPath
			.using(Configuration.defaultConfiguration())
			.parseUtf8(datapayload);

		var metadataMap = EmbeddingService.getMetadataMap(
			documentContext,
			"$.sample;$.store.bicycle"
		);

		assertTrue(metadataMap.containsKey("sample"));
		assertTrue(metadataMap.containsKey("store"));

		Map<String, Object> sample = (Map<String, Object>) metadataMap.get("sample");
		Map<String, Object> store = (Map<String, Object>) metadataMap.get("store");

		assertEquals(20, sample.get("age"));

		assertTrue(store.containsKey("bicycle"));
		assertFalse(store.containsKey("book"));

	}

}