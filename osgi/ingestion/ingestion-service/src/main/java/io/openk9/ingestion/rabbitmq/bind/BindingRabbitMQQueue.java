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
import io.openk9.ingestion.api.ReceiverReactor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.ServiceTracker;
import reactor.rabbitmq.Sender;

import java.util.function.Supplier;

@Component(
	immediate = true,
	service = BindingRabbitMQQueue.class
)
public class BindingRabbitMQQueue {

	@Activate
	public void activate(BundleContext bundleContext) {

		Sender sender = _senderProvider.get();

		_bundleTracker = new BundleTracker<>(
			bundleContext, Bundle.ACTIVE | Bundle.STOPPING,
			new BindingBundleTrackerCustomizer());

		_bundleTracker.open();

		_serviceTracker = new ServiceTracker<>(
			bundleContext, Binding.class,
			new BindingServiceTrackerCustomizer(
				sender, _receiverReactor, bundleContext));

		_serviceTracker.open();

	}

	@Modified
	public void modified(BundleContext bundleContext) {
		deactivate();

		activate(bundleContext);
	}


	@Deactivate
	public void deactivate() {
		_bundleTracker.close();
		_serviceTracker.close();
	}

	private BundleTracker<Void> _bundleTracker;

	private ServiceTracker<Binding, Binding> _serviceTracker;

	@Reference(target = "(rabbit=sender)", policyOption = ReferencePolicyOption.GREEDY)
	private Supplier<Sender> _senderProvider;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ReceiverReactor _receiverReactor;

}
