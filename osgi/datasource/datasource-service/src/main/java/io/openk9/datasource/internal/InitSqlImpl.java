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

package io.openk9.datasource.internal;

import io.openk9.sql.api.InitSql;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Hashtable;
import java.util.Map;

@Component(
	immediate = true,
	service = InitSql.class
)
public class InitSqlImpl implements InitSql {

	@Activate
	void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	void deactivate() {
		if (_serviceRegistration != null) {
			_serviceRegistration.unregister();
			_serviceRegistration = null;
		}
		_bundleContext = null;
	}

	public String initSqlFile() {
		return "init.sql";
	}

	@Override
	public void onAfterCreate() {
		_serviceRegistration = _bundleContext.registerService(
			InitSql.Executed.class,
			Executed.INSTANCE, new Hashtable<>(
				Map.of(
					"init-sql", "io.openk9.datasource.internal.InitSqlImpl"
				)
			)
		);
	}

	private BundleContext _bundleContext;

	private ServiceRegistration _serviceRegistration;

}