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

package io.openk9.search.client.internal.index;

import io.openk9.search.client.api.BulkReactorActionListener;
import io.openk9.search.client.api.DeleteRequestFactory;
import io.openk9.search.client.api.DocWriteRequestFactory;
import io.openk9.search.client.api.Index;
import io.openk9.search.client.api.IndexRequestFactory;
import io.openk9.search.client.api.ReactorActionListener;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import io.openk9.search.client.api.UpdateRequestFactory;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component(
	immediate = true,
	service = Index.class
)
public class IndexImpl implements Index {

	@Override
	public Mono<IndexResponse> sendIndexRequest(IndexRequest request) {
		return Mono.create(sink -> _restHighLevelClientProvider
			.get()
			.indexAsync(
				request, RequestOptions.DEFAULT,
				new ReactorActionListener<>(sink))
		);
	}

	@Override
	public Mono<IndexResponse> sendIndexRequest(
		Function<IndexRequestFactory, IndexRequest> requestFunction) {
		return Mono.defer(() ->
			sendIndexRequest(requestFunction.apply(_docWriteRequestFactory)));
	}

	@Override
	public Mono<UpdateResponse> sendUpdateRequest(UpdateRequest request) {
		return Mono.create(sink -> _restHighLevelClientProvider
			.get()
			.updateAsync(
				request, RequestOptions.DEFAULT,
				new ReactorActionListener<>(sink))
		);
	}

	@Override
	public Mono<UpdateResponse> sendUpdateRequest(
		Function<UpdateRequestFactory, UpdateRequest> requestFunction) {
		return Mono.defer(() ->
			sendUpdateRequest(requestFunction.apply(_docWriteRequestFactory)));
	}

	@Override
	public Mono<DeleteResponse> sendDeleteRequest(
		DeleteRequest request) {
		return Mono.create(sink -> _restHighLevelClientProvider
			.get()
			.deleteAsync(
				request, RequestOptions.DEFAULT,
				new ReactorActionListener<>(sink))
		);
	}

	@Override
	public Mono<DeleteResponse> sendDeleteRequest(
		Function<DeleteRequestFactory, DeleteRequest> requestFunction) {
		return Mono.defer(() ->
			sendDeleteRequest(requestFunction.apply(_docWriteRequestFactory)));
	}

	@Override
	public Flux<BulkItemResponse> sendRequestBulk(DocWriteRequest<?> request) {
		return Flux.create(sink -> _restHighLevelClientProvider
			.get()
			.bulkAsync(
				new BulkRequest()
					.add(request)
					.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL),
				RequestOptions.DEFAULT,
				new BulkReactorActionListener(sink)));
	}

	@Override
	public Flux<BulkItemResponse> sendRequestBulk(
		Function<DocWriteRequestFactory, DocWriteRequest<?>> requestFunction) {
		return Flux.defer(
			() -> sendRequestBulk(
				requestFunction.apply(_docWriteRequestFactory)));
	}

	@Reference
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	@Reference
	private DocWriteRequestFactory _docWriteRequestFactory;

}
