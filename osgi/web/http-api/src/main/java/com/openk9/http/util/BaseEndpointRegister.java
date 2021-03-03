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

package com.openk9.http.util;

import com.openk9.http.web.Endpoint;
import com.openk9.osgi.util.AutoCloseables;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class BaseEndpointRegister
	implements AutoCloseables.AutoCloseableSafe {

	protected BaseEndpointRegister registerEndpoint(Endpoint endpoint) {

		String basePath = getBasePath();

		Map<String, Object> props = null;

		if (basePath != null && !basePath.isEmpty()) {
			props = Collections.singletonMap("base.path", basePath);
		}

		return registerEndpoint(endpoint, props);
	}

	protected BaseEndpointRegister registerEndpoint(Endpoint...endpoints) {
		for (Endpoint endpoint : endpoints) {
			registerEndpoint(endpoint);
		}
		return this;
	}

	protected BaseEndpointRegister registerEndpoint(
		Endpoint endpoint, Map<String, Object> props) {

		Dictionary<String, Object> propsDictionary = null;

		if (props != null) {
			propsDictionary = new Hashtable<>(props);
		}

		_serviceRegistrations.add(
			_bundleContext.registerService(
				Endpoint.class, endpoint, propsDictionary));

		return this;
	}

	@Override
	public void close() {
		Iterator<ServiceRegistration> iterator =
			_serviceRegistrations.iterator();

		while (iterator.hasNext()) {
			iterator.next().unregister();
			iterator.remove();
		}

	}

	public abstract String getBasePath();

	protected void setBundleContext(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	private BundleContext _bundleContext;

	private List<ServiceRegistration> _serviceRegistrations =
		new ArrayList<>();

}
