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

package io.openk9.plugin.internal.web;

import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import io.openk9.plugin.api.PluginInfoProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class PluginListHttpHandler
	implements HttpHandler, RouterHandler {

	@Override
	public HttpServerRoutes handle(
		HttpServerRoutes router) {
		return router.get("/v1/plugin", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return _httpResponseWriter.write(
			httpResponse, _pluginInfoProvider.getPluginInfoList());
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private PluginInfoProvider _pluginInfoProvider;

	@Reference(target = "(type=json)", policyOption = ReferencePolicyOption.GREEDY)
	private HttpResponseWriter _httpResponseWriter;


}
