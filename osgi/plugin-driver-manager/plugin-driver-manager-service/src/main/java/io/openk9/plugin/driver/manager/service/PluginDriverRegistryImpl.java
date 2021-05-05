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

import io.openk9.plugin.driver.manager.api.PluginDriver;
import io.openk9.plugin.driver.manager.api.PluginDriverRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component(immediate = true, service = PluginDriverRegistry.class)
public class PluginDriverRegistryImpl implements PluginDriverRegistry {

	@Activate
	public void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_serviceTracker = new ServiceTracker<>(
			_bundleContext, PluginDriver.class,
			new PluginDriverServiceTrackerCustomizer());

		_serviceTracker.open();

	}

	@Deactivate
	public void deactivate() {
		_serviceTracker.close();
		_bundleContext = null;
	}

	@Override
	public Optional<PluginDriver> getPluginDriver(String name) {
		return Optional.ofNullable(_pluginDriverMap.get(name));
	}

	@Override
	public Collection<PluginDriver> getPluginDriverList(
		Iterable<String> names) {

		List<PluginDriver> result = new ArrayList<>();

		for (String name : names) {
			PluginDriver pluginDriver = _pluginDriverMap.get(name);
			if (pluginDriver != null) {
				result.add(pluginDriver);
			}
		}

		return result;


	}

	@Override
	public Collection<PluginDriver> getPluginDriverList() {
		return new ArrayList<>(_pluginDriverMap.values());
	}

	class PluginDriverServiceTrackerCustomizer implements
		ServiceTrackerCustomizer<PluginDriver, PluginDriver> {

		@Override
		public PluginDriver addingService(
			ServiceReference<PluginDriver> reference) {

			PluginDriver service = PluginDriverRegistryImpl
				.this._bundleContext.getService(reference);

			_pluginDriverMap.put(service.getClass().getName(), service);

			return service;
		}

		@Override
		public void modifiedService(
			ServiceReference<PluginDriver> reference, PluginDriver service) {

			removedService(reference, service);

			addingService(reference);

		}

		@Override
		public void removedService(
			ServiceReference<PluginDriver> reference, PluginDriver service) {

			PluginDriverRegistryImpl
				.this._bundleContext.ungetService(reference);

			_pluginDriverMap.remove(service.getClass().getName());

		}
	}

	private ServiceTracker<PluginDriver, PluginDriver> _serviceTracker;

	private BundleContext _bundleContext;

	private final Map<String, PluginDriver> _pluginDriverMap =
		new HashMap<>();



}
