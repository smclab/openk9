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

package io.openk9.sql.internal;

import io.openk9.core.api.async.Async;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.sql.api.InitSql;
import io.openk9.sql.internal.tracker.InitSqlServiceTracker;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.ConnectionFactoryProvider;
import io.r2dbc.spi.Option;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

@Component(immediate = true)
public class Activator {

	@interface Config {
		String uri() default "r2dbc:postgresql://openk9:openk9@localhost:5432/openk9";
		int poolMaxSize() default 20;
		int poolMaxIdleTime() default 1000;
		int maxLifeTime() default 10_000;
	}

	@Activate
	public void activate(BundleContext bundleContext, Config config) {

		Async.run(() -> {

			ClassLoader classLoader = bundleContext.getBundle().adapt(
				BundleWiring.class).getClassLoader();

			ServiceLoader<ConnectionFactoryProvider> connectionFactoryProviders =
				AccessController.doPrivileged(
					(PrivilegedAction<ServiceLoader<ConnectionFactoryProvider>>)
						() -> ServiceLoader.load(
							ConnectionFactoryProvider.class,
							classLoader));

			Iterator<ConnectionFactoryProvider> iterator =
				connectionFactoryProviders.iterator();

			ConnectionFactoryOptions connectionFactoryOptions =
				ConnectionFactoryOptions.parse(config.uri())
					.mutate()
					.option(Option.valueOf("errorResponseLogLevel"), "ERROR")
					.build();

			ConnectionFactoryProvider connectionFactoryProvider = iterator.next();

			ConnectionFactory connectionFactory =
				connectionFactoryProvider.create(connectionFactoryOptions);

			ConnectionPoolConfiguration connectionPoolConfiguration =
				ConnectionPoolConfiguration.builder(connectionFactory)
					.maxSize(config.poolMaxSize())
					.maxIdleTime(Duration.ofMillis(config.poolMaxIdleTime()))
					.maxLifeTime(Duration.ofMillis(config.maxLifeTime()))
					.build();

			ConnectionPool connectionPool =
				new ConnectionPool(connectionPoolConfiguration);

			ServiceRegistration<ConnectionFactory>
				connectionFactoryServiceRegistration =
				bundleContext.registerService(
					ConnectionFactory.class, connectionPool, null);

			_autoCloseables.add(
				AutoCloseables.mergeAutoCloseableToSafe(
					connectionFactoryServiceRegistration::unregister,
					connectionPool));

			ServiceTracker<InitSql, InitSql> serviceTracker = new ServiceTracker<>(
				bundleContext, InitSql.class, new InitSqlServiceTracker(
				bundleContext,
				connectionPool)
			);

			serviceTracker.open();

			_autoCloseables.add(serviceTracker::close);

		});

	}

	@Deactivate
	public void deactivate() {

		Async.run(() -> {
			Iterator<AutoCloseables.AutoCloseableSafe> iterator =
				_autoCloseables.iterator();

			while (iterator.hasNext()) {
				iterator.next().close();
				iterator.remove();
			}
		});

	}

	private final List<AutoCloseables.AutoCloseableSafe> _autoCloseables =
		new ArrayList<>();

}
