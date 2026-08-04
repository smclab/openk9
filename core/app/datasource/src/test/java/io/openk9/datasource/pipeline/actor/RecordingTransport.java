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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.bulk.OperationType;
import org.opensearch.client.transport.Endpoint;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.TransportOptions;

/**
 * Records the OpenSearch operations issued and returns canned responses; the
 * writers under test inspect only the completion and the bulk error flag, not
 * the response body.
 */
final class RecordingTransport implements OpenSearchTransport {

	final List<String> operations =
		Collections.synchronizedList(new ArrayList<>());
	private final JsonpMapper mapper = new JacksonJsonpMapper();
	private final BulkResponse bulkResponse;

	RecordingTransport() {
		this(successfulBulk());
	}

	RecordingTransport(BulkResponse bulkResponse) {
		this.bulkResponse = bulkResponse;
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
			operations.add("delete");
			return CompletableFuture.completedFuture(
				(ResponseT) DeleteByQueryResponse.of(b -> b));
		}

		if (request instanceof BulkRequest) {
			operations.add("bulk");
			return CompletableFuture.completedFuture((ResponseT) bulkResponse);
		}

		throw new IllegalArgumentException("unexpected request: " + request);
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
