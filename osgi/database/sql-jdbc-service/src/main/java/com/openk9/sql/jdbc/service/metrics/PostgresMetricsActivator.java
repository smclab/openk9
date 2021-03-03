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

package com.openk9.sql.jdbc.service.metrics;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.db.PostgreSQLDatabaseMetrics;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;

@Component(
	immediate = true,
	service = PostgresMetricsActivator.class
)
public class PostgresMetricsActivator {

	@Activate
	public void activate() {
		new PostgreSQLDatabaseMetrics(_dataSource, "openk9")
			.bindTo(Metrics.globalRegistry);
	}

	@Reference
	private DataSource _dataSource;

}
