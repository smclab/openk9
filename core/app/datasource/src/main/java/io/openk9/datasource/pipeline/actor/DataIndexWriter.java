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
import java.util.Map;
import jakarta.enterprise.inject.spi.CDI;

import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Writer;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.jboss.logging.Logger;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;

/**
 * Writes into the data index the one document a work stage hands over, or drops
 * it when the source no longer has it.
 * <p>
 * It is the writer of the enrich pipeline, whose documents carry no vector, and
 * the delete path of every scheduling type: an embedded document is written by
 * {@link ChunkStreamWriter}, one bulk per batch of chunks, but its deletion
 * still lands here, because a deleted document skips the processor chain
 * altogether.
 * <p>
 * Both paths start by dropping whatever was indexed for the content id. The
 * documents carry no {@code _id} of their own, so without that delete a second
 * run of the same content would add a copy instead of replacing it.
 * <p>
 * One instance serves the whole scheduling and can have the writes of several
 * documents in flight at once. It holds no per-document field: every step
 * carries its own held message, so two documents cannot smear their state onto
 * each other.
 */
public class DataIndexWriter extends AbstractBehavior<Writer.Command> {

	private final static org.jboss.logging.Logger log =
		Logger.getLogger(DataIndexWriter.class);

	private final OpenSearchAsyncClient asyncClient;
	private final String indexName;
	private final ActorRef<Writer.Response> replyTo;
	private final long datasourceId;

	public DataIndexWriter(
		ActorContext<Writer.Command> context,
		SchedulerDTO scheduler,
		ActorRef<Writer.Response> replyTo) {

		this(
			context,
			CDI.current().select(OpenSearchAsyncClient.class).get(),
			scheduler.getIndexName(),
			scheduler.getDatasourceId(),
			replyTo
		);
	}

	DataIndexWriter(
		ActorContext<Writer.Command> context,
		OpenSearchAsyncClient asyncClient,
		String indexName,
		long datasourceId,
		ActorRef<Writer.Response> replyTo) {

		super(context);
		this.asyncClient = asyncClient;
		this.indexName = indexName;
		this.datasourceId = datasourceId;
		this.replyTo = replyTo;
	}

	public static Behavior<Writer.Command> create(
		SchedulerDTO scheduler, ActorRef<Writer.Response> replyTo) {

		return Behaviors.setup(ctx ->
			new DataIndexWriter(ctx, scheduler, replyTo));
	}

	/**
	 * Creates the writer on an explicit OpenSearch client; the actor is not a
	 * CDI bean, so this is how tests pass a fake one.
	 */
	static Behavior<Writer.Command> create(
		OpenSearchAsyncClient asyncClient,
		String indexName,
		long datasourceId,
		ActorRef<Writer.Response> replyTo) {

		return Behaviors.setup(ctx -> new DataIndexWriter(
			ctx, asyncClient, indexName, datasourceId, replyTo));
	}

	@Override
	public Receive<Writer.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Writer.Start.class, this::onStart)
			.onMessage(DeleteCompleted.class, this::onDeleteCompleted)
			.onMessage(DeletedBeforeIndex.class, this::onDeletedBeforeIndex)
			.onMessage(IndexCompleted.class, this::onIndexCompleted)
			.build();
	}

	private Behavior<Writer.Command> onStart(Writer.Start start) {

		var dataPayload = start.dataPayload();
		var heldMessage = start.heldMessage();

		// A document with no payload is gone from the source: what was indexed
		// for it is dropped and nothing is written back.

		if (dataPayload == null) {

			try {
				getContext().pipeToSelf(
					DataIndexOps.deleteByContentId(asyncClient, indexName, heldMessage),
					(deleteResponse, throwable) ->
						new DeleteCompleted(heldMessage, throwable)
				);
			}
			catch (IOException e) {
				log.errorf("%s: I/O failed to search engine.", heldMessage);

				replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
			}

			return this;
		}

		// Else the previous version is dropped and the new one is written.

		try {

			var document = DataIndexOps.parseDocument(dataPayload);

			getContext().pipeToSelf(
				DataIndexOps.deleteByContentId(asyncClient, indexName, heldMessage),
				(deleteResponse, throwable) ->
					new DeletedBeforeIndex(heldMessage, throwable, document)
			);
		}
		catch (IllegalArgumentException e) {
			log.warnf("%s: Failed to parse the document from jsonPayload.", heldMessage);

			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}
		catch (IOException e) {
			log.errorf("%s: I/O failed to search engine.", heldMessage);

			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}

		return this;
	}

	private Behavior<Writer.Command> onDeleteCompleted(DeleteCompleted deleteCompleted) {

		var heldMessage = deleteCompleted.heldMessage();
		var throwable = deleteCompleted.throwable();

		if (throwable != null) {

			log.warnf("%s: Documents deletion failed.", heldMessage);

			DataIndexOps.sendDatasourceEventError(
				datasourceId, indexName, heldMessage, throwable.getMessage());

			replyTo.tell(
				new Writer.Failure(new WriterException(throwable), heldMessage));
		}
		else {

			log.infof("%s: Documents deleted.", heldMessage);

			DataIndexOps.sendDatasourceEventDelete(
				datasourceId, indexName, heldMessage);

			replyTo.tell(new Writer.Success(heldMessage));
		}

		return this;
	}

	private Behavior<Writer.Command> onDeletedBeforeIndex(
		DeletedBeforeIndex deletedBeforeIndex) {

		var heldMessage = deletedBeforeIndex.heldMessage();
		var deletionException = deletedBeforeIndex.throwable();

		if (deletionException != null) {
			log.warnf("%s: Deletion failed.", heldMessage);

			DataIndexOps.sendDatasourceEventError(
				datasourceId, indexName, heldMessage, deletionException.getMessage());

			replyTo.tell(new Writer.Failure(
				new WriterException(deletionException), heldMessage));

			return this;
		}

		try {
			getContext().pipeToSelf(
				asyncClient.index(DataIndexOps.buildIndexRequest(
					indexName, deletedBeforeIndex.document())),
				(indexResponse, throwable) ->
					new IndexCompleted(heldMessage, throwable)
			);
		}
		catch (IOException e) {
			log.errorf("%s: I/O failed to search engine.", heldMessage);

			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}

		return this;
	}

	private Behavior<Writer.Command> onIndexCompleted(IndexCompleted indexCompleted) {

		var heldMessage = indexCompleted.heldMessage();
		var throwable = indexCompleted.throwable();

		if (throwable != null) {

			if (log.isDebugEnabled()) {
				log.debugf(throwable, "%s: Error on index request", heldMessage);
			}

			DataIndexOps.sendDatasourceEventError(
				datasourceId, indexName, heldMessage, throwable.getMessage());

			replyTo.tell(new Writer.Failure(new WriterException(throwable), heldMessage));

			return this;
		}

		log.infof("%s: Document stored successfully", heldMessage);

		DataIndexOps.sendDatasourceEventCreate(datasourceId, indexName, heldMessage);

		replyTo.tell(new Writer.Success(heldMessage));

		return this;
	}

	/**
	 * The delete of a document that is only being dropped.
	 */
	private record DeleteCompleted(
		HeldMessage heldMessage,
		Throwable throwable
	) implements Writer.Command {}

	/**
	 * The delete of the previous version of a document that is about to be
	 * written again.
	 */
	private record DeletedBeforeIndex(
		HeldMessage heldMessage,
		Throwable throwable,
		Map<String, Object> document
	) implements Writer.Command {}

	private record IndexCompleted(
		HeldMessage heldMessage,
		Throwable throwable
	) implements Writer.Command {}

}
