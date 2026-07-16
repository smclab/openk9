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

import java.util.List;

import io.openk9.datasource.processor.payload.BinaryPayload;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.ResourcesPayload;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnrichPipelineTest {

	private static DataPayload payloadWithSignedBinary() {
		var binary = BinaryPayload.builder()
			.id("file-1")
			.contentType("application/pdf")
			.url("http://minio:9000/openk9-acme/42/content-1/file-1?X-Amz-Signature=secret")
			.build();

		return DataPayload.builder()
			.resources(ResourcesPayload.of(List.of(binary)))
			.build();
	}

	private static JsonObject binaryOf(DataPayload dataPayload) {
		return new JsonObject(new String(Json.encodeToBuffer(dataPayload).getBytes()))
			.getJsonObject("resources")
			.getJsonArray("binaries")
			.getJsonObject(0);
	}

	@Test
	void stripRemovesPreSignedUrlFromBinaries() {
		// a payload as it leaves an enricher, still carrying the injected URL
		var dataPayload = payloadWithSignedBinary();

		// strip the ephemeral credential before the payload reaches the index
		EnrichPipeline.stripBinaryUrls(dataPayload);

		// the indexed binary keeps its reference but not the URL
		var binary = binaryOf(dataPayload);
		Assertions.assertFalse(
			binary.containsKey("url"),
			() -> "pre-signed URL leaked to the index: " + binary);
		Assertions.assertEquals("file-1", binary.getString("id"));
		Assertions.assertEquals("application/pdf", binary.getString("contentType"));
	}

	@Test
	void withoutStripThePreSignedUrlWouldLeak() {
		// guards the regression: the URL is present until it is stripped, so a
		// missing strip would persist the bearer credential in OpenSearch
		var binary = binaryOf(payloadWithSignedBinary());

		Assertions.assertTrue(binary.containsKey("url"));
	}

}
