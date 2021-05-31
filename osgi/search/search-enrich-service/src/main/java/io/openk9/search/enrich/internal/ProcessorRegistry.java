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

package io.openk9.search.enrich.internal;

import io.openk9.ingestion.api.Binding;
import io.openk9.search.enrich.api.EnrichProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.util.tracker.ServiceTracker;

@Component(
	immediate = true,
	service = ProcessorRegistry.class
)
public class ProcessorRegistry {

	@interface Config {
		String exchangeName() default "openk9.erich.direct";
		String exchangeType() default "direct";
		int prefetch() default 250;
	}

	@Activate
	@Modified
	public void activate(BundleContext bundleContext, Config config) {

		Binding.Exchange exchange = Binding.Exchange.of(
			config.exchangeName(), Binding.Exchange.Type
				.valueOf(config.exchangeType())
		);

		_serviceTracker = new ServiceTracker<>(
			bundleContext, EnrichProcessor.class,
			new EnrichProcessorServiceTracker(
				config.prefetch(), exchange, bundleContext)
		);

		_serviceTracker.open();

	}

	@Deactivate
	public void deactivate() {
		_serviceTracker.close();
	}

	private ServiceTracker<EnrichProcessor, EnrichProcessor> _serviceTracker;

}
