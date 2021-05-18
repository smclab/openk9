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

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.openk9.http.socket.WebSocketHandler;
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.internal.http.util.HttpPredicateUtil;
import io.openk9.internal.http.util.ServiceTrackerProcessor;
import io.openk9.internal.http.util.UrlUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.resources.LoopResources;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(immediate = true, service = ReactorNettyActivator.class)
public class ReactorNettyActivator {

	@interface Config {
		int port() default 8080;
		String name() default "openk9-http";
		int selectCount() default 1;
		int workerCount() default -1;
		boolean daemon() default true;
		boolean wiretap() default true;
		boolean accessLog() default true;
		String contextPath() default "/";
		boolean compress() default false;
		boolean forwarded() default true;
		HttpProtocol httpProtocol() default HttpProtocol.HTTP11;
	}

	@Activate
	public void init(BundleContext bundleContext, Config config) {

		LoopResources loopResources = LoopResources
			.create(
				config.name(),
				config.selectCount(),
				config.workerCount() == -1
					? LoopResources.DEFAULT_IO_WORKER_COUNT
					: config.workerCount(),
				config.daemon());

		HttpServer httpServer =
			HttpServer
				.create()
				.metrics(true, Function.identity())
				.accessLog(config.accessLog())
				.forwarded(config.forwarded())
				.wiretap(config.wiretap())
				.runOn(loopResources)
				.compress(config.compress())
				.handle(this::_handle)
				.port(config.port())
				.protocol(config.httpProtocol());

		httpServer.warmup().block();

		DisposableServer disposableServer = httpServer
			.bindNow();

		_dispose = disposableServer::disposeNow;

		_serviceTracker =
			ServiceTrackerProcessor
				.create(bundleContext, Endpoint.class)
				.map(b -> s -> {

						String contextPath = config.contextPath();

						Object property = s.getProperty("base.path");

						String basePath = UrlUtil.BLANK;

						if (property != null) {
							basePath =(String)property;
						}

						Endpoint service = b.getService(s);

						String path = service.getPath();

						path = contextPath + basePath + path;

						String[] split = path.split(UrlUtil.S_SLASH);

						path = Arrays
							.stream(split)
							.filter(e -> !e.isEmpty())
							.collect(
								Collectors.joining(
									UrlUtil.S_SLASH, UrlUtil.S_SLASH,
									path.endsWith("/") ? "/" : ""));

						if (_log.isDebugEnabled()) {
							_log.debug(
								String.format(
									"path: %s, service: %s",
									path, service)
							);
						}

						if (service instanceof WebSocketHandler) {
							return HttpEnpointRoutes.ws(
								(WebSocketHandler)service,
								HttpPredicateUtil.get(path));
						}
						else {

							HttpHandler httpHandler =(HttpHandler)service;

							HttpHandler httpFilter =
								_httpFilterRegistry.getHttpFilter(
									path, (HttpHandler)service);


							if (httpHandler.prefix()) {
								return HttpEnpointRoutes.noWs(
									httpFilter,
									HttpPredicateUtil.prefix(path)
								);
							}

							return HttpEnpointRoutes.noWs(
								httpFilter,
								HttpPredicateUtil
									.getPredicate(httpHandler.method())
									.apply(path)
							);

						}

					},
					b -> (r, o) -> b.ungetService(r)
				)
				.open();

	}

	@Deactivate
	public void destroy() {
		_dispose.run();
		_dispose = _EMPTY_RUNNABLE;
		_serviceTracker.close();
		_serviceTracker = null;
	}

	@Modified
	public void modified(BundleContext bundleContext, Config config) {
		destroy();
		init(bundleContext, config);
	}

	private Publisher<Void> _handle(
		HttpServerRequest req, HttpServerResponse res) {

		ServiceReference<Endpoint>[] serviceReferences =
			_serviceTracker.getServiceReferences();

		java.util.List<HttpEnpointRoutes> services;

		if (serviceReferences == null) {
			services = Collections.emptyList();
		}
		else {
			services = Arrays
				.stream(serviceReferences)
				.sorted()
				.map(_serviceTracker::getService)
				.collect(Collectors.toList());
		}

		boolean isWebSocketHttpRequest =
			req
				.requestHeaders()
				.containsValue(
					HttpHeaderNames.CONNECTION,
					HttpHeaderValues.UPGRADE, true);

		java.util.List<HttpEnpointRoutes> list =
			services.stream()
				.filter(s -> s.getCondition().test(req))
				.collect(Collectors.toList());

		if (list.size() >= 2) {
			list = Collections.singletonList(list.get(0));
		}

		if (isWebSocketHttpRequest) {
			return _handleFirstHER(
				req, res, list
					.stream()
					.filter(HttpEnpointRoutes::isWebSocket));
		}
		else {
			return _handleFirstHER(
				req, res, list
					.stream()
					.filter(HttpEnpointRoutes::isNotWebSocket));

		}

	}

	private Publisher<Void> _handleFirstHER(
		HttpServerRequest req, HttpServerResponse res,
		Stream<HttpEnpointRoutes> list) {

		return list
			.map(s -> s.handle(req, res))
			.findFirst()
			.orElseGet(res::sendNotFound);
	}

	@Reference
	private HttpFilterRegistry _httpFilterRegistry;

	private ServiceTracker<Endpoint, HttpEnpointRoutes> _serviceTracker;

	private Runnable _dispose = _EMPTY_RUNNABLE;

	private static final Runnable _EMPTY_RUNNABLE = () -> {};

	private static final Logger _log = LoggerFactory.getLogger(
		ReactorNettyActivator.class.getName());

}
