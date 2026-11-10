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

package io.openk9.common.util.ingestion;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class IngestionUtilsTest {

	@Test
	void should_return_no_document_types_when_payload_is_missing() {
		assertArrayEquals(
			new String[0], IngestionUtils.getDocumentTypes(null));
	}

	@Test
	void should_return_payload_keys_as_document_types() {
		// a payload with two top-level keys
		var datasourcePayload = Map.<String, Object>of(
			"web", Map.of("title", "Lorem ipsum"),
			"file", Map.of());

		var documentTypes = IngestionUtils.getDocumentTypes(datasourcePayload);

		// key order of Map.of is unspecified
		Arrays.sort(documentTypes);
		assertArrayEquals(new String[] {"file", "web"}, documentTypes);
	}

}
