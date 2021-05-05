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

package io.openk9.plugin.driver.manager.service;

import io.openk9.osgi.util.AutoCloseables;
import io.openk9.plugin.driver.manager.api.DocumentTypeFactory;
import io.openk9.plugin.driver.manager.api.DocumentTypeFactoryRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

@Component(
	immediate = true,
	service = DocumentTypeFactoryRegistry.class
)
public class DocumentTypeFactoryRegistryImpl
	implements DocumentTypeFactoryRegistry {

	@Activate
	public void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	@Override
	public AutoCloseables.AutoCloseableSafe register(
		DocumentTypeFactory.DefaultDocumentTypeFactory defaultDocumentTypeFactory) {
		return _register(defaultDocumentTypeFactory);
	}

	@Override
	public AutoCloseables.AutoCloseableSafe register(
		DocumentTypeFactory.DefaultDocumentTypeFactory...defaultDocumentTypeFactory) {

		return _register(Arrays.asList(defaultDocumentTypeFactory));
	}

	@Override
	public AutoCloseables.AutoCloseableSafe register(
		Iterable<DocumentTypeFactory.DefaultDocumentTypeFactory> baseDocumentTypeFactory) {

		return _register(baseDocumentTypeFactory);
	}

	private AutoCloseables.AutoCloseableSafe _register(
		DocumentTypeFactory.DefaultDocumentTypeFactory documentTypeFactory) {

		ServiceRegistration<DocumentTypeFactory> serviceRegistration =
			_bundleContext.registerService(
				DocumentTypeFactory.class,
				documentTypeFactory,
				new Hashtable<>(
					Map.of(
						DocumentTypeFactory.DEFAULT,
						documentTypeFactory.isDefault(),
						DocumentTypeFactory.PLUGIN_DRIVER_NAME,
						documentTypeFactory.getPluginDriverName()
					)
				)
			);

		return AutoCloseables.mergeAutoCloseableToSafe(
			serviceRegistration::unregister);

	}

	private AutoCloseables.AutoCloseableSafe _register(
		Iterable<DocumentTypeFactory.DefaultDocumentTypeFactory> args) {

		AutoCloseables.AutoCloseableSafe autoCloseableSafe = () -> {};

		for (DocumentTypeFactory.DefaultDocumentTypeFactory dtf : args) {
			autoCloseableSafe = autoCloseableSafe.andThen(_register(dtf));
		}

		return autoCloseableSafe;

	}

	private BundleContext _bundleContext;

}
