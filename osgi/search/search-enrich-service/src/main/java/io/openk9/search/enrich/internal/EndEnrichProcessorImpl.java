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

package io.openk9.search.enrich.internal;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.json.api.ObjectNode;
import io.openk9.search.client.api.DocWriteRequestFactory;
import io.openk9.search.client.api.IndexBus;
import io.openk9.search.client.api.Search;
import io.openk9.search.client.api.util.SearchUtil;
import io.openk9.search.enrich.api.EndEnrichProcessor;
import io.openk9.search.enrich.api.dto.EnrichProcessorContext;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = EndEnrichProcessor.class
)
public class EndEnrichProcessorImpl implements EndEnrichProcessor {

	@Override
	public String name() {
		return EndEnrichProcessorImpl.class.getName();
	}

	@Override
	public Mono<Void> exec(
		EnrichProcessorContext enrichProcessorContext) {

		return _createDocWriterRequest(enrichProcessorContext)
			.doOnNext(_indexBus::sendRequest)
			.then();

	}

	private Mono<DocWriteRequest> _createDocWriterRequest(
		EnrichProcessorContext enrichProcessorContext) {

		return Mono.defer(() -> {

			ObjectNode objectNode =
				_cborFactory.treeNode(
					enrichProcessorContext.getObjectNode()).toObjectNode();

			long tenantId = objectNode.get("tenantId").asLong();

			String contentId = objectNode.get("contentId").asText();

			String pluginDriverName = enrichProcessorContext.
				getPluginDriverName();

			return _search
				.search(factory -> {

					SearchRequest searchRequest =
						factory.createSearchRequestData(
							tenantId, pluginDriverName);

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
				.map(e -> {
					UpdateRequest updateRequest =
						_docWriteRequestFactory.createDataUpdateRequest(
							tenantId, pluginDriverName, e.getId());
					return updateRequest.doc(
						objectNode.toString(), XContentType.JSON);
				})
				.cast(DocWriteRequest.class)
				.switchIfEmpty(
					Mono.fromSupplier(() -> {
						IndexRequest indexRequest =
							_docWriteRequestFactory.createDataIndexRequest(
								tenantId, pluginDriverName);
						return indexRequest.source(
							objectNode.toString(), XContentType.JSON);
					}));
		});
	}

	@Reference
	private Search _search;

	@Reference
	private DocWriteRequestFactory _docWriteRequestFactory;

	@Reference
	private CBORFactory _cborFactory;

	@Reference
	private IndexBus _indexBus;

}
