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

import io.netty.handler.logging.LogLevel;
import io.openk9.http.web.ExceptionHandler;
import io.openk9.http.web.RouterHandler;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.netty.resources.LoopResources;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

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
		boolean compress() default false;
		boolean forwarded() default true;
		HttpProtocol httpProtocol() default HttpProtocol.HTTP11;
		AdvancedByteBufFormat wiretapFormat() default AdvancedByteBufFormat.TEXTUAL;
	}

	@Activate
	void activate(Config config) {

		_executorService = Executors.newSingleThreadExecutor();

		_executorService.execute(() -> {

			LoopResources loopResources = LoopResources
				.create(
					config.name(),
					config.selectCount(),
					config.workerCount() == -1
						? LoopResources.DEFAULT_IO_WORKER_COUNT
						: config.workerCount(),
					config.daemon());

			RouterHandler routerHandler = _getRouterHandler();

			HttpServer httpServer =
				HttpServer
					.create()
					.metrics(true, Function.identity())
					.accessLog(config.accessLog())
					.forwarded(config.forwarded())
					.wiretap(config.wiretap())
					.wiretap("openk9", LogLevel.DEBUG, config.wiretapFormat())
					.runOn(loopResources)
					.compress(config.compress())
					.handle(routerHandler.handle(HttpServerRoutes.newRoutes()))
					.mapHandle(this::_mapHandle)
					.port(config.port())
					.protocol(config.httpProtocol());

			httpServer.warmup().block();

			DisposableServer disposableServer = httpServer
				.bindNow();

			_log.info("reactor netty listening on " + config.port());

			try {

				disposableServer
					.onDispose()
					.block();
			}
			catch (Exception exception) {

				Throwable unwrap = Exceptions.unwrap(exception);

				if (unwrap instanceof InterruptedException) {
					loopResources.dispose();
					disposableServer.dispose();
					_log.info("shutdown reactor netty");
					Thread.currentThread().interrupt();
				}
			}

		});

	}

	private Mono<Void> _mapHandle(Mono<Void> voidMono, Connection connection) {
		return voidMono.onErrorResume(throwable ->
			Mono.<Void>defer(() -> {
				Throwable unwrap = Exceptions.unwrap(throwable);

				if (_log.isErrorEnabled()) {
					_log.debug(throwable.getMessage());
				}
				else if (_log.isDebugEnabled()) {
					_log.debug(throwable.getMessage(), throwable);
				}

				HttpServerResponse response =
					(HttpServerResponse) connection;

				ExceptionHandler exceptionHandler =
					ExceptionHandler.INTERNAL_SERVER_ERROR;

				for (
					ExceptionHandler<? extends Throwable> e :
						_exceptionHandlerRegistry.values()) {

					if (e.getType() == unwrap.getClass()) {
						exceptionHandler = e;
						break;
					}

				}

				return exceptionHandler.map(unwrap, response);
			})
		);
	}

	private RouterHandler _getRouterHandler() {

		Collection<RouterHandler> values = _routerHandlerRegistry.values();

		return values
			.stream()
			.reduce(RouterHandler.NOTHING, RouterHandler::andThen);

	}

	@Modified
	void modified(Config config) {

		deactivate();

		activate(config);

	}

	@Deactivate
	void deactivate() {
		_executorService.shutdownNow();
	}


	@Reference(
		cardinality = ReferenceCardinality.AT_LEAST_ONE,
		bind = "addRouterHandler",
		unbind = "removeRouterHandler",
		policy = ReferencePolicy.STATIC,
		policyOption = ReferencePolicyOption.GREEDY,
		service = RouterHandler.class
	)
	public void addRouterHandler(
		ServiceReference serviceReference, RouterHandler routerHandler) {
		_routerHandlerRegistry.put(serviceReference, routerHandler);
	}

	public void removeRouterHandler(
		ServiceReference serviceReference, RouterHandler routerHandler) {
		_routerHandlerRegistry.remove(serviceReference);
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		bind = "addExceptionHandler",
		unbind = "removeExceptionHandler",
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		service = ExceptionHandler.class
	)
	public void addExceptionHandler(
		ServiceReference serviceReference, ExceptionHandler exceptionHandler) {
		_exceptionHandlerRegistry.put(serviceReference, exceptionHandler);
	}

	public void removeExceptionHandler(
		ServiceReference serviceReference, ExceptionHandler exceptionHandler) {
		_exceptionHandlerRegistry.remove(serviceReference);
	}

	private Map<ServiceReference, RouterHandler> _routerHandlerRegistry =
		new TreeMap<>();

	private Map<
		ServiceReference,
		ExceptionHandler<? extends Throwable>> _exceptionHandlerRegistry =
			new TreeMap<>();

	private ExecutorService _executorService;

	private static final Logger _log = LoggerFactory.getLogger(
		ReactorNettyActivator.class.getName());

}
