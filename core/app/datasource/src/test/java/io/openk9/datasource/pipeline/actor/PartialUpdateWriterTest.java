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
import java.util.Map;

import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.datasource.TestUtils;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Writer;

import org.apache.pekko.actor.testkit.typed.javadsl.BehaviorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestInbox;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.core.SearchResponse;

class PartialUpdateWriterTest {

	static final String INDEX_NAME = "tenant-data-index";

	static final ShardingKey SHARDING_KEY =
		ShardingKey.fromStrings("tenant", "schedule-1");

	static byte[] partialDocument = TestUtils
		.getResourceAsJsonObject("partialupdatewriter/partial-document.json")
		.toBuffer()
		.getBytes();

	static byte[] jsonArray = "[]".getBytes();

	static byte[] partialWithAcl =
		"{\"contentId\":\"content-1\",\"acl\":{\"public\":false}}".getBytes();

	@Test
	void should_update_single_document_with_partial_fields() {
		// classic enrich pipeline: one indexed document for the contentId
		var partial = PartialUpdateWriter.preparePartialDocument(partialDocument);
		var bulkOperations = PartialUpdateWriter.buildBulkOperations(
			INDEX_NAME, List.of("doc-1"), partial);

		// a single update operation targets the document by _id
		Assertions.assertEquals(1, bulkOperations.size());

		var operation = bulkOperations.getFirst();
		Assertions.assertTrue(operation.isUpdate());
		Assertions.assertEquals("doc-1", operation.update().id());
		Assertions.assertEquals(INDEX_NAME, operation.update().index());
	}

	@Test
	void should_update_all_chunked_documents_in_bulk() {
		// chunked content: N indexed documents share the contentId
		var partial = PartialUpdateWriter.preparePartialDocument(partialDocument);
		var documentIds = List.of("doc-1", "doc-2", "doc-3");

		var bulkOperations = PartialUpdateWriter.buildBulkOperations(
			INDEX_NAME, documentIds, partial);

		// one update operation per _id, all on the same index
		Assertions.assertEquals(documentIds.size(), bulkOperations.size());

		for (int i = 0; i < documentIds.size(); i++) {
			var operation = bulkOperations.get(i);

			Assertions.assertTrue(operation.isUpdate());
			Assertions.assertEquals(documentIds.get(i), operation.update().id());
			Assertions.assertEquals(INDEX_NAME, operation.update().index());
		}
	}

	@Test
	void should_keep_arrays_whole_for_full_replacement() {
		var partial = PartialUpdateWriter.preparePartialDocument(partialDocument);

		// the array is kept as a single value, so the update
		// replaces the indexed array entirely instead of merging it.
		Assertions.assertEquals(List.of("alpha", "beta"), partial.get("tags"));
	}

	@Test
	void should_build_no_operations_when_no_documents_match() {
		// 0 hits on the contentId search: the update is cancelled
		var partial = PartialUpdateWriter.preparePartialDocument(partialDocument);

		var bulkOperations = PartialUpdateWriter.buildBulkOperations(
			INDEX_NAME, List.of(), partial);

		Assertions.assertTrue(bulkOperations.isEmpty());
	}

	@Test
	void should_not_add_default_acl_on_update() {
		// the default acl is injected only at creation:
		// when the payload omits acl, the update adds none.
		var partial = PartialUpdateWriter.preparePartialDocument(partialDocument);

		Assertions.assertFalse(partial.containsKey("acl"));
	}

	@Test
	void should_update_acl_when_present_in_partial_document() {
		// an acl carried by the partial document is applied, not stripped
		var partial = PartialUpdateWriter.preparePartialDocument(partialWithAcl);

		Assertions.assertEquals(Map.of("public", false), partial.get("acl"));
	}

	@Test
	void should_strip_null_fields_and_type_from_partial_document() {
		var partial = PartialUpdateWriter.preparePartialDocument(partialDocument);

		// null fields must not clear the indexed values
		Assertions.assertFalse(partial.containsKey("rawContent"));

		// the type control field is pipeline metadata, not document content
		Assertions.assertFalse(partial.containsKey("type"));

		// the other fields are kept
		Assertions.assertEquals("Updated title", partial.get("title"));
		Assertions.assertEquals("content-1", partial.get("contentId"));
	}

	@Test
	void should_throw_when_partial_document_is_not_an_object() {
		// a json array is not a valid partial document
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> PartialUpdateWriter.preparePartialDocument(jsonArray)
		);
	}

	@Test
	void should_reply_success_and_cancel_when_no_documents_match() {
		// setup the writer with a reply inbox
		var replyToInbox = TestInbox.<Writer.Response>create();
		var writer = BehaviorTestKit.create(
			PartialUpdateWriter.create(scheduler(), replyToInbox.getRef()));

		// a search that returns 0 hits for the contentId
		var heldMessage = new HeldMessage(SHARDING_KEY, 1L, 0L, "content-1");
		var emptyHits = new SearchResponse.Builder<Void>()
			.took(0)
			.timedOut(false)
			.shards(shards -> shards.total(1).successful(1).failed(0))
			.hits(hits -> hits.hits(List.of()))
			.build();

		writer.run(new PartialUpdateWriter.SearchDocumentIds(
			heldMessage, Map.of(), emptyHits, null));

		// the update is cancelled with a Success no-op reply
		var response = replyToInbox.receiveMessage();
		Assertions.assertInstanceOf(Writer.Success.class, response);
		Assertions.assertEquals(heldMessage, response.heldMessage());
	}

	static SchedulerDTO scheduler() {
		var scheduler = new SchedulerDTO();
		scheduler.setDatasourceId(1L);

		return scheduler;
	}

}
