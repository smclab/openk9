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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.openk9.datasource.events.DatasourceEventBus;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.index.util.OpenSearchUtils;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch._types.Conflicts;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;

/**
 * Stateless operations on the vector index, shared by the two writers of the
 * chunks of a document: {@link VectorIndexWriter}, which writes a whole
 * document in one bulk, and {@link ChunkStreamWriter}, which writes one bulk
 * per batch of a streamed document. Every method takes the client and the index
 * it works on, so no per-document state can be smeared between the callers.
 */
final class VectorIndexOps {

	private VectorIndexOps() {}

	/**
	 * Reads the chunks out of a JSON payload, which is either a single chunk
	 * object or an array of them.
	 *
	 * @throws IllegalArgumentException when the payload is not valid JSON
	 */
	static List<Map<String, Object>> parseChunks(byte[] json)
		throws IllegalArgumentException {

		var documentContext = JsonPath
			.using(Configuration.defaultConfiguration())
			.parseUtf8(json);

		Object root = documentContext.read("$");

		if (root instanceof List) {
			return (List<Map<String, Object>>) root;
		}
		else {
			return List.of((Map<String, Object>) root);
		}
	}

	/**
	 * Drops every chunk previously indexed for the content of the held message.
	 */
	static CompletableFuture<DeleteByQueryResponse> deleteChunksByContentId(
		OpenSearchAsyncClient asyncClient, String indexName, HeldMessage heldMessage)
		throws IOException {

		return asyncClient.deleteByQuery(delete -> delete
			.index(indexName)
			.ignoreUnavailable(true)
			.conflicts(Conflicts.Proceed)
			.query(query -> query
				.match(match -> match
					.field("contentId.keyword")
					.query(fieldValue -> fieldValue
						.stringValue(heldMessage.contentId()))
				))
		);
	}

	/**
	 * Builds the bulk index request for a set of chunks, applying the default
	 * public ACL when a chunk carries none.
	 */
	static BulkRequest buildBulkRequest(
		String indexName, List<Map<String, Object>> chunks) {

		List<BulkOperation> bulkOperations = new ArrayList<>();

		for (Map<String, Object> chunk : chunks) {

			// Handle ACL mapping, fallback if not defined.
			var acl = (Map<String, Object>) chunk.get("acl");

			if (acl == null || acl.isEmpty()) {
				chunk.put("acl", Map.of("public", true));
			}

			bulkOperations.add(new BulkOperation.Builder()
				.index(new IndexOperation.Builder<>()
					.index(indexName)
					.document(chunk)
					.build())
				.build());
		}

		return new BulkRequest.Builder()
			.index(indexName)
			.operations(bulkOperations)
			.build();
	}

	/**
	 * Aggregates the reasons of every failed item of a bulk response in a single
	 * human readable text.
	 */
	static String aggregateErrors(BulkResponse bulkResponse) {

		return bulkResponse.items()
			.stream()
			.map(BulkResponseItem::error)
			.filter(Objects::nonNull)
			.map(OpenSearchUtils::getPrimaryAndFirstCauseReason)
			.collect(Collectors.joining("\n------------------------------------\n"));
	}

	static void sendDatasourceEventCreate(
		long datasourceId, String indexName, HeldMessage heldMessage) {

		DatasourceEventBus.sendMessage(DatasourceMessage.New.builder()
			.datasourceId(datasourceId)
			.contentId(heldMessage.contentId())
			.tenantId(heldMessage.processKey().tenantId())
			.indexName(indexName)
			.build()
		);

	}

	static void sendDatasourceEventDelete(
		long datasourceId, String indexName, HeldMessage heldMessage) {

		DatasourceEventBus.sendMessage(DatasourceMessage.Delete
			.builder()
			.datasourceId(datasourceId)
			.contentId(heldMessage.contentId())
			.tenantId(heldMessage.processKey().tenantId())
			.indexName(indexName)
			.build()
		);
	}

	static void sendDatasourceEventError(
		long datasourceId, String indexName, HeldMessage heldMessage, String reasons) {

		DatasourceEventBus.sendMessage(DatasourceMessage.Failure
			.builder()
			.datasourceId(datasourceId)
			.contentId(heldMessage.contentId())
			.tenantId(heldMessage.processKey().tenantId())
			.indexName(indexName)
			.error(reasons)
			.build()
		);
	}

}
