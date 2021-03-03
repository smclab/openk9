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

package com.openk9.schemaregistry.internal.endpoint;

import com.openk9.http.web.Endpoint;
import com.openk9.http.web.HttpHandler;
import com.openk9.http.web.HttpRequest;
import com.openk9.http.web.HttpResponse;
import com.openk9.json.api.JsonFactory;
import com.openk9.schemaregistry.repository.SchemaRegistryRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Component(service = Endpoint.class, immediate = true)
public class FindOneBySubjectFormatVersion implements HttpHandler {

	@Override
	public String getPath() {
		return "/v1/schema-registry/{subject}/{format}/v{version}";
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		String subject = httpRequest.pathParam("subject");
		String format = httpRequest.pathParam("format");
		String version = httpRequest.pathParam("version");

		Mono<String> result =
			_schemaRegistryRepository
				.findBySubjectAndFormatAndVersion(
					subject, format, Integer.valueOf(version))
				.map(_jsonFactory::toJson);

		httpResponse.addHeader("Content-Type", "application/json");

		return httpResponse.sendString(result);
	}

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private SchemaRegistryRepository _schemaRegistryRepository;

}
