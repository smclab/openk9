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

package io.openk9.index.writer.internal.writer;

import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.ReceiverReactor;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.ObjectNode;
import io.openk9.search.client.api.IndexBus;
import io.openk9.search.client.api.Search;
import io.openk9.search.client.api.util.SearchUtil;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = InsertIndexWriter.class
)
public class InsertIndexWriter {

	@Activate
	void activate() {

		_disposable =
			_receiverReactor
				.consumeAutoAck(_binding.getQueue(), 1)
				.concatMap(
					delivery -> {

						ObjectNode enrichProcessorContext =
							_jsonFactory
								.fromJsonToJsonNode(delivery.getBody())
								.toObjectNode();

						String routingKey =
							delivery
								.getEnvelope()
								.getRoutingKey();

						long tenantId =
							enrichProcessorContext
								.get("datasourceContext")
								.get("tenant")
								.get("tenantId")
								.asLong();

						String pluginDriverName = enrichProcessorContext
							.get("pluginDriver").get("name").asText();

						String indexName = String.join(
							"-", Long.toString(tenantId), pluginDriverName, "data");

						if (routingKey.endsWith("entity")) {
							return Mono.fromSupplier(() ->
								new IndexRequest(
									indexName
								).source(
									enrichProcessorContext.toString(),
									XContentType.JSON)
							);
						}

						return _createDocWriterRequest(
							indexName, enrichProcessorContext);

					})
				.onErrorContinue(this::_manageExceptions)
				.doOnNext(_indexBus::sendRequest)
				.subscribe();

	}

	@Deactivate
	void deactivate() {
		_disposable.dispose();
	}

	private void _manageExceptions(Throwable throwable, Object object) {

		if (_log.isErrorEnabled()) {
			if (object == null) {
				_log.error(throwable.getMessage(), throwable);
			}
			else {
				_log.error(
					"error on object: { " + object.toString() + " }",
					throwable);
			}
		}

	}

	private Mono<DocWriteRequest> _createDocWriterRequest(
		String indexName, ObjectNode enrichProcessorContext) {

		return Mono.defer(() -> {

			ObjectNode objectNode =
				enrichProcessorContext
					.get("payload")
					.toObjectNode();

			String contentId = objectNode.get("contentId").asText();

			return _search
				.search(factory -> {

					SearchRequest searchRequest = new SearchRequest(indexName);

					MatchQueryBuilder matchQueryBuilder =
						QueryBuilders.matchQuery("contentId", contentId);

					SearchSourceBuilder searchSourceBuilder =
						new SearchSourceBuilder();

					searchSourceBuilder.query(matchQueryBuilder);

					return searchRequest.source(searchSourceBuilder);

				})
				.onErrorReturn(SearchUtil.EMPTY_SEARCH_RESPONSE)
				.filter(e -> e.getHits().getHits().length > 0)
				.flatMapIterable(SearchResponse::getHits)
				.next()
				.map(e -> new UpdateRequest(indexName, e.getId())
					.doc(objectNode.toString(), XContentType.JSON)
				)
				.cast(DocWriteRequest.class)
				.switchIfEmpty(
					Mono.fromSupplier(() ->
						new IndexRequest(indexName).source(
							objectNode.toString(), XContentType.JSON)
					));
		});
	}

	private Disposable _disposable;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ReceiverReactor _receiverReactor;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private IndexBus _indexBus;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private Search _search;

	@Reference(
		target = "(component.name=io.openk9.index.writer.internal.binding.IndexWriterBinding)"
	)
	private Binding _binding;

	private static final Logger _log = LoggerFactory.getLogger(
		InsertIndexWriter.class);

}
