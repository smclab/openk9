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

package io.openk9.vertx.internal.consul;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.ConsulClientOptions;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.util.Map;
import java.util.Objects;

@Component(
	immediate = true,
	service = VertxConsulActivator.class,
	property = {
		"host=consul",
		"port:Integer=8500",
		"scan-period:Integer=2000"
	}
)
public class VertxConsulActivator {

	@Activate
	void activate(Map<String, Object> config, BundleContext context) {

		prevConfig = config;

		ConsulClientOptions opts =
			new ConsulClientOptions(JsonObject.mapFrom(config));

		_consulClient = ConsulClient.create(_vertx, opts);

		_serviceRegistration =
			context.registerService(
				ConsulClient.class, _consulClient, null);

	}

	@Modified
	void modified(Map<String, Object> config, BundleContext bundleContext) {
		if (!Objects.equals(prevConfig, config)) {
			deactivate();
			activate(config, bundleContext);
		}
	}

	@Deactivate
	void deactivate() {
		try {
			_consulClient.close();
		}
		catch (Exception e) {
			// ignore
		}
		_consulClient = null;
		_serviceRegistration.unregister();
		_serviceRegistration = null;
	}

	private ConsulClient _consulClient;

	private ServiceRegistration _serviceRegistration;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private Vertx _vertx;

	private transient Map<String, Object> prevConfig = null;

}
