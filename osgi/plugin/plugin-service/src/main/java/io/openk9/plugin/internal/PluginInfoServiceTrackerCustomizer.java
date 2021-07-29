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

import io.netty.handler.codec.http.HttpMethod;
import io.openk9.common.api.constant.Strings;
import io.openk9.http.osgi.BaseStaticEndpoint;
import io.openk9.http.web.RouterHandler;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.plugin.api.PluginInfo;
import io.openk9.reactor.netty.util.HttpPrefixPredicate;
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

		String path = "/plugins/" + pluginInfo.getPluginId() + "/static";

		RouterHandler routerHandler = router ->
			router
				.route(
					HttpPrefixPredicate.of(
						path, HttpMethod.GET),
					BaseStaticEndpoint.of(
						reference.getBundle(), path, Strings.BLANK, "")
				);

		ServiceRegistration<RouterHandler> serviceRegistration =
			_bundleContext.registerService(
				RouterHandler.class,
				routerHandler,
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
