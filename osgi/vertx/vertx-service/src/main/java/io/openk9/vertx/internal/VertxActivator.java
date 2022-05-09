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

package io.openk9.vertx.internal;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	service = VertxActivator.class,
	property = {
		"consul.config.disabled=true"
	}
)
public class VertxActivator {

	@Activate
	void activate(BundleContext bundleContext, Map<String, Object> config)
		throws IOException {

		_vertx = Vertx.vertx(new VertxOptions(JsonObject.mapFrom(config)));

		_serviceRegistrations.add(
			bundleContext.registerService(
				Vertx.class, _vertx, null));

		_serviceRegistrations.add(
			bundleContext.registerService(
				EventBus.class, _vertx.eventBus(), null));

		_serviceRegistrations.add(
			bundleContext.registerService(
				FileSystem.class, _vertx.fileSystem(), null));

	}

	@Modified
	void modified(BundleContext bundleContext, Map<String, Object> config)
		throws IOException{

		deactivate();
		activate(bundleContext, config);

	}

	@Deactivate
	void deactivate() {

		_vertx.close().toCompletionStage().toCompletableFuture().join();

		Iterator<ServiceRegistration> iterator =
			_serviceRegistrations.iterator();

		while (iterator.hasNext()) {
			ServiceRegistration sr = iterator.next();
			sr.unregister();
			iterator.remove();
		}
	}

	private Vertx _vertx;

	private final List<ServiceRegistration> _serviceRegistrations =
		new ArrayList<>();

	private static final Logger _log = LoggerFactory.getLogger(
		VertxActivator.class);

}
