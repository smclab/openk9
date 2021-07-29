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

package io.openk9.ext.http.response.serialization;

import io.openk9.http.util.HttpResponseWriter;
import io.openk9.json.api.JsonFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

@Component(
	immediate = true,
	property = "type=json",
	service = HttpResponseWriter.class
)
public class JsonHttpResponseWriter implements HttpResponseWriter {

	@Override
	public Publisher<Void> write(
		HttpServerResponse httpResponse, Object value) {
		return write(httpResponse, Mono.just(value));
	}

	@Override
	public Publisher<Void> write(
		HttpServerResponse httpResponse, Flux<?> value) {
		return write(httpResponse, value.collectList());
	}

	@Override
	public Publisher<Void> write(
		HttpServerResponse httpResponse, Mono<?> value) {
		return httpResponse
			.header("Content-type", "application/json")
			.sendString(value.map(_jsonFactory::toJson));
	}

	@Reference
	private JsonFactory _jsonFactory;

}
