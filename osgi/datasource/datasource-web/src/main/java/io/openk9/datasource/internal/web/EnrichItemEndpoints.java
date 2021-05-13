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

import io.openk9.http.exception.HttpException;
import io.openk9.model.EnrichItem;
import io.openk9.datasource.repository.EnrichItemRepository;
import io.openk9.http.util.BaseEndpointRegister;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.json.api.JsonFactory;
import io.openk9.sql.api.client.Criteria;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(immediate = true, service = EnrichItemEndpoints.class)
public class EnrichItemEndpoints extends BaseEndpointRegister {

	@Activate
	public void activate(BundleContext bundleContext) {
		setBundleContext(bundleContext);

		this.registerEndpoint(
			HttpHandler.post("/reorder", this::_reorder)
		);

	}

	private Publisher<Void> _reorder(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		Mono<List<Long>> body =
			Mono
				.from(httpRequest.aggregateBodyToString())
				.map(json -> _jsonFactory.fromJsonList(json, Long.class));

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
									.orElseThrow(() -> new HttpException(500, "EnrichItem for id: " + id + " not found"))
								)
						.collect(Collectors.toList())
					)
					.flatMap(
						enrichItemListOrdered ->
							Flux.zip(
								Flux.fromStream(Stream.iterate(1, x -> x + 1).limit(enrichItemListOrdered.size())),
								Flux.fromIterable(enrichItemListOrdered)
							)
							.map(t2 -> EnrichItem
								.builder()
								.enrichItemId(t2.getT2().getEnrichItemId())
								._position(t2.getT1())
								.active(t2.getT2().getActive())
								.enrichPipelineId(t2.getT2().getEnrichPipelineId())
								.jsonConfig(t2.getT2().getJsonConfig())
								.serviceName(t2.getT2().getServiceName())
								.name(t2.getT2().getName())
								.build()
							)
							.flatMap(_enrichItemRepository::update)
							.then()
					)
			);

		return null;
	}

	@Deactivate
	public void deactivate() {
		this.close();
	}

	private Publisher<Void> _addDatasource(
		HttpRequest request, HttpResponse response) {

		Mono<String> jsonResponse =
			_getDatasourceFromBodyAttribute(request)
				.flatMap(_enrichItemRepository::addEnrichItem)
				.map(_jsonFactory::toJson);

		return response.sendString(jsonResponse);

	}

	private Mono<EnrichItem> _getDatasourceFromBodyAttribute(
		HttpRequest request) {

		return Mono
			.from(request.aggregateBodyToString())
			.map(s -> _jsonFactory.fromJson(s, EnrichItem.class));

	}

	@Override
	public String getBasePath() {
		return "/v1/enrich-item";
	}

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private EnrichItemRepository _enrichItemRepository;
}
