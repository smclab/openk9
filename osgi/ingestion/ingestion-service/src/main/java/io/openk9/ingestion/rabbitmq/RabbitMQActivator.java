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

package io.openk9.ingestion.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import io.openk9.osgi.util.AutoCloseables;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.ReceiverOptions;
import reactor.rabbitmq.SenderOptions;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Component(immediate = true, service = RabbitMQActivator.class)
public class RabbitMQActivator {

	@interface Config {
		String uri() default "amqp://projectq:projectq@localhost:5672";
		String connectionNameSuffix() default "_rabbitmq_osgi";
	}

	@Activate
	public void activate(
		BundleContext bundleContext, Config config) throws Exception {

		ConnectionFactory connectionFactory = new ConnectionFactory();

		connectionFactory.useNio();

		String hostName = InetAddress.getLocalHost().getHostName();

		Map<String, Object> clientProperties =
			connectionFactory.getClientProperties();

		Map<String, Object> newClientProperties =
			clientProperties != null
				? new HashMap<>(clientProperties)
				: new HashMap<>();

		newClientProperties.put(
			"connection_name",
			hostName + config.connectionNameSuffix()
		);

		connectionFactory.setClientProperties(newClientProperties);

		connectionFactory.setUri(config.uri());

		_registerService(
			bundleContext, Supplier.class, () -> connectionFactory,
			Collections.singletonMap("rabbit", "connection-factory"));

		_registerService(
			bundleContext, connectionFactory, cf -> new SenderOptions()
				.connectionFactory(cf)
				.resourceManagementScheduler(Schedulers.boundedElastic()),
			RabbitFlux::createSender,
			Collections.singletonMap("rabbit", "sender"));

		_registerService(
			bundleContext, connectionFactory,
			cf -> new ReceiverOptions().connectionFactory(cf),
			RabbitFlux::createReceiver,
			Collections.singletonMap("rabbit", "receiver"));

	}

	@Modified
	public void modified(
		BundleContext bundleContext, Config config) throws Exception {

		deactivate();
		
		activate(bundleContext, config);
	}

	@Deactivate
	public void deactivate() {
		Iterator<AutoCloseables.AutoCloseableSafe> iterator =
			_autoCloseables.iterator();

		while (iterator.hasNext()) {
			iterator.next().close();
			iterator.remove();
		}
	}

	private <OPTIONS, SERVICE extends Object & AutoCloseable>
		void _registerService(
			BundleContext bundleContext, ConnectionFactory connectionFactory,
			Function<ConnectionFactory, OPTIONS> createOptions,
			Function<OPTIONS, SERVICE> createService,
			Map<String, Object> props) {

		OPTIONS options = createOptions.apply(connectionFactory);

		SERVICE service = createService.apply(options);

		ServiceRegistration<Supplier> senderServiceRegistration =
			bundleContext.registerService(
				Supplier.class, () -> service, new Hashtable<>(props));

		_autoCloseables.add(
			AutoCloseables.mergeAutoCloseableToSafe(
				service, senderServiceRegistration::unregister));

	}

	private <T> void _registerService(
		BundleContext bundleContext, Class<T> clazz, T service,
		Map<String, Object> props) {

		ServiceRegistration<T> objectServiceRegistration =
			bundleContext.registerService(
				clazz, service, new Hashtable<>(props));

		_autoCloseables.add(
			AutoCloseables.mergeAutoCloseableToSafe(
				objectServiceRegistration::unregister));

	}
	private final Collection<AutoCloseables.AutoCloseableSafe> _autoCloseables =
		new ArrayList<>();

}
