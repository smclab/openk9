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

package com.openk9.internal.http.client;

import com.openk9.http.client.HttpClient;
import com.openk9.http.client.HttpClientFactory;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = HttpClientFactory.class
)
public class HttpClientFactoryImpl implements HttpClientFactory {

	@Override
	public HttpClient getHttpClient(String baseUrl) {
		return new HttpClientImpl(
			reactor.netty.http.client.HttpClient
				.create()
				.baseUrl(baseUrl)
		);
	}

}
