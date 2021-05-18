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

package io.openk9.sql.internal.client;

import io.openk9.sql.api.client.DatabaseClient;
import io.openk9.sql.internal.client.delete.DefaultDeleteFromSpec;
import io.openk9.sql.internal.client.insert.DefaultInsertFromSpec;
import io.openk9.sql.internal.client.select.DefaultSelectFromSpec;
import io.openk9.sql.internal.client.update.DefaultUpdateFromSpec;
import io.r2dbc.spi.ConnectionFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = DatabaseClient.class)
public class DatabaseClientImpl implements DatabaseClient {

	@Override
	public SelectFromSpec select() {
		return new DefaultSelectFromSpec(_connectionFactory);
	}

	@Override
	public InsertFromSpec insert() {
		return new DefaultInsertFromSpec(_connectionFactory);
	}

	@Override
	public DeleteFromSpec delete() {
		return new DefaultDeleteFromSpec(_connectionFactory);
	}

	@Override
	public UpdateFromSpec update() {
		return new DefaultUpdateFromSpec(_connectionFactory);
	}

	@Reference
	private ConnectionFactory _connectionFactory;

}
