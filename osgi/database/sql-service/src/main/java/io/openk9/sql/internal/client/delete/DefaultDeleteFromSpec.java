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

package io.openk9.sql.internal.client.delete;

import io.r2dbc.spi.ConnectionFactory;
import io.openk9.sql.api.client.DatabaseClient;

public class DefaultDeleteFromSpec implements DatabaseClient.DeleteFromSpec {

	public DefaultDeleteFromSpec(ConnectionFactory connectionFactory) {
		_connectionFactory = connectionFactory;
	}

	@Override
	public DatabaseClient.DeleteMatchingSpec from(String table) {
		return new DefaultDeleteMatchingSpec(_connectionFactory, table);
	}

	private final ConnectionFactory _connectionFactory;
}
