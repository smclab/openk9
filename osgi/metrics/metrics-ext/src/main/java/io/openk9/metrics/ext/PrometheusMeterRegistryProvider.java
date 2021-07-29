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

package io.openk9.metrics.ext;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import io.openk9.metrics.api.MeterRegistryProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.Map;

@Component(
	immediate = true,
	service = {
		MeterRegistryProvider.class,
		RouterHandler.class
	}
)
public class PrometheusMeterRegistryProvider
	implements MeterRegistryProvider, HttpHandler, RouterHandler {

	@Activate
	public void activate(Map<String, Object> props) {
		_prometheusMeterRegistry = new PrometheusMeterRegistry((String key) -> {
			Object value = props.get(key);
			return value == null ? null : String.valueOf(value);
		});
	}

	@Modified
	public void modified(Map<String, Object> props) {
		_prometheusMeterRegistry.close();
		activate(props);
	}

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.get("/v1/prometheus", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return httpResponse.sendString(
			Mono.fromSupplier(_prometheusMeterRegistry::scrape));
	}

	@Override
	public MeterRegistry getMeterRegistry() {
		return _prometheusMeterRegistry;
	}

	private PrometheusMeterRegistry _prometheusMeterRegistry;

}
