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

import io.openk9.datasource.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class VectorIndexWriterTest {

	static byte[] payload = TestUtils
		.getResourceAsJsonArray("vectoridxwriter/embeddedchunks.json")
		.toBuffer()
		.getBytes();
	static Sample johnDoe = new Sample("John", "Doe", "john.doe@acme.com", 20);

	@Test
	void getChunks() {
		var chunks = VectorIndexWriter.getChunks(payload);

		Assertions.assertEquals(3, chunks.size());

		var chunk = (Map<String, Object>) chunks.get(1);
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
	void getVectorSize() {
		var vectorSize = VectorIndexWriter.getVectorSize(payload);

		Assertions.assertEquals(6, vectorSize);
	}

	record Sample(
		String firstName,
		String lastName,
		String email,
		int age
	) {}

}