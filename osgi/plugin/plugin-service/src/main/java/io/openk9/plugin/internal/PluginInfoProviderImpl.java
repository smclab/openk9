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

package io.openk9.plugin.internal;

import io.openk9.plugin.api.PluginInfo;
import io.openk9.plugin.api.PluginInfoProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component(
	immediate = true,
	service = PluginInfoProvider.class
)
public class PluginInfoProviderImpl implements PluginInfoProvider {

	@Activate
	void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	@Deactivate
	void deactivate() {
		_bundleContext = null;
	}

	@Override
	public Collection<PluginInfo> getPluginInfoList() {
		return _getServices(null);
	}

	@Override
	public Collection<PluginInfo> getPluginInfoList(Filter filter) {
		return _getServices(filter.toString());
	}

	@Override
	public Collection<PluginInfo> getPluginInfoList(String filter) {
		return _getServices(filter);
	}

	@Override
	public Optional<PluginInfo> getPluginInfoByPluginId(String pluginId) {
		return _getServices(null)
			.stream()
			.filter(pi -> pi.getPluginId().equals(pluginId))
			.findFirst();
	}

	@Override
	public Optional<PluginInfo> getPluginInfoByBundleId(long bundleId) {
		return _getServices(null)
			.stream()
			.filter(pi -> pi.getBundleInfo().getId() == bundleId)
			.findFirst();
	}

	@Override
	public Optional<PluginInfo> getPluginInfoByBSN(String bundleSymbolicName) {
		return _getServices(null)
			.stream()
			.filter(pi -> pi.getBundleInfo().getSymbolicName().equals(bundleSymbolicName))
			.findFirst();
	}

	private Collection<PluginInfo> _getServices(String filter) {

		try {

			Collection<ServiceReference<PluginInfo>> serviceReferences;

			if (filter == null) {
				serviceReferences = _bundleContext.getServiceReferences(
					PluginInfo.class, null);
			}
			else {
				serviceReferences = _bundleContext.getServiceReferences(
					PluginInfo.class, filter);
			}

			if (!serviceReferences.isEmpty()) {

				Collection<PluginInfo> pluginInfoList =
					new ArrayList<>(serviceReferences.size());

				for (ServiceReference<PluginInfo> serviceReference : serviceReferences) {
					try {
						pluginInfoList.add(
							_bundleContext.getService(serviceReference));
					}
					finally {
						_bundleContext.ungetService(serviceReference);
					}
				}

				return pluginInfoList;
			}

		}
		catch (InvalidSyntaxException ise) {
			_log.error(ise.getMessage(), ise);
		}

		return List.of();

	}

	private BundleContext _bundleContext;

	private static final Logger _log = LoggerFactory.getLogger(
		PluginInfoProviderImpl.class
	);

}
