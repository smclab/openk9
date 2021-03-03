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

package com.openk9.plugin.internal.web;

import com.openk9.http.util.HttpResponseWriter;
import com.openk9.http.web.Endpoint;
import com.openk9.http.web.HttpHandler;
import com.openk9.http.web.HttpRequest;
import com.openk9.http.web.HttpResponse;
import com.openk9.plugin.api.PluginInfoProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;

@Component(
	immediate = true,
	service = Endpoint.class
)
public class PluginListHttpHandler implements HttpHandler {

	@Override
	public int method() {
		return GET;
	}

	@Override
	public String getPath() {
		return "/v1/plugin";
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		return _httpResponseWriter.write(
			httpResponse, _pluginInfoProvider.getPluginInfoList());
	}

	@Reference
	private PluginInfoProvider _pluginInfoProvider;

	@Reference(target = "(type=json)")
	private HttpResponseWriter _httpResponseWriter;

}
