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

package io.openk9.schemaregistry.internal.endpoint;

import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.json.api.JsonFactory;
import io.openk9.schemaregistry.model.Schema;
import io.openk9.schemaregistry.repository.SchemaRegistryRepository;
import io.openk9.schemaregistry.validator.SchemaValidator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Component(service = Endpoint.class, immediate = true)
public class RegisterSchema implements HttpHandler {
	@Override
	public String getPath() {
		return "/v1/schema-registry";
	}

	@Override
	public int method() {
		return POST;
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		Mono<Schema> registerdSchema = Mono.from(httpRequest.bodyAttributes())
			.map(map -> map.get("schema"))
			.filter(strings -> !strings.isEmpty())
			.map(l -> l.get(0))
			.map(json -> _jsonFactory.fromJson(json, Schema.class));

		httpResponse.addHeader("Content-Type", "application/json");

		return httpResponse.sendString(
			_schemaRegistryRepository
				.registerSchema(registerdSchema)
				.map(schema -> _jsonFactory.toJson(schema)));

	}

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private SchemaRegistryRepository _schemaRegistryRepository;

	@Reference(target = "(format=avro)")
	private SchemaValidator _schemaValidator;


}
