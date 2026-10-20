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

import io.openk9.datasource.model.EnrichItem;
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
			.stagingUrl("http://minio:9000/openk9-acme/42/content-1/file-1?X-Amz-Signature=secret")
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
			binary.containsKey("stagingUrl"),
			() -> "pre-signed URL leaked to the index: " + binary);
		Assertions.assertEquals("file-1", binary.getString("id"));
		Assertions.assertEquals("application/pdf", binary.getString("contentType"));
	}

	@Test
	void withoutStripThePreSignedUrlWouldLeak() {
		// guards the regression: the URL is present until it is stripped, so a
		// missing strip would persist the bearer credential in OpenSearch
		var binary = binaryOf(payloadWithSignedBinary());

		Assertions.assertTrue(binary.containsKey("stagingUrl"));
	}

	private static DataPayload payload(String rawContent, Map<String, Object> rest) {
		return DataPayload.builder()
			.rawContent(rawContent)
			.rest(new HashMap<>(rest))
			.build();
	}

	@Test
	void mergeReplacesRawContentWithAnEmptyOne() {
		var prev = payload("connector text", Map.of());
		var next = payload("", Map.of());

		var merged = EnrichPipeline.mergeResponse(
			"$", EnrichItem.BehaviorMergeType.REPLACE, prev, next);

		Assertions.assertEquals("", merged.getRawContent());
	}

	@Test
	void mergeReplacesRawContentAtTheTopLevelWithANestedJsonPath() {
		var prev = payload("connector text", Map.of("title", "report"));
		var next = payload("extracted text", Map.of("content", "extracted text"));

		var merged = EnrichPipeline.mergeResponse(
			"$.tika", EnrichItem.BehaviorMergeType.REPLACE, prev, next);

		// the jsonPath moves only the rest under tika, not the rawContent
		Assertions.assertEquals("extracted text", merged.getRawContent());
		Assertions.assertEquals("report", merged.getRest().get("title"));
		Assertions.assertFalse(merged.getRest().containsKey("content"));
		Assertions.assertEquals(
			"extracted text",
			new JsonObject(merged.getRest()).getJsonObject("tika").getString("content"));
	}

	@Test
	void mergeReplacesRawContentAlsoWithTheMergeBehavior() {
		var prev = payload("connector text", Map.of());
		var next = payload("extracted text", Map.of());

		var merged = EnrichPipeline.mergeResponse(
			"$", EnrichItem.BehaviorMergeType.MERGE, prev, next);

		Assertions.assertEquals("extracted text", merged.getRawContent());
	}

	private static BinaryPayload binary(String id, Map<String, Object> metadata) {
		return BinaryPayload.builder()
			.id(id)
			.name(id + ".jpg")
			.contentType("image/jpeg")
			.sourceUrl("https://source/" + id)
			.stagingUrl("http://minio/" + id + "?X-Amz-Signature=secret")
			.metadata(metadata)
			.build();
	}

	private static DataPayload payloadWith(BinaryPayload... binaries) {
		return DataPayload.builder()
			.contentId("content-1")
			.resources(ResourcesPayload.of(List.of(binaries)))
			.rest(new HashMap<>())
			.build();
	}

	@Test
	void mergeAddsTheReturnedMetadataKeysAndKeepsTheOthers() {
		// two images, the first with a page
		var prev = payloadWith(
			binary("img-1", Map.of("page", 12)),
			binary("img-2", null));

		// the enricher returns only a caption for the first one
		var next = payloadWith(BinaryPayload.builder()
			.id("img-1")
			.metadata(Map.of("caption", "a bridge"))
			.build());

		var merged = EnrichPipeline.mergeResponse(
			"$.tika", EnrichItem.BehaviorMergeType.REPLACE, prev, next);

		// the first keeps its page and gets the caption
		var binaries = merged.getResources().getBinaries();
		Assertions.assertEquals(
			Map.of("page", 12, "caption", "a bridge"),
			binaries.get(0).getMetadata());
		Assertions.assertEquals("img-1.jpg", binaries.get(0).getName());

		// the second is as before
		Assertions.assertEquals(binary("img-2", null), binaries.get(1));

		// the previous payload is left untouched
		Assertions.assertEquals(
			Map.of("page", 12),
			prev.getResources().getBinaries().get(0).getMetadata());
	}

	@Test
	void mergeUpdatesTheContentTypeButNotTheIdentity() {
		// an image of unknown type
		var original = binary("img-1", null);
		original.setContentType("application/octet-stream");
		var prev = payloadWith(original);

		// the enricher corrects the type and tries to change the URLs
		var next = payloadWith(BinaryPayload.builder()
			.id("img-1")
			.contentType("image/png")
			.sourceUrl("https://elsewhere/img-1")
			.stagingUrl("http://elsewhere/img-1")
			.build());

		var merged = EnrichPipeline.mergeResponse(
			"$", EnrichItem.BehaviorMergeType.MERGE, prev, next);

		// the type is the enricher's, the URLs are the connector's
		var binary = merged.getResources().getBinaries().get(0);
		Assertions.assertEquals("image/png", binary.getContentType());
		Assertions.assertEquals("https://source/img-1", binary.getSourceUrl());
		Assertions.assertEquals(
			"http://minio/img-1?X-Amz-Signature=secret", binary.getStagingUrl());
		Assertions.assertEquals("img-1.jpg", binary.getName());
	}

	@Test
	void mergeDiscardsABinaryWithAnUnknownId() {
		var prev = payloadWith(binary("img-1", null));

		// the enricher returns a binary the connector never sent
		var next = payloadWith(
			BinaryPayload.builder().id("img-1").build(),
			BinaryPayload.builder().id("intruder").name("x.png").build());

		var merged = EnrichPipeline.mergeResponse(
			"$", EnrichItem.BehaviorMergeType.REPLACE, prev, next);

		// only the known binary stays, unchanged
		Assertions.assertEquals(
			List.of(binary("img-1", null)),
			merged.getResources().getBinaries());
	}

}
