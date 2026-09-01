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

package io.openk9.datasource.pipeline.actor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openk9.datasource.TestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DataIndexOpsTest {

	static byte[] chunks = TestUtils
		.getResourceAsJsonArray("dataidxwriter/chunks.json")
		.toBuffer()
		.getBytes();

	static byte[] document = TestUtils
		.getResourceAsJsonObject("dataidxwriter/document.json")
		.toBuffer()
		.getBytes();

	static byte[] empty = new byte[]{};

	static byte[] emptyArray = "[]".getBytes();

	static Sample johnDoe = new Sample("John", "Doe", "john.doe@acme.com", 20);

	@Test
	void should_get_chunks_as_list() {
		var chunks = DataIndexOps.parseChunks(DataIndexOpsTest.chunks);

		Assertions.assertEquals(3, chunks.size());

		var chunk = chunks.getFirst();
		var metadata = (Map<String, Object>) chunk.get("sample");
		var sample = new Sample(
			(String) metadata.get("firstName"),
			(String) metadata.get("lastName"),
			(String) metadata.get("email"),
			(int) metadata.get("age")
		);

		Assertions.assertEquals(johnDoe, sample);
	}

	@Test
	void should_get_document_as_list() {
		var document = DataIndexOps.parseChunks(DataIndexOpsTest.document);

		Assertions.assertEquals(1, document.size());

		var chunk = (Map<String, Object>) document.getFirst();
		var metadata = (Map<String, Object>) chunk.get("sample");
		var sample = new Sample(
			(String) metadata.get("firstName"),
			(String) metadata.get("lastName"),
			(String) metadata.get("email"),
			(int) metadata.get("age")
		);

		Assertions.assertEquals(johnDoe, sample);
	}

	@Test
	void should_get_document_as_map() {
		var document = DataIndexOps.parseDocument(DataIndexOpsTest.document);

		var metadata = (Map<String, Object>) document.get("sample");
		var sample = new Sample(
			(String) metadata.get("firstName"),
			(String) metadata.get("lastName"),
			(String) metadata.get("email"),
			(int) metadata.get("age")
		);

		Assertions.assertEquals(johnDoe, sample);
	}

	@Test
	void should_reject_an_array_as_a_document() {
		// the enrich pipeline always hands over a single object: a payload of
		// another shape is a failure of that document, not a cast error that
		// takes the writer down.
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> DataIndexOps.parseDocument(DataIndexOpsTest.chunks)
		);
	}

	@Test
	void should_make_a_document_without_acl_public() {
		Map<String, Object> document = new HashMap<>(Map.of("contentId", "content-1"));

		DataIndexOps.buildIndexRequest("an-index", document);

		Assertions.assertEquals(Map.of("public", true), document.get("acl"));
	}

	@Test
	void should_keep_the_acl_a_document_carries() {
		var acl = Map.of("allow", List.of("group-1"));
		Map<String, Object> document = new HashMap<>(Map.of(
			"contentId", "content-1", "acl", acl));

		DataIndexOps.buildIndexRequest("an-index", document);

		Assertions.assertEquals(acl, document.get("acl"));
	}

	@Test
	void should_get_emptyArray_as_empty_list() {
		var emptyArray = DataIndexOps.parseChunks(DataIndexOpsTest.emptyArray);

		Assertions.assertTrue(emptyArray.isEmpty());
	}

	@Test
	void should_throws_on_empty_string() {

		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> DataIndexOps.parseChunks(DataIndexOpsTest.empty)
		);

	}


	record Sample(
		String firstName,
		String lastName,
		String email,
		int age
	) {}

}