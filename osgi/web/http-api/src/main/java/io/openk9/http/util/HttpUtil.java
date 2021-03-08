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

import io.openk9.http.web.HttpRequest;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.function.Function;

public class HttpUtil {

	public static String getHostName(HttpRequest httpRequest) {
		InetSocketAddress inetSocketAddress = httpRequest.hostAddress();
		return inetSocketAddress.getHostName();
	}

	public static <T> Mono<T> mapBodyRequest(
		HttpRequest httpRequest, Function<String, T> mapper) {

		return Mono
			.from(httpRequest.aggregateBodyToString())
			.map(mapper);

	}

}
