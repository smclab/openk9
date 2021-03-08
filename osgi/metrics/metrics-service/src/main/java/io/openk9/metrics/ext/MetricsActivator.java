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

package io.openk9.metrics.ext;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.openk9.metrics.api.MeterRegistryProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@Component(
	immediate = true,
	service = MetricsActivator.class
)
public class MetricsActivator {

	@Activate
	public void activate(BundleContext context) {

		_serviceTracker = new ServiceTracker<>(
			context, MeterRegistryProvider.class,
			new ServiceTrackerCustomizer<
				MeterRegistryProvider, MeterRegistry>() {
				@Override
				public MeterRegistry addingService(
					ServiceReference<MeterRegistryProvider> reference) {

					MeterRegistryProvider service =
						context.getService(reference);

					MeterRegistry meterRegistry = service.getMeterRegistry();

					Metrics.addRegistry(meterRegistry);

					return meterRegistry;
				}

				@Override
				public void modifiedService(
					ServiceReference<MeterRegistryProvider> reference,
					MeterRegistry service) {

					removedService(reference, service);

					addingService(reference);

				}

				@Override
				public void removedService(
					ServiceReference<MeterRegistryProvider> reference,
					MeterRegistry service) {

					Metrics.removeRegistry(service);

					context.ungetService(reference);

				}
			});

		_serviceTracker.open();

	}

	@Deactivate
	public void deactivate() {
		_serviceTracker.close();
	}

	private ServiceTracker<MeterRegistryProvider, MeterRegistry>
		_serviceTracker;

}
