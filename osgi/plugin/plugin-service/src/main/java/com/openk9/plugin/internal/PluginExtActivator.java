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

package com.openk9.plugin.internal;

import com.openk9.osgi.util.AutoCloseables;
import com.openk9.plugin.api.PluginInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.ServiceTracker;

@Component(
	immediate = true,
	service = PluginExtActivator.class
)
public class PluginExtActivator {

	@Activate
	void activate(BundleContext bundleContext) {

		BundleTracker<Void> pluginBundleTracker = new BundleTracker<>(
			bundleContext,
			Bundle.ACTIVE | Bundle.STOPPING,
			new PluginBundleTrackerCustomizer());

		pluginBundleTracker.open();

		ServiceTracker<PluginInfo, AutoCloseables.AutoCloseableSafe>
			pluginInfoServiceTracker = new ServiceTracker<>(
				bundleContext, PluginInfo.class,
				new PluginInfoServiceTrackerCustomizer(bundleContext));

		pluginInfoServiceTracker.open();

		_closeable = AutoCloseables.mergeAutoCloseableToSafe(
			pluginInfoServiceTracker::close,
			pluginBundleTracker::close
		);

	}

	@Deactivate
	void deactivate() {
		_closeable.close();
	}

	private AutoCloseables.AutoCloseableSafe _closeable;


}
