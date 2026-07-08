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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import jakarta.enterprise.inject.spi.CDI;

import io.openk9.datasource.events.DatasourceEventBus;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.index.util.OpenSearchUtils;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Writer;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.jboss.logging.Logger;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.bulk.UpdateOperation;
import org.opensearch.client.opensearch.core.search.Hit;

public class PartialUpdateWriter extends AbstractBehavior<Writer.Command> {

	private static final Logger log = Logger.getLogger(PartialUpdateWriter.class);
	// The default search size (10) is too small: we raise it to the
	// max_result_window (10_000, the most a single search can return) so a
	// single search covers every document of a contentId without paginating.
	private static final int MAX_UPDATABLE_DOCUMENTS = 10_000;

	private final String indexName;
	private final ActorRef<Writer.Response> replyTo;
	private final long datasourceId;
	private OpenSearchAsyncClient asyncClient;

	/**
	 * Creates a writer bound to the target index and datasource of the
	 * given scheduler. Use the {@link #create} factory to obtain the
	 * behavior; this constructor is invoked by {@link Behaviors#setup}.
	 *
	 * @param context the actor context
	 * @param scheduler the scheduler holding the target index and datasource id
	 * @param replyTo the actor notified with the write outcome
	 */
	public PartialUpdateWriter(
		ActorContext<Writer.Command> context,
		SchedulerDTO scheduler,
		ActorRef<Writer.Response> replyTo) {

		super(context);
		this.indexName = scheduler.getIndexName();
		this.datasourceId = scheduler.getDatasourceId();
		this.replyTo = replyTo;
	}

	/**
	 * Creates the behavior of a writer that partially updates all the
	 * documents indexed with the contentId of the received payload.
	 *
	 * @param scheduler the scheduler holding the target index name
	 * @param replyTo the actor notified with the write outcome
	 * @return the partial update writer behavior
	 */
	public static Behavior<Writer.Command> create(
		SchedulerDTO scheduler, ActorRef<Writer.Response> replyTo) {

		return Behaviors.setup(ctx ->
			new PartialUpdateWriter(ctx, scheduler, replyTo));
	}

