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

package io.openk9.internal.http;

import io.openk9.http.socket.WebSocketHandler;
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.internal.http.util.ClassLoaderUtil;
import io.openk9.internal.http.util.Predicates;
import io.openk9.internal.http.ws.WebSocketSessionFactory;
import io.vavr.CheckedFunction4;
import io.vavr.Function4;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class HttpEnpointRoutes<T extends Endpoint> {

	private final T _endpoint;

	private final Predicate<HttpServerRequest> _condition;

	HttpEnpointRoutes(
		T endpoint,
		Predicate<HttpServerRequest> condition) {
		_endpoint = endpoint;
		_condition = condition;
	}

	T getEndpoint() {
		return _endpoint;
	}

	Predicate<HttpServerRequest> getCondition() {
		return _condition;
	}

	public abstract boolean isWebSocket();

	public abstract boolean isNotWebSocket();

	public abstract Publisher<Void> handle(
		HttpServerRequest request, HttpServerResponse response);

	private static class WebSocketRoutes extends HttpEnpointRoutes<WebSocketHandler> {

		WebSocketRoutes(
			WebSocketHandler endpoint,
			Predicate<HttpServerRequest> condition) {
			super(endpoint, condition);
		}

		@Override
		public boolean isWebSocket() {
			return true;
		}

		@Override
		public boolean isNotWebSocket() {
			return !isWebSocket();
		}

		@Override
		public Publisher<Void> handle(
			HttpServerRequest request, HttpServerResponse response) {

			return withWebsocketSupport(
				request, getEndpoint().getPath(), getEndpoint().getProtocols(),
				getEndpoint().getMaxFramePayloadLength(),
				(in, out) ->
					getEndpoint().apply(
						WebSocketSessionFactory.createWebSocketSession(
							in, out, request, response)));

		}

	}

	private static class NoWebSocketRoutes extends HttpEnpointRoutes<HttpHandler> {

		NoWebSocketRoutes(
			HttpHandler endpoint,
			Predicate<HttpServerRequest> condition) {
			super(endpoint, condition);
		}

		@Override
		public boolean isWebSocket() {
			return false;
		}

		@Override
		public boolean isNotWebSocket() {
			return !isWebSocket();
		}

		@Override
		public Publisher<Void> handle(
			final HttpServerRequest request, HttpServerResponse response) {

			Arrays
				.stream(Predicates.getPredicateArray(getCondition()))
				.filter(p -> p.test(request))
				.findFirst()
				.filter(s ->
					_httpPredicateClass
						.isAssignableFrom(s.getClass()))
				.ifPresent(p -> request.paramsResolver(
					(Function<String, Map <String, String>>)p));

			HttpResponseImpl httpResponse = new HttpResponseImpl(response);

			return Mono.from(
				getEndpoint()
					.apply(new HttpRequestImpl(request), httpResponse)
			)
				.onErrorResume(
					getEndpoint().errorHandler().exceptionType(),
					throwable -> Mono.from(
						getEndpoint()
							.errorHandler()
							.apply(throwable, httpResponse)));


		}

	}

	static HttpEnpointRoutes ws(
		WebSocketHandler endpoint, Predicate<HttpServerRequest> condition) {
		return new WebSocketRoutes(endpoint, condition);
	}

	static HttpEnpointRoutes noWs(
		HttpHandler endpoint, Predicate<HttpServerRequest> condition) {
		return new NoWebSocketRoutes(endpoint, condition);
	}

	private static final Class<?> _httpPredicateClass;

	static {
		try {
			_httpPredicateClass =
				ClassLoaderUtil.getClassLoader().loadClass(
					"reactor.netty.http.server.HttpPredicate");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

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

	private static Mono<Void> withWebsocketSupport(
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

}
