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

package io.openk9.ingestion.driver.manager.internal;

import io.openk9.ingestion.driver.manager.api.DocumentTypeFactoryRegistry;
import io.openk9.ingestion.driver.manager.api.DocumentTypeFactoryRegistryAware;
import io.openk9.osgi.util.AutoCloseables;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@Component(
	immediate = true,
	service = DocumentTypeFactoryRegistryAwareInterceptor.class
)
public class DocumentTypeFactoryRegistryAwareInterceptor {

	@Activate
	public void activate(BundleContext bundleContext) {

		_bundleContext = bundleContext;

		_serviceTracker = new ServiceTracker<>(
			_bundleContext, DocumentTypeFactoryRegistryAware.class,
			new DocumentTypeFactoryRegistryAwareServiceCustomizer()
		);

		_serviceTracker.open();
	}

	@Deactivate
	public void deactivate() {
		_serviceTracker.close();
		_serviceTracker = null;
		_bundleContext = null;
	}

	class DocumentTypeFactoryRegistryAwareServiceCustomizer
		implements ServiceTrackerCustomizer<
			DocumentTypeFactoryRegistryAware,
			AutoCloseables.AutoCloseableSafe> {


		@Override
		public AutoCloseables.AutoCloseableSafe addingService(
			ServiceReference<DocumentTypeFactoryRegistryAware> reference) {

			BundleContext bundleContext =
				DocumentTypeFactoryRegistryAwareInterceptor
					.this._bundleContext;

			DocumentTypeFactoryRegistryAware service =
				bundleContext.getService(reference);

			return service.apply(_documentTypeFactoryRegistry);
		}

		@Override
		public void modifiedService(
			ServiceReference<DocumentTypeFactoryRegistryAware> reference,
			AutoCloseables.AutoCloseableSafe closeableSafe) {

			removedService(reference, closeableSafe);

			addingService(reference);

		}

		@Override
		public void removedService(
			ServiceReference<DocumentTypeFactoryRegistryAware> reference,
			AutoCloseables.AutoCloseableSafe closeableSafe) {

			BundleContext bundleContext =
				DocumentTypeFactoryRegistryAwareInterceptor
					.this._bundleContext;

			bundleContext.ungetService(reference);

			closeableSafe.close();

		}

	}

	private BundleContext _bundleContext;

	private ServiceTracker<
		DocumentTypeFactoryRegistryAware, AutoCloseables.AutoCloseableSafe>
			_serviceTracker;

	@Reference
	private DocumentTypeFactoryRegistry _documentTypeFactoryRegistry;

}
