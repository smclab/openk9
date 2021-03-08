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

import io.openk9.common.api.constant.Strings;
import io.openk9.http.osgi.BaseStaticEndpoint;
import io.openk9.http.web.Endpoint;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.plugin.api.PluginInfo;
import lombok.RequiredArgsConstructor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@RequiredArgsConstructor
public class PluginInfoServiceTrackerCustomizer
	implements ServiceTrackerCustomizer<PluginInfo, AutoCloseables.AutoCloseableSafe> {

	@Override
	public AutoCloseables.AutoCloseableSafe addingService(
		ServiceReference<PluginInfo> reference) {

		PluginInfo pluginInfo = _bundleContext.getService(reference);

		BaseStaticEndpoint baseStaticEndpoint = new BaseStaticEndpoint() {

			{
				_bundle = reference.getBundle();
			}

			@Override
			public String getStaticFolder() {
				return Strings.BLANK;
			}

			@Override
			public String getPath() {
				return _path;
			}

			@Override
			public int method() {
				return GET;
			}

			@Override
			public boolean prefix() {
				return true;
			}

			private final String _path =
				"/plugins/" + pluginInfo.getPluginId() + "/static";

		};

		ServiceRegistration<Endpoint> serviceRegistration =
			_bundleContext.registerService(
				Endpoint.class,
				baseStaticEndpoint,
				null
			);

		return AutoCloseables.mergeAutoCloseableToSafe(
			serviceRegistration::unregister);
	}

	@Override
	public void modifiedService(
		ServiceReference<PluginInfo> reference,
		AutoCloseables.AutoCloseableSafe service) {

		removedService(reference, service);

		addingService(reference);

	}

	@Override
	public void removedService(
		ServiceReference<PluginInfo> reference,
		AutoCloseables.AutoCloseableSafe service) {

		_bundleContext.ungetService(reference);

		service.close();

	}

	private final BundleContext _bundleContext;

}
