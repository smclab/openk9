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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.bulk.OperationType;
import org.opensearch.client.transport.Endpoint;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.TransportOptions;

/**
 * Records the OpenSearch operations issued and returns canned responses; the
 * writers under test inspect only the completion and the bulk error flag, not
 * the response body.
 * <p>
 * Responses complete immediately unless the transport is {@link #gated()}, in
 * which case each one waits for a {@link #release()}: that is what lets a test
 * hold a write in flight and observe what the actor does with the messages
 * that arrive meanwhile.
 */
final class RecordingTransport implements OpenSearchTransport {

	final List<String> operations =
		Collections.synchronizedList(new ArrayList<>());
	private final JsonpMapper mapper = new JacksonJsonpMapper();
	private final BulkResponse bulkResponse;
	private final boolean gated;
	private final Map<String, Throwable> failures;
	private final List<Runnable> pending =
		Collections.synchronizedList(new ArrayList<>());

	RecordingTransport() {
		this(successfulBulk());
	}

	RecordingTransport(BulkResponse bulkResponse) {
		this(bulkResponse, false, Map.of());
	}

	private RecordingTransport(
		BulkResponse bulkResponse, boolean gated, Map<String, Throwable> failures) {

		this.bulkResponse = bulkResponse;
		this.gated = gated;
		this.failures = failures;
	}

	/**
	 * A transport whose every response stays pending until {@link #release()}.
	 */
	static RecordingTransport gated() {
		return new RecordingTransport(successfulBulk(), true, Map.of());
	}

	/**
	 * A transport whose responses to the given operation ("delete" or "index")
	 * fail, the way the client reports a request the engine rejected.
	 */
	static RecordingTransport failing(String operation, String reason) {
		return new RecordingTransport(
			successfulBulk(), false, Map.of(operation, new IOException(reason)));
	}

	/**
	 * Completes the oldest response still pending.
	 *
	 * @throws IllegalStateException when no response is pending
	 */
	void release() {

		Runnable next;

		synchronized (pending) {
			if (pending.isEmpty()) {
				throw new IllegalStateException("no pending response to release");
			}

			next = pending.remove(0);
		}

		next.run();
	}

	/**
	 * Blocks until at least {@code count} operations have been issued, so a
	 * test can release a response only once it exists.
	 *
	 * @throws AssertionError when they are not issued within the timeout
	 */
	void awaitOperations(int count) {

		var deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();

		while (operations.size() < count) {

			if (System.nanoTime() > deadline) {
				throw new AssertionError(
					"expected at least " + count + " operations, got "
						+ operations);
			}

			LockSupport.parkNanos(Duration.ofMillis(10).toNanos());
		}
	}

	private <R> CompletableFuture<R> answer(R response) {
		return answer(future -> future.complete(response));
	}

	private <R> CompletableFuture<R> answerFailure(Throwable throwable) {
		return answer(future -> future.completeExceptionally(throwable));
	}

	private <R> CompletableFuture<R> answer(Consumer<CompletableFuture<R>> outcome) {

		var future = new CompletableFuture<R>();

		if (gated) {
			pending.add(() -> outcome.accept(future));
		}
		else {
			outcome.accept(future);
		}

		return future;
	}

	/**
	 * A bulk response with no errors.
	 */
	static BulkResponse successfulBulk() {
		return BulkResponse.of(b -> b
			.took(0)
			.errors(false)
			.items(List.of()));
	}

	/**
	 * A bulk response whose single item failed with the given reason.
	 */
	static BulkResponse failedBulk(String reason) {
		return BulkResponse.of(b -> b
			.took(0)
			.errors(true)
			.items(List.of(BulkResponseItem.of(item -> item
				.operationType(OperationType.Index)
				.index("an-index")
				.status(400)
				.error(ErrorCause.of(error -> error
					.type("mapper_parsing_exception")
					.reason(reason)))))));
	}

	@Override
	public <RequestT, ResponseT, ErrorT> ResponseT performRequest(
		RequestT request,
		Endpoint<RequestT, ResponseT, ErrorT> endpoint,
		TransportOptions options) {

		throw new UnsupportedOperationException("async only");
	}

	@Override
	@SuppressWarnings("unchecked")
	public <RequestT, ResponseT, ErrorT> CompletableFuture<ResponseT>
	performRequestAsync(
		RequestT request,
		Endpoint<RequestT, ResponseT, ErrorT> endpoint,
		TransportOptions options) {

		if (request instanceof DeleteByQueryRequest) {
			return record("delete", DeleteByQueryResponse.of(b -> b));
		}

		if (request instanceof IndexRequest<?>) {
			return record("index", indexed());
		}

		if (request instanceof BulkRequest) {
			return record("bulk", bulkResponse);
		}

		throw new IllegalArgumentException("unexpected request: " + request);
	}

	/**
	 * Notes the operation and answers it, unless the transport is set to fail it.
	 */
	@SuppressWarnings("unchecked")
	private <R> CompletableFuture<R> record(String operation, Object response) {

		operations.add(operation);

		var failure = failures.get(operation);

		return failure != null ? answerFailure(failure) : answer((R) response);
	}

	/**
	 * The response of a document created by a single-document index request.
	 */
	private static IndexResponse indexed() {
		return IndexResponse.of(b -> b
			.index("an-index")
			.id("an-id")
			.version(1L)
			.result(Result.Created)
			.shards(shards -> shards.total(1).successful(1).failed(0))
			.seqNo(0L)
			.primaryTerm(1L));
	}

	@Override
	public JsonpMapper jsonpMapper() {
		return mapper;
	}

	@Override
	public TransportOptions options() {
		return TransportOptions.builder().build();
	}

	@Override
	public void close() {
	}
}
