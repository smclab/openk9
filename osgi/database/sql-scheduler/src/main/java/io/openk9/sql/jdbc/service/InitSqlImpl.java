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

package io.openk9.sql.jdbc.service;

import io.openk9.sql.api.InitSql;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.system.SystemService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.Exceptions;

@Component(
	immediate = true,
	service = InitSql.class
)
public class InitSqlImpl implements InitSql {
	@Override
	public String initSqlFile() {
		return "tables_postgres.sql";
	}

	@Override
	public void onAfterCreate() {
		try {
			_featuresService.installFeature("scheduler", _systemService.getVersion());
		}
		catch (Exception exception) {
			throw Exceptions.bubble(exception);
		}
	}

	@Reference
	private FeaturesService _featuresService;

	@Reference
	private SystemService _systemService;

}
