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

package com.openk9.internal.http.util;

import io.vavr.CheckedFunction4;
import io.vavr.Function4;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HttpServerOperationsUtil {

	public static Mono<Void> withWebsocketSupport(
		HttpServerRequest req, String url, String protocols,
		int maxFramePayloadLength,
		BiFunction<WebsocketInbound, WebsocketOutbound,
			Publisher<Void>> websocketHandler) {

		return _methodInvoker
			.apply(req)
			.apply(url, protocols, maxFramePayloadLength, websocketHandler);

	}

	static Function<HttpServerRequest, Function4<
		String, String, Integer,
		BiFunction<WebsocketInbound, WebsocketOutbound,
			Publisher<Void>>, Mono<Void>>> _methodInvoker;

	static {
		try {

			Class<?> httpServerOperations = ClassLoaderUtil
				.getClassLoader()
				.loadClass(
					"reactor.netty.http.server.HttpServerOperations");

			Method withWebsocketSupport =
				httpServerOperations.getDeclaredMethod(
					"withWebsocketSupport", String.class, String.class,
					int.class, BiFunction.class);

			withWebsocketSupport.setAccessible(true);

			_methodInvoker = req -> CheckedFunction4.<
				String, String, Integer,
				BiFunction<
					WebsocketInbound,
					WebsocketOutbound,
					Publisher<Void>>,
				Mono<Void>>narrow(
				(s, s2, integer, f2) ->
					(Mono<Void>)withWebsocketSupport.invoke(
						req, s, s2, integer, f2)).unchecked();


		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
