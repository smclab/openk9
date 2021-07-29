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

package io.openk9.repository.http.internal;

import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.repository.http.api.RepositoryHttpExtender;
import io.openk9.repository.http.internal.util.ReactiveRepositoryHttpHandler;
import io.openk9.sql.api.entity.ReactiveRepository;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@Component(
	immediate = true,
	service = RepositoryHttpExtenderImpl.class
)
public class RepositoryHttpExtenderImpl {

	@interface Config {
		String endpointPath() default "/v2";
	}

	@Activate
	public void activate(BundleContext bundleContext, Config config) {

		_bundleContext = bundleContext;

		_config = config;

		_serviceTracker = new ServiceTracker<>(
			_bundleContext, RepositoryHttpExtender.class,
			new RepositoryHttpExtenderServiceTrackerCustomizer());

		_serviceTracker.open();

	}

	@Modified
	public void modified(BundleContext bundleContext, Config config) {
		deactivate();
		activate(bundleContext, config);
	}

	@Deactivate
	public void deactivate() {
		_serviceTracker.close();
		_bundleContext = null;
	}

	private class RepositoryHttpExtenderServiceTrackerCustomizer
		implements ServiceTrackerCustomizer<RepositoryHttpExtender, ServiceRegistration<RouterHandler>> {

		@Override
		public ServiceRegistration<RouterHandler> addingService(
			ServiceReference<RepositoryHttpExtender> reference) {

			BundleContext bundleContext =
				RepositoryHttpExtenderImpl.this._bundleContext;

			Config config = RepositoryHttpExtenderImpl.this._config;

			String prefixPath = config.endpointPath();

			RepositoryHttpExtender repositoryHttpExtender =
				bundleContext.getService(reference);

			ReactiveRepository<Object, Object> reactiveRepository =
				repositoryHttpExtender.getReactiveRepository();

			Class<?> entityClass = reactiveRepository.entityClass();

			String simpleName = entityClass.getSimpleName();

			String endpointName = Character.toLowerCase(
				simpleName.charAt(0)) + simpleName.substring(1);

			ReactiveRepositoryHttpHandler reactiveRepositoryHttpHandler =
				new ReactiveRepositoryHttpHandler(
					prefixPath + "/" + endpointName,
					reactiveRepository,
					_jsonFactory,
					_httpResponseWriter
				);

			return _bundleContext.registerService(
				RouterHandler.class, reactiveRepositoryHttpHandler, null);
		}

		@Override
		public void modifiedService(
			ServiceReference<RepositoryHttpExtender> reference,
			ServiceRegistration<RouterHandler> autoCloseableSafe) {

			removedService(reference, autoCloseableSafe);

			addingService(reference);

		}

		@Override
		public void removedService(
			ServiceReference<RepositoryHttpExtender> reference,
			ServiceRegistration<RouterHandler> autoCloseableSafe) {

			BundleContext bundleContext =
				RepositoryHttpExtenderImpl.this._bundleContext;

			bundleContext.ungetService(reference);

			autoCloseableSafe.unregister();


		}

	}

	private BundleContext _bundleContext;

	private Config _config;

	private ServiceTracker<
		RepositoryHttpExtender, ServiceRegistration<RouterHandler>>
			_serviceTracker;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference(
		target = "(type=json)",
		policyOption = ReferencePolicyOption.GREEDY
	)
	private HttpResponseWriter _httpResponseWriter;

}
