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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.openk9.datasource.TestUtils;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.ProviderModel;
import io.openk9.ml.grpc.EmbeddingOuterClass;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class EmbeddingServiceUtilsTest {

	@Test
	void should_get_everything_from_the_payload() {

		var datapayload = TestUtils
			.getResourceAsJsonObject("embedding/datapayload.json")
			.toBuffer()
			.getBytes();

		var documentContext = JsonPath
			.using(Configuration.defaultConfiguration())
			.parseUtf8(datapayload);

		var metadataMap = EmbeddingService.getRoot(
			documentContext
		);

		assertTrue(metadataMap.containsKey("sample"));
		assertTrue(metadataMap.containsKey("store"));

		Map<String, Object> sample = (Map<String, Object>) metadataMap.get("sample");
		Map<String, Object> store = (Map<String, Object>) metadataMap.get("store");

		assertEquals(20, sample.get("age"));

		assertTrue(store.containsKey("bicycle"));
		assertTrue(store.containsKey("book"));

	}

	@Test
	void should_get_windows() {
		var size = 3;
		var total = 5;
		var integers = List.of(1, 2, 3, 4, 5);

		var result = integers.stream().map(i -> {
			var previous = EmbeddingService.getPreviousWindow(size, i, integers);

			var next = EmbeddingService.getNextWindow(size, i, total, integers);

			return new ItemWithNeighbors(i, previous, next);
		}).toList();

		assertEquals(0, result.get(0).previous().size());
		assertEquals(3, result.get(0).next().size());

		assertEquals(1, result.get(1).previous().size());
		assertEquals(3, result.get(1).next().size());

		assertEquals(2, result.get(2).previous().size());
		assertEquals(2, result.get(2).next().size());

		assertEquals(3, result.get(3).previous().size());
		assertEquals(1, result.get(3).next().size());

		assertEquals(3, result.get(4).previous().size());
		assertEquals(0, result.get(4).next().size());
	}

	record ItemWithNeighbors(int i, List<Integer> previous, List<Integer> next) {}

	@Test
	void mapToEmbeddingModelRequest_should_propagate_apiUrl_when_present() {

		var embeddingModel = new EmbeddingModel();
		embeddingModel.setApiKey("test-key");
		embeddingModel.setApiUrl("https://api.openai.com/v1/embeddings");
		var providerModel = new ProviderModel();
		providerModel.setProvider("OPEN_AI");
		providerModel.setModel("text-embedding-ada-002");
		embeddingModel.setProviderModel(providerModel);

		var request = EmbeddingService.mapToEmbeddingModelRequest(embeddingModel);

		assertTrue(request.hasApiUrl());
		assertEquals("https://api.openai.com/v1/embeddings", request.getApiUrl());
		assertEquals("test-key", request.getApiKey());
		assertEquals("OPEN_AI", request.getProviderModel().getProvider());
		assertEquals("text-embedding-ada-002", request.getProviderModel().getModel());
	}

	@Test
	void mapToEmbeddingModelRequest_should_omit_apiUrl_when_null() {

		var embeddingModel = new EmbeddingModel();
		embeddingModel.setApiKey("test-key");
		embeddingModel.setApiUrl(null);
		var providerModel = new ProviderModel();
		providerModel.setProvider("HUGGING_FACE");
		providerModel.setModel("sentence-transformers/all-MiniLM-L6-v2");
		embeddingModel.setProviderModel(providerModel);

		var request = EmbeddingService.mapToEmbeddingModelRequest(embeddingModel);

		assertFalse(
			request.hasApiUrl(),
			"apiUrl must be unset on the gRPC payload when the entity does not provide one"
		);
		assertEquals("test-key", request.getApiKey());
		assertEquals("HUGGING_FACE", request.getProviderModel().getProvider());
	}

	@Test
	void mapToEmbeddingModelRequest_should_omit_apiUrl_when_blank() {

		var embeddingModel = new EmbeddingModel();
		embeddingModel.setApiKey("test-key");
		embeddingModel.setApiUrl("");
		var providerModel = new ProviderModel();
		providerModel.setProvider("HUGGING_FACE");
		providerModel.setModel("sentence-transformers/all-MiniLM-L6-v2");
		embeddingModel.setProviderModel(providerModel);

		var request = EmbeddingService.mapToEmbeddingModelRequest(embeddingModel);

		assertFalse(
			request.hasApiUrl(),
			"apiUrl must be unset on the gRPC payload when the entity provides a blank value"
		);
		assertEquals("test-key", request.getApiKey());
		assertEquals("HUGGING_FACE", request.getProviderModel().getProvider());
	}

	@Test
	void toGrpcVectorDataType_should_map_every_type_and_fallback_to_float32() {

		// a fresh model and a null-coerced one both default to FLOAT32
		var embeddingModel = new EmbeddingModel();
		assertEquals(
			EmbeddingModel.VectorDataType.FLOAT32,
			embeddingModel.getVectorDataType());

		embeddingModel.setVectorDataType(null);
		assertEquals(
			EmbeddingModel.VectorDataType.FLOAT32,
			embeddingModel.getVectorDataType());

		// FLOAT32 and null (pre-existing models) map to the grpc default
		assertEquals(
			EmbeddingOuterClass.VectorDataType.VECTOR_DATA_TYPE_FLOAT32,
			EmbeddingService.toGrpcVectorDataType(
				EmbeddingModel.VectorDataType.FLOAT32));

		assertEquals(
			EmbeddingOuterClass.VectorDataType.VECTOR_DATA_TYPE_FLOAT32,
			EmbeddingService.toGrpcVectorDataType(null));

		// BYTE and BINARY map to their quantized grpc counterparts
		assertEquals(
			EmbeddingOuterClass.VectorDataType.VECTOR_DATA_TYPE_BYTE,
			EmbeddingService.toGrpcVectorDataType(
				EmbeddingModel.VectorDataType.BYTE));

		assertEquals(
			EmbeddingOuterClass.VectorDataType.VECTOR_DATA_TYPE_BINARY,
			EmbeddingService.toGrpcVectorDataType(
				EmbeddingModel.VectorDataType.BINARY));
	}

	@Test
	void mapToEmbeddingModelRequest_should_set_multimodal_when_true() {

		// a multimodal model carrying a pre-existing jsonConfig
		var embeddingModel = new EmbeddingModel();
		embeddingModel.setJsonConfig("{\"foo\":\"bar\"}");
		embeddingModel.setMultimodal(true);

		var request = EmbeddingService.mapToEmbeddingModelRequest(embeddingModel);

		// the flag travels in its own field, the jsonConfig is left untouched
		assertTrue(request.getMultimodal());
		var fields = request.getJsonConfig().getFieldsMap();
		assertFalse(fields.containsKey("multimodal"));
		assertEquals("bar", fields.get("foo").getStringValue());
	}

	@Test
	void mapToEmbeddingModelRequest_should_not_set_multimodal_when_false() {

		// a text-only model carrying a pre-existing jsonConfig
		var embeddingModel = new EmbeddingModel();
		embeddingModel.setJsonConfig("{\"foo\":\"bar\"}");

		var request = EmbeddingService.mapToEmbeddingModelRequest(embeddingModel);

		// the flag stays at its proto3 default and the jsonConfig is unchanged
		assertFalse(request.getMultimodal());
		var fields = request.getJsonConfig().getFieldsMap();
		assertFalse(fields.containsKey("multimodal"));
		assertEquals("bar", fields.get("foo").getStringValue());
	}

	@Test
	void mapToEmbeddingModelRequest_should_leave_jsonConfig_unset_when_null() {

		// a text-only model without any jsonConfig (pre-existing behaviour)
		var embeddingModel = new EmbeddingModel();

		var request = EmbeddingService.mapToEmbeddingModelRequest(embeddingModel);

		// the jsonConfig stays unset, as on the text-only wire
		assertFalse(request.hasJsonConfig());
		assertFalse(request.getMultimodal());
	}

	@Test
	void composeRequest_should_compose_a_text_only_request() {

		// a payload with text in the configured field and no binaries
		var payload = """
			{
				"contentId": "content-1",
				"datasourceId": 1,
				"rawContent": "some text to embed",
				"title": "a title"
			}""".getBytes();

		var composed = EmbeddingService.composeRequest(
			"tenant", textOnlyConfig(), payload);

		// the request carries the extracted text, no refs, the model's type
		var request = composed.request();
		assertEquals("some text to embed", request.getText());
		assertEquals(0, request.getRefsCount());
		assertEquals(
			EmbeddingOuterClass.VectorDataType.VECTOR_DATA_TYPE_BYTE,
			request.getVectorDataType());
		assertEquals("tenant", request.getTenantId());

		// the module re-splits the text in chunks, so the source field must
		// leave the merged root; the other fields must stay
		assertFalse(composed.root().containsKey("rawContent"));
		assertTrue(composed.root().containsKey("title"));

		// no binaries sent, nothing to diff at end of stream
		assertTrue(composed.sentFileIds().isEmpty());
		assertEquals("content-1", composed.contentId());
	}

	@Test
	void composeRequest_should_fail_without_text_nor_refs() {

		// a payload without the text field and without binaries
		var payload = """
			{
				"contentId": "content-1",
				"datasourceId": 1,
				"title": "a title"
			}""".getBytes();

		var config = textOnlyConfig();

		// the guard fails the document before any gRPC call
		assertThrows(
			PayloadEmbeddingFailed.class,
			() -> EmbeddingService.composeRequest("tenant", config, payload));
	}

	@Test
	void composeRequest_should_send_one_ref_per_binary() {

		// a payload with text and two staged binaries, one of which reached the
		// datasource without a contentType
		var payload = """
			{
				"contentId": "content-1",
				"datasourceId": 7,
				"rawContent": "some text to embed",
				"resources": {
					"binaries": [
						{
							"id": "file-a",
							"name": "pikachu.png",
							"contentType": "image/png"
						},
						{
							"id": "file-b",
							"name": "unknown.bin"
						}
					]
				}
			}""".getBytes();

		var resolver = new RecordingUrlResolver();

		var composed = EmbeddingService.composeRequest(
			"tenant", textOnlyConfig(), payload, resolver);

		var request = composed.request();

		// one ref per binary, in payload order, and the text alongside them
		assertEquals("some text to embed", request.getText());
		assertEquals(2, request.getRefsCount());

		var first = request.getRefs(0);
		assertEquals("file-a", first.getFileId());
		assertEquals("image/png", first.getContentType());

		var second = request.getRefs(1);
		assertEquals("file-b", second.getFileId());

		// no contentType on the payload means none on the wire (the proto3
		// default, an empty string): the module must not read an invented one.
		assertEquals("", second.getContentType());

		// the URL of every ref comes from the resolver, called once per binary
		// with the coordinates of that binary
		assertEquals(
			List.of(
				"tenant|7|content-1|file-a",
				"tenant|7|content-1|file-b"),
			resolver.calls);

		assertEquals("presigned:tenant|7|content-1|file-a", first.getUrl());
		assertEquals("presigned:tenant|7|content-1|file-b", second.getUrl());

		// bookkeeping: the contentType is what a chunk-doc's media_type is
		// resolved from (null falls back to application/octet-stream), the
		// sent ids are what the end-of-stream diff is computed against
		var contentTypes = composed.contentTypeByFileId();
		assertEquals(2, contentTypes.size());
		assertEquals("image/png", contentTypes.get("file-a"));
		assertNull(contentTypes.get("file-b"));

		assertEquals(Set.of("file-a", "file-b"), composed.sentFileIds());
	}

	@Test
	void composeRequest_should_compose_a_binary_only_request() {

		// an image document: no text field at all, one staged binary
		var payload = """
			{
				"contentId": "content-1",
				"datasourceId": 7,
				"title": "pikachu",
				"resources": {
					"binaries": [
						{
							"id": "file-a",
							"name": "pikachu.png",
							"contentType": "image/png"
						}
					]
				}
			}""".getBytes();

		var composed = EmbeddingService.composeRequest(
			"tenant", textOnlyConfig(), payload, new RecordingUrlResolver());

		var request = composed.request();

		// the GetMessages guard ("fail if there is no text") is relaxed here:
		// a document made only of binaries is legitimate and is sent with the
		// text field unset, not empty.
		assertFalse(request.hasText());
		assertEquals(1, request.getRefsCount());
		assertEquals("file-a", request.getRefs(0).getFileId());
	}

	@Test
	void composeRequest_should_fail_the_document_when_the_presign_fails() {

		// a document whose binary cannot be signed (object store unreachable)
		var payload = """
			{
				"contentId": "content-1",
				"datasourceId": 7,
				"resources": {
					"binaries": [{"id": "file-a", "contentType": "image/png"}]
				}
			}""".getBytes();

		var config = textOnlyConfig();

		EmbeddingService.BinaryUrlResolver failing =
			(tenantId, datasourceId, contentId, fileId) -> {
				throw new IllegalStateException("object store unreachable");
			};

		// the failure must reach the caller, not produce a ref without a URL
		assertThrows(
			IllegalStateException.class,
			() -> EmbeddingService.composeRequest(
				"tenant", config, payload, failing));
	}

	/**
	 * A presign that signs nothing: it records its arguments and returns a URL
	 * derived from them, so a test can assert both what was asked and what
	 * ended up on the wire.
	 */
	private static final class RecordingUrlResolver
		implements EmbeddingService.BinaryUrlResolver {

		final List<String> calls = new ArrayList<>();

		@Override
		public String presignGet(
			String tenantId, long datasourceId, String contentId, String fileId) {

			var call = String.join(
				"|", tenantId, String.valueOf(datasourceId), contentId, fileId);

			calls.add(call);

			return "presigned:" + call;
		}
	}

	private static EmbeddingService.EmbeddingChunksRequest textOnlyConfig() {

		// the docTypeField only contributes its path here
		var docTypeField = Mockito.mock(DocTypeField.class);
		Mockito.when(docTypeField.getPath()).thenReturn("rawContent");

		return new EmbeddingService.EmbeddingChunksRequest(
			docTypeField,
			2,
			EmbeddingOuterClass.EmbeddingModel.getDefaultInstance(),
			EmbeddingOuterClass.RequestChunk.getDefaultInstance(),
			EmbeddingOuterClass.VectorDataType.VECTOR_DATA_TYPE_BYTE);
	}

}
