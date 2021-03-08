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

package io.openk9.ingestion.rabbitmq.bind;

import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.rabbitmq.util.Constants;
import io.openk9.osgi.util.AutoCloseables;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BindingBundleTrackerCustomizer
	implements BundleTrackerCustomizer<Void> {

	@Override
	public Void addingBundle(Bundle bundle, BundleEvent event) {

		if (bundle.getState() != Bundle.ACTIVE) {
			removedBundle(bundle, event, null);
			return null;
		}

		Dictionary<String, String> headers = bundle.getHeaders(null);

		String exchange = headers.get(Constants.RABBIT_EXCHANGE);
		String routingKey = headers.get(Constants.RABBIT_ROUTING_KEY);
		String queue = headers.get(Constants.RABBIT_QUEUE);

		if (exchange == null || routingKey == null || queue == null) {
			return null;
		}

		String[] split = exchange.split("\\.");

		Binding.Exchange.Type type = Binding.Exchange.Type.valueOf(split[1]);

		Dictionary<String, Object> senderProps = new Hashtable<>();

		senderProps.put("exchange", exchange);
		senderProps.put("routingKey", routingKey);
		senderProps.put("queue", queue);
		senderProps.put("exchangeType", type.name());

		ServiceRegistration<Binding> serviceRegistration =
			bundle
				.getBundleContext()
				.registerService(
					Binding.class,
					Binding.of(exchange, type, routingKey, queue),
					senderProps);

		_registrationMap.put(
			bundle, AutoCloseables
				.mergeAutoCloseableToSafe(serviceRegistration::unregister));

		return null;
	}

	@Override
	public void modifiedBundle(
		Bundle bundle, BundleEvent event, Void nothing) {

		removedBundle(bundle, event, null);

		addingBundle(bundle, event);

	}

	@Override
	public void removedBundle(
		Bundle bundle, BundleEvent event, Void nothing) {

		AutoCloseables.AutoCloseableSafe autoCloseableSafe =
			_registrationMap.remove(bundle);

		if (autoCloseableSafe != null) {
			autoCloseableSafe.close();
		}

	}

	private final Map<Bundle, AutoCloseables.AutoCloseableSafe> _registrationMap =
		new ConcurrentHashMap<>();

}
