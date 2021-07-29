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

import io.openk9.datasource.repository.EnrichPipelineRepository;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.EnrichPipeline;
import io.openk9.reactor.netty.util.ReactorNettyUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

@Component(immediate = true, service = RouterHandler.class)
public class EnrichPipelineEndpoints implements RouterHandler {

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router
			.post("/v1/enrich-pipeline/", this::_addDatasource)
			.delete("/v1/enrich-pipeline/{id}", this::_deleteDatasource)
			.get("/v1/enrich-pipeline/{id}", this::_getDatasourceById)
			.get("/v1/enrich-pipeline/", this::_findAll)
			.put("/v1/enrich-pipeline/", this::_updateDatasource);
	}

	private Publisher<Void> _findAll(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		Flux<String> response =
			_enrichPipelineRepository
				.findAll()
				.map(_jsonFactory::toJson);

		return httpResponse.sendString(response);
	}

	private Publisher<Void> _updateDatasource(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		Mono<String> jsonResponse =
			_getDatasourceFromBodyAttribute(httpRequest)
				.flatMap(_enrichPipelineRepository::updateEnrichPipeline)
				.map(_jsonFactory::toJson);

		return httpResponse.sendString(jsonResponse);
	}

	private Publisher<Void> _getDatasourceById(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {
		String id = httpRequest.param("id");

		Mono<String> response = _enrichPipelineRepository
			.findByPrimaryKey(Long.valueOf(id))
			.map(_jsonFactory::toJson);

		return httpResponse.sendString(response.flux());
	}

	private Publisher<Void> _deleteDatasource(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		String id = httpRequest.param("id");

		return _enrichPipelineRepository
			.removeEnrichPipeline(Long.valueOf(id))
			.then((Mono<Void>)httpResponse.sendString(Mono.just("{}")));
	}

	private Publisher<Void> _addDatasource(
		HttpServerRequest request, HttpServerResponse response) {

		Mono<String> jsonResponse =
			_getDatasourceFromBodyAttribute(request)
				.flatMap(_enrichPipelineRepository::addEnrichPipeline)
				.map(_jsonFactory::toJson);

		return response.sendString(jsonResponse);

	}

	private Mono<EnrichPipeline> _getDatasourceFromBodyAttribute(
		HttpServerRequest request) {

		return Mono
			.from(ReactorNettyUtils.aggregateBodyAsByteArray(request))
			.map(s -> _jsonFactory.fromJson(s, EnrichPipeline.class));

	}

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private EnrichPipelineRepository _enrichPipelineRepository;

}