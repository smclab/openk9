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

package io.openk9.datasource.pipeline.stages.working;

import io.openk9.datasource.pipeline.actor.WriterException;
import io.openk9.datasource.util.CborSerializable;

import org.apache.pekko.actor.typed.ActorRef;

public interface Writer {

	interface Command extends CborSerializable {}

	sealed interface Response extends CborSerializable {
		HeldMessage heldMessage();
	}

	record Start(byte[] dataPayload, HeldMessage heldMessage)
		implements Command {}

	/**
	 * A single batch of a streaming write. {@code firstBatch} marks the
	 * first batch of a content: the writer deletes the previously indexed chunks
	 * of the content before indexing this batch, so an early module failure
	 * leaves the prior version intact. The writer answers {@code replyTo} with a
	 * {@link BatchAck} once the batch is durably written, which is what gates the
	 * caller's next batch (ask-per-batch backpressure).
	 */
	record WriteBatch(
		byte[] dataPayload,
		boolean firstBatch,
		HeldMessage heldMessage,
		ActorRef<Response> replyTo
	) implements Command {}

	/**
	 * Signals that the batch stream of a content is exhausted; the writer replies
	 * {@link Success} once, closing the single per-document write.
	 * {@code wroteAnyBatch} is {@code false} when no batch was written (a
	 * zero-chunk stream): the writer completes the write but emits no creation
	 * event, since nothing was indexed and the prior version is untouched.
	 */
	record EndStream(HeldMessage heldMessage, boolean wroteAnyBatch)
		implements Command {}

	record Success(HeldMessage heldMessage) implements Response {}

	record Failure(WriterException exception, HeldMessage heldMessage) implements Response {}

	/** Acknowledges that a {@link WriteBatch} has been durably written. */
	record BatchAck(HeldMessage heldMessage) implements Response {}

}
