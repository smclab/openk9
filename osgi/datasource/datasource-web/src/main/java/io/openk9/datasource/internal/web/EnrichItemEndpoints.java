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

package io.openk9.datasource.internal.web;

import io.openk9.datasource.repository.EnrichItemRepository;
import io.openk9.http.exception.HttpException;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.EnrichItem;
import io.openk9.reactor.netty.util.ReactorNettyUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(immediate = true, service = RouterHandler.class)
public class EnrichItemEndpoints implements RouterHandler {

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.post("/v1/enrich-item/reorder", this::_reorder);
	}

	private Publisher<Void> _reorder(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		Mono<List<Long>> body =
			Mono
				.from(ReactorNettyUtils.aggregateBodyAsByteArray(httpRequest))
				.map(json -> _jsonFactory.fromJsonList(json, Long.class));


		Flux<Void> response =
			body
				.flatMapMany(ids ->
					_enrichItemRepository
						.findByPrimaryKeys(ids)
						.collectList()
						.map(
							enrichItemList ->
								ids
									.stream()
									.map(id -> enrichItemList
										.stream()
										.filter(enrichItem ->
											enrichItem
												.getEnrichItemId()
												.equals(id))
										.findFirst()
										.orElseThrow(() -> new HttpException(
											500, "EnrichItem for id: " + id +
												 " not found"))
									)
									.collect(Collectors.toList())
						)
						.flatMap(
							enrichItemListOrdered ->
								Flux.zip(
									Flux.fromStream(
										Stream.iterate(1, x -> x + 1).limit(
											enrichItemListOrdered.size())),
									Flux.fromIterable(enrichItemListOrdered)
								)
									.map(t2 -> EnrichItem
										.builder()
										.enrichItemId(t2.getT2().getEnrichItemId())
										._position(t2.getT1())
										.active(t2.getT2().getActive())
										.enrichPipelineId(
											t2.getT2().getEnrichPipelineId())
										.jsonConfig(t2.getT2().getJsonConfig())
										.serviceName(t2.getT2().getServiceName())
										.name(t2.getT2().getName())
										.build()
									)
									.flatMap(_enrichItemRepository::update)
									.then()
						)
				);

		return _httpResponseWriter.write(
			httpResponse, response.then(Mono.just("{}")));

	}

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private EnrichItemRepository _enrichItemRepository;

	@Reference
	private HttpResponseWriter _httpResponseWriter;
}
