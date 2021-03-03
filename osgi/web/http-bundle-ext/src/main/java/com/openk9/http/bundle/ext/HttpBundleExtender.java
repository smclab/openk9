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

package com.openk9.http.bundle.ext;

import com.openk9.http.osgi.BaseStaticEndpoint;
import com.openk9.http.web.Endpoint;
import com.openk9.http.web.HttpHandler;
import com.openk9.osgi.util.AutoCloseables;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.util.Dictionary;

public class HttpBundleExtender implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {

		_bundleTracker = new BundleTracker<>(
			context, Bundle.ACTIVE | Bundle.STOPPING,
			new BundleTrackerCustomizer<AutoCloseables.AutoCloseableSafe>() {
				@Override
				public AutoCloseables.AutoCloseableSafe addingBundle(
					Bundle bundle, BundleEvent event) {

					if (bundle.getState() != Bundle.ACTIVE) {
						removedBundle(bundle, event, NOTHING);
						return null;
					}

					Dictionary<String, String> headers =
						bundle.getHeaders(null);

					String contextPath =
						headers.get(REACTIVE_WEB_CONTEXT_PATH);

					if (contextPath == null) {
						removedBundle(bundle, event, NOTHING);
						return null;
					}

					String staticFolder =
						headers.get(REACTIVE_WEB_CONTEXT_PATH_FOLDER);

					String staticFolderVar;

					if (staticFolder == null) {
						staticFolderVar = "";
					}
					else {
						staticFolderVar = staticFolder;
					}

					HttpHandler httpHandler = new BaseStaticEndpoint() {
						{
							_bundle = bundle;
						}

						@Override
						public String getStaticFolder() {
							return staticFolderVar;
						}

						@Override
						public String getPath() {
							return contextPath;
						}

						@Override
						public boolean prefix() {
							return true;
						}

						@Override
						public int method() {
							return HttpHandler.GET;
						}

					};

					BundleContext bundleContext = bundle.getBundleContext();

					ServiceRegistration<Endpoint> serviceRegistration =
						bundleContext.registerService(
							Endpoint.class, httpHandler, null);

					return AutoCloseables.mergeAutoCloseableToSafe(
						serviceRegistration::unregister);
				}

				@Override
				public void modifiedBundle(
					Bundle bundle, BundleEvent event,
					AutoCloseables.AutoCloseableSafe object) {

					removedBundle(bundle, event, object);

					addingBundle(bundle, event);

				}

				@Override
				public void removedBundle(
					Bundle bundle, BundleEvent event,
					AutoCloseables.AutoCloseableSafe object) {

					object.close();

				}
			}
		);

		_bundleTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		_bundleTracker.close();
	}

	private BundleTracker<AutoCloseables.AutoCloseableSafe> _bundleTracker;

	public static final AutoCloseables.AutoCloseableSafe NOTHING = () -> {};

	private static final String REACTIVE_WEB_CONTEXT_PATH =
		"Reactive-Web-ContextPath";

	private static final String REACTIVE_WEB_CONTEXT_PATH_FOLDER =
		"Reactive-Web-ContextPath-Folder";

}
