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

package com.openk9.sql.internal.client.update;

import io.r2dbc.spi.ConnectionFactory;
import com.openk9.sql.api.client.DatabaseClient;

import java.util.Map;

public class DefaultUpdateFromSpec implements DatabaseClient.UpdateFromSpec {

	public DefaultUpdateFromSpec(ConnectionFactory connectionFactory) {
		_connectionFactory = connectionFactory;
	}

	@Override
	public DatabaseClient.GenericUpdateSpec<Map<String, Object>> from(
		String table) {

		return new DefaultGenericUpdateSpec<>(_connectionFactory, table);
	}

	private final ConnectionFactory _connectionFactory;
}
