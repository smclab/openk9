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
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.netty.resources.LoopResources;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component(
	immediate = true,
	service = ReactorNettyActivator.class,
	configurationPolicy = ConfigurationPolicy.REQUIRE
)
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
		AdvancedByteBufFormat wiretapFormat() default AdvancedByteBufFormat.HEX_DUMP;
	}

	public class RouterHandlerProxy implements InvocationHandler {

		@Override
		public Object invoke(
			Object proxy, Method method, Object[] args) throws Throwable {

			if (method.getName().equals("apply")) {

				HttpServerRoutes httpServerRoutes =
					_prevHttpServerRouters.get();

				if (httpServerRoutes == null) {
					_updateState(
						_semaphore, _getRouterHandler(),
						_prevHttpServerRouters);
				}
				else {
					if (!_semaphore.get()) {
						_updateState(
							_semaphore, _getRouterHandler(),
							_prevHttpServerRouters);
					}
				}

				return method.invoke(_prevHttpServerRouters.get(), args);

			}
			else {
				return method.invoke(proxy, args);
			}

		}

		private void _updateState(
			AtomicBoolean semaphore, RouterHandler _getRouterHandler,
			AtomicReference<HttpServerRoutes> prevHttpServerRouters) {
			semaphore.set(true);
			HttpServerRoutes handle = _getRouterHandler
				.handle(HttpServerRoutes.newRoutes());
			prevHttpServerRouters.set(handle);
		}

		private RouterHandler _getRouterHandler() {

			Collection<RouterHandler> values = _routerHandlerRegistry.values();

			return values
				.stream()
				.reduce(RouterHandler.NOTHING, RouterHandler::andThen);

		}

		private final AtomicReference<HttpServerRoutes> _prevHttpServerRouters =
			new AtomicReference<>();

	}

	@Activate
	void activate(Config config) {

		_thread = new Thread(() -> {

			BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> proxy =
				(BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>>)Proxy.newProxyInstance(
					Thread.currentThread().getContextClassLoader(),
					new Class[]{BiFunction.class},
					new RouterHandlerProxy()
				);

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
					.wiretap("openk9", LogLevel.DEBUG, config.wiretapFormat())
					.runOn(loopResources)
					.compress(config.compress())
					.handle(proxy)
					.mapHandle(this::_mapHandle)
					.port(config.port())
					.protocol(config.httpProtocol());

			httpServer.warmup().block();

			try {
				httpServer
					.bindUntilJavaShutdown(
						Duration.ofSeconds(60), this::callback);
			}
			catch (Exception exception) {

				Throwable unwrap = Exceptions.unwrap(exception);

				if (unwrap instanceof InterruptedException) {
					loopResources.disposeLater().block();
					_log.info("shutdown reactor netty");
				}
			}

		});

		_thread.setDaemon(true);

		_thread.start();

	}

	private Mono<Void> _mapHandle(Mono<Void> voidMono, Connection connection) {
		return voidMono.onErrorResume(throwable ->
			Mono.<Void>defer(() -> {
				Throwable unwrap = Exceptions.unwrap(throwable);

				if (_log.isErrorEnabled()) {
					_log.error(throwable.getMessage(), throwable);
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

	private void callback(DisposableServer server) {
		_log.info("HTTP server started on port: " + server.port());
		try {
			double uptime = ManagementFactory.getRuntimeMXBean().getUptime();
			_log.info("JVM running for " + uptime + "ms");
		}
		catch (Throwable e) {
			// ignore
		}
	}

	@Modified
	void modified(Config config) {

		deactivate();

		activate(config);

	}

	@Deactivate
	void deactivate() {
		_thread.interrupt();
		_thread = null;
	}


	@Reference(
		cardinality = ReferenceCardinality.AT_LEAST_ONE,
		bind = "addRouterHandler",
		unbind = "removeRouterHandler",
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		service = RouterHandler.class
	)
	public void addRouterHandler(
		ServiceReference serviceReference, RouterHandler routerHandler) {
		_routerHandlerRegistry.put(serviceReference, routerHandler);
		_semaphore.set(false);
	}

	public void removeRouterHandler(
		ServiceReference serviceReference, RouterHandler routerHandler) {
		_routerHandlerRegistry.remove(serviceReference);
		_semaphore.set(false);
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

	private final AtomicBoolean _semaphore = new AtomicBoolean(false);

	private Map<
		ServiceReference,
		ExceptionHandler<? extends Throwable>> _exceptionHandlerRegistry =
			new TreeMap<>();

	private Thread _thread;

	private static final Logger _log = LoggerFactory.getLogger(
		ReactorNettyActivator.class.getName());

}