	/**
	 * Handles the writer protocol: the {@link Writer.Start} command that
	 * begins the partial update, followed by the internal responses of the
	 * document-ids search and of the bulk update.
	 *
	 * @return the message handlers of this writer
	 */
	@Override
	public Receive<Writer.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Writer.Start.class, this::onStart)
			.onMessage(SearchDocumentIds.class, this::onSearchDocumentIds)
			.onMessage(BulkUpdateResponse.class, this::onBulkUpdateResponse)
			.build();
	}

	/**
	 * Builds one update operation for each document id, all merging
	 * the same partial document. No default acl is added: the acl
	 * field is a creation-only concern.
	 *
	 * @param indexName the index holding the documents
	 * @param documentIds the ids of the documents to update
	 * @param partialDocument the fields to merge into each document
	 * @return the bulk operations, one per document id
	 */
	protected static List<BulkOperation> buildBulkOperations(
		String indexName,
		List<String> documentIds,
		Map<String, Object> partialDocument) {

		List<BulkOperation> bulkOperations = new ArrayList<>();

		for (String documentId : documentIds) {

			var bulkOperation = new BulkOperation.Builder()
				.update(new UpdateOperation.Builder<Map<String, Object>>()
					.index(indexName)
					.id(documentId)
					.document(partialDocument)
					.build())
				.build();

			bulkOperations.add(bulkOperation);
		}

		return bulkOperations;
	}

	/**
	 * Parses the partial document to merge from the raw payload.
	 * Null-valued fields are dropped, so a partial update cannot
	 * clear fields of the indexed documents, and the type control
	 * field is dropped because it is pipeline metadata. Arrays are
	 * kept as-is: the update replaces them entirely.
	 *
	 * @param json the raw payload
	 * @return the fields to merge into the indexed documents
	 * @throws IllegalArgumentException if the payload is not a json object
	 */
	protected static Map<String, Object> preparePartialDocument(byte[] json)
		throws IllegalArgumentException {

		var documentContext = JsonPath
			.using(Configuration.defaultConfiguration())
			.parseUtf8(json);

		Object root = documentContext.read("$");

		if (!(root instanceof Map)) {
			throw new IllegalArgumentException(
				"the partial document must be a json object");
		}

		Map<String, Object> partialDocument =
			new HashMap<>((Map<String, Object>) root);

		partialDocument.remove("type");
		partialDocument.values().removeIf(Objects::isNull);

		return partialDocument;
	}

	private OpenSearchAsyncClient asyncClient() {
		if (asyncClient == null) {
			asyncClient = CDI.current().select(OpenSearchAsyncClient.class).get();
		}

		return asyncClient;
	}

	private CompletableFuture<SearchResponse<Void>> searchDocumentIds(
		HeldMessage heldMessage)
		throws IOException {

		return asyncClient().search(search -> search
			.index(indexName)
			.ignoreUnavailable(true)
			.size(MAX_UPDATABLE_DOCUMENTS)
			.source(source -> source.fetch(false))
			.query(query -> query
				.term(term -> term
					.field("contentId.keyword")
					.value(fieldValue -> fieldValue
						.stringValue(heldMessage.contentId()))
				)), Void.class
		);
	}

	private Behavior<Writer.Command> onBulkUpdateResponse(
		BulkUpdateResponse bulkUpdateResponse) {

		var bulkResponse = bulkUpdateResponse.bulkResponse();
		var heldMessage = bulkUpdateResponse.heldMessage();
		var throwable = bulkUpdateResponse.throwable();

		if (throwable != null) {

			if (log.isDebugEnabled()) {
				log.debugf(throwable, "%s: Error on bulk update request", heldMessage);
			}

			replyTo.tell(new Writer.Failure(new WriterException(throwable), heldMessage));

		}
		else if (bulkResponse != null) {

			if (bulkResponse.errors()) {

				// Aggregate all errors
				String errors = bulkResponse.items()
					.stream()
					.map(BulkResponseItem::error)
					.filter(Objects::nonNull)
					.map(OpenSearchUtils::getPrimaryAndFirstCauseReason)
					.collect(Collectors.joining("\n------------------------------------\n"));

				if (log.isDebugEnabled()) {
					log.debugf("%s: Bulk update request error: %s", heldMessage, errors);
				}

				sendDatasourceEventError(heldMessage, errors);

				replyTo.tell(new Writer.Failure(
					new WriterException(errors), heldMessage));
			}
			else {

				log.infof("%s: Documents updated successfully", heldMessage);

				sendDatasourceEventUpdate(heldMessage);

				replyTo.tell(new Writer.Success(heldMessage));
			}

		}
		else {
			log.errorf("%s: Response is null.", heldMessage);

			replyTo.tell(new Writer.Failure(
				new WriterException("No response"), heldMessage));
		}

		return this;
	}

	private Behavior<Writer.Command> onSearchDocumentIds(
		SearchDocumentIds searchDocumentIds) {

		var heldMessage = searchDocumentIds.heldMessage();
		var throwable = searchDocumentIds.throwable();

		if (throwable != null) {

			log.warnf("%s: Documents search failed.", heldMessage);

			sendDatasourceEventError(heldMessage, throwable.getMessage());

			replyTo.tell(new Writer.Failure(
				new WriterException(throwable), heldMessage));

			return this;
		}

		var documentIds = searchDocumentIds.searchResponse()
			.hits()
			.hits()
			.stream()
			.map(Hit::id)
			.toList();

		// If there is no document with this contentId, the update is cancelled.

		if (documentIds.isEmpty()) {

			log.infof(
				"%s: No documents found for this contentId, partial update cancelled.",
				heldMessage
			);

			replyTo.tell(new Writer.Success(heldMessage));

			return this;
		}

		var bulkOperations = buildBulkOperations(
			indexName, documentIds, searchDocumentIds.partialDocument());

		var bulkRequest = new BulkRequest.Builder()
			.index(indexName)
			.operations(bulkOperations)
			.build();

		try {

			getContext().pipeToSelf(
				asyncClient().bulk(bulkRequest),
				(bulkResponse, bulkThrowable) -> new BulkUpdateResponse(
					heldMessage, bulkResponse, bulkThrowable)
			);

		}
		catch (IOException e) {
			log.errorf("%s: I/O failed to search engine.", heldMessage);

			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}

		return this;
	}

	private Behavior<Writer.Command> onStart(Writer.Start start) {

		var dataPayload = start.dataPayload();
		var heldMessage = start.heldMessage();

		try {

			var partialDocument = preparePartialDocument(dataPayload);

			getContext().pipeToSelf(
				searchDocumentIds(heldMessage),
				(searchResponse, throwable) ->
					new SearchDocumentIds(
						heldMessage,
						partialDocument,
						searchResponse,
						throwable
					)
			);
		}
		catch (IllegalArgumentException e) {
			log.warnf("%s: Failed to parse the partial document.", heldMessage);

			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}
		catch (IOException e) {
			log.errorf("%s: I/O failed to search engine.", heldMessage);

			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}

		return this;
	}

	private void sendDatasourceEventError(HeldMessage heldMessage, String reasons) {

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

	private void sendDatasourceEventUpdate(HeldMessage heldMessage) {

		DatasourceEventBus.sendMessage(DatasourceMessage.Update
			.builder()
			.datasourceId(datasourceId)
			.contentId(heldMessage.contentId())
			.tenantId(heldMessage.processKey().tenantId())
			.indexName(indexName)
			.build()
		);
	}

	private record BulkUpdateResponse(
		HeldMessage heldMessage,
		BulkResponse bulkResponse,
		Throwable throwable
	) implements Writer.Command {}

	record SearchDocumentIds(
		HeldMessage heldMessage,
		Map<String, Object> partialDocument,
		SearchResponse<Void> searchResponse,
		Throwable throwable
	) implements Writer.Command {}

}
