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

package io.openk9.bundle.installer.via.config;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Component(
	immediate = true,
	service = BundleInstallerConfigurationListener.class
)
public class BundleInstallerConfigurationListener {

	@Activate
	void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
		_serviceTracker = new ServiceTracker<>(
			bundleContext, BundleInstallerConfiguration.class,
			new BundleInstallerConfigurationServiceTrackerCustomizer());
		_serviceTracker.open();
	}

	@Deactivate
	void deactivate() {
		_serviceTracker.close();
	}

	private ServiceTracker<BundleInstallerConfiguration, BundleInstallerConfiguration>
		_serviceTracker;

	private BundleContext _bundleContext;

	public class BundleInstallerConfigurationServiceTrackerCustomizer implements
		ServiceTrackerCustomizer<BundleInstallerConfiguration, BundleInstallerConfiguration> {

		@Override
		public BundleInstallerConfiguration addingService(
			ServiceReference<BundleInstallerConfiguration> reference) {

			BundleInstallerConfiguration bic =
				_bundleContext.getService(reference);

			String bundleSymbolicName = bic.bundleSymbolicName();
			String bundleBase64 = bic.bundleBase64();

			Optional<Bundle> first =
				findBundle(bundleSymbolicName);

			try {

				byte[] bundleBytes = Base64.getDecoder().decode(bundleBase64);

				ByteArrayInputStream byteArrayInputStream =
					new ByteArrayInputStream(bundleBytes);

				if (first.isPresent()) {
					Bundle bundle = first.get();

					bundle.update(byteArrayInputStream);

				}
				else {

					Bundle bundle = _bundleContext.installBundle(
						bundleSymbolicName,
						byteArrayInputStream);

					bundle.start();
				}
			}
			catch (Exception e) {
				_log.error(e.getMessage(), e);
			}

			return bic;
		}

		@Override
		public void modifiedService(
			ServiceReference<BundleInstallerConfiguration> reference,
			BundleInstallerConfiguration service) {

			removedService(reference, service);

			addingService(reference);

		}

		@Override
		public void removedService(
			ServiceReference<BundleInstallerConfiguration> reference,
			BundleInstallerConfiguration service) {

			Optional<Bundle> bundle = findBundle(service.bundleSymbolicName());

			if (bundle.isPresent()) {
				try {
					bundle.get().uninstall();
				}
				catch (Exception e) {
					_log.error(e.getMessage(), e);
				}
			}

		}

		private Optional<Bundle> findBundle(String bundleSymbolicName) {
			return Arrays
				.stream(_bundleContext.getBundles())
				.filter(bundle -> bundle
					.getSymbolicName()
					.equals(bundleSymbolicName))
				.findFirst();
		}



	}

	private static final Logger _log = LoggerFactory.getLogger(
		BundleInstallerConfigurationListener.class);

}
