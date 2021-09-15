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

package io.openk9.http.util;

import io.netty.handler.codec.http.QueryStringDecoder;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class HttpUtil {

	public static String getHostName(HttpServerRequest httpRequest) {
		InetSocketAddress inetSocketAddress = httpRequest.hostAddress();
		return inetSocketAddress.getHostName();
	}

	public static Mono<byte[]> bodyToByteArray(HttpServerRequest httpRequest) {
		return httpRequest.receive().aggregate().asByteArray();
	}

	public static Map<String, List<String>> getQueryParams(
		HttpServerRequest request) {

		QueryStringDecoder queryStringDecoder =
			new QueryStringDecoder(request.uri());

		return queryStringDecoder.parameters();

	}


}
