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

package io.openk9.reactor.netty.util;

import io.netty.handler.codec.http.HttpMethod;
import reactor.netty.http.server.HttpServerRequest;

import java.util.function.Predicate;

public final class HttpPrefixPredicate implements Predicate<HttpServerRequest> {

	final HttpMethod method;
	final String     prefix;

	public HttpPrefixPredicate(String prefix, HttpMethod method) {
		this.prefix = prefix;
		this.method = method;
	}

	@Override
	public boolean test(HttpServerRequest key) {
		return method.equals(key.method()) && key.uri().startsWith(prefix);
	}

	public static Predicate<HttpServerRequest> of(
		String prefix, HttpMethod httpMethod) {
		return new HttpPrefixPredicate(prefix, httpMethod);
	}


}