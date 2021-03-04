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

package com.openk9.ingestion.rabbitmq.bind;

import com.openk9.ingestion.api.BundleSender;
import com.openk9.ingestion.api.BundleSenderProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Component(
	immediate = true,
	service = BundleSenderProvider.class
)
public class BundleSenderProviderImpl implements BundleSenderProvider {

	@Activate
	public void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	@Override
	public BundleSender getBundleSender(String routingKey) {

		try {

			String filter = "(routingKey=" + routingKey + ")";

			Collection<ServiceReference<BundleSender>> serviceReferences =
				_bundleContext.getServiceReferences(
					BundleSender.class, filter);

			ServiceReference<BundleSender> serviceReference =
				serviceReferences
					.stream()
					.findFirst()
					.orElse(null);

			if (serviceReference == null) {

				_log.warn(
					"service with filter: " +
					filter + " not found return null");

				return null;
			}

			try {
				return _bundleContext.getService(serviceReference);
			}
			finally {
				_bundleContext.ungetService(serviceReference);
			}

		}
		catch (InvalidSyntaxException e) {
			_log.warn(e.getMessage(), e);
		}

		return null;
	}

	private BundleContext _bundleContext;

	private static final Logger _log = LoggerFactory.getLogger(
		BundleSenderProviderImpl.class.getName());

}
