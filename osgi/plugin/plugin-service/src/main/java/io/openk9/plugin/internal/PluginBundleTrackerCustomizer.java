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

import io.openk9.osgi.util.AutoCloseables;
import io.openk9.plugin.api.BundleInfo;
import io.openk9.plugin.api.PluginInfo;
import io.openk9.plugin.api.Constants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginBundleTrackerCustomizer
	implements BundleTrackerCustomizer<Void> {

	@Override
	public Void addingBundle(Bundle bundle, BundleEvent event) {

		if (bundle.getState() != Bundle.ACTIVE) {
			removedBundle(bundle, event, null);
			return null;
		}

		Dictionary<String, String> headers = bundle.getHeaders(null);

		if (headers.get(Constants.OPENK9_PLUGIN) == null) {
			return null;
		}
		String pluginWebId = headers.get(Constants.OPENK9_PLUGIN_WEB_ID);

		if (pluginWebId == null) {
			pluginWebId = bundle.getSymbolicName();
		}

		PluginInfo pluginInfo = PluginInfo.DefaultPluginInfo.of(
			pluginWebId,
			BundleInfo
				.builder()
				.id(bundle.getBundleId())
				.lastModified(bundle.getLastModified())
				.symbolicName(bundle.getSymbolicName())
				.version(bundle.getVersion().toString())
				.state(_STATE_MAP.get(bundle.getState()))
				.build()
		);

		ServiceRegistration<PluginInfo> pluginInfoSR =
			bundle
				.getBundleContext()
				.registerService(PluginInfo.class, pluginInfo, null);

		_registrationMap.put(
			bundle, AutoCloseables
				.mergeAutoCloseableToSafe(pluginInfoSR::unregister));

		return null;
	}

	@Override
	public void modifiedBundle(
		Bundle bundle, BundleEvent event, Void nothing) {

		removedBundle(bundle, event, null);

		addingBundle(bundle, event);

	}

	@Override
	public void removedBundle(
		Bundle bundle, BundleEvent event, Void nothing) {

		AutoCloseables.AutoCloseableSafe autoCloseableSafe =
			_registrationMap.remove(bundle);

		if (autoCloseableSafe != null) {
			autoCloseableSafe.close();
		}

	}

	private static final Map<Integer, String> _STATE_MAP = Map.of(
		Bundle.UNINSTALLED, "UNINSTALLED",
		Bundle.INSTALLED, "INSTALLED",
		Bundle.RESOLVED, "RESOLVED",
		Bundle.STARTING, "STARTING",
		Bundle.STOPPING, "STOPPING",
		Bundle.ACTIVE, "ACTIVE"
	);

	private final Map<Bundle, AutoCloseables.AutoCloseableSafe> _registrationMap =
		new ConcurrentHashMap<>();

}
