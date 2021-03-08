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

package io.openk9.search.client.api;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface Index {

	Mono<IndexResponse> sendIndexRequest(IndexRequest request);

	Mono<IndexResponse> sendIndexRequest(
		Function<IndexRequestFactory, IndexRequest> requestFunction);

	Mono<UpdateResponse> sendUpdateRequest(UpdateRequest request);

	Mono<UpdateResponse> sendUpdateRequest(
		Function<UpdateRequestFactory, UpdateRequest> requestFunction);

	Mono<DeleteResponse> sendDeleteRequest(DeleteRequest request);

	Mono<DeleteResponse> sendDeleteRequest(
		Function<DeleteRequestFactory, DeleteRequest> requestFunction);

	Flux<BulkItemResponse> sendRequestBulk(DocWriteRequest<?> request);

	Flux<BulkItemResponse> sendRequestBulk(
		Function<DocWriteRequestFactory, DocWriteRequest<?>> requestFunction);
}
