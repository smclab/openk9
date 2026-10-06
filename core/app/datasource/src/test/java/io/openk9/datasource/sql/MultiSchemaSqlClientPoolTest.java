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

package io.openk9.datasource.sql;

import io.quarkus.hibernate.reactive.runtime.customized.MultiSchemaSqlClientPool;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A caller that gives up on a tenant connection, while it waits in the pool
 * queue or while the tenant schema is being set, must not keep that
 * connection taken: it goes back to the pool as soon as it arrives.
 */
class MultiSchemaSqlClientPoolTest {

	private static final String TENANT_ID = "tenant_a";

	private final Promise<SqlConnection> acquisition = Promise.promise();
	private final SqlConnection sqlConnection = mock(SqlConnection.class);
	private MultiSchemaSqlClientPool pool;

	@BeforeEach
	void setUp() {
		// a Vert.x pool that hands over the connection only when the test says so
		var vertxPool = mock(Pool.class);
		when(vertxPool.getConnection()).thenReturn(acquisition.future());
		when(sqlConnection.close()).thenReturn(Future.succeededFuture());

		var jdbcServices = mock(JdbcServices.class);
		when(jdbcServices.getSqlExceptionHelper())
			.thenReturn(new SqlExceptionHelper(false));
		var serviceRegistry = mock(ServiceRegistryImplementor.class);
		when(serviceRegistry.getService(JdbcServices.class)).thenReturn(jdbcServices);
		when(serviceRegistry.getService(SqlStatementLogger.class))
			.thenReturn(mock(SqlStatementLogger.class));

		pool = new MultiSchemaSqlClientPool(vertxPool);
		pool.injectServices(serviceRegistry);
	}

	@Test
	@DisplayName("Should close a connection that arrives after the caller gave up waiting")
	void should_close_a_connection_that_arrives_after_cancellation() {
		// the caller gives up while its request waits in the pool queue
		pool.getConnection(TENANT_ID).toCompletableFuture().cancel(false);

		// then the pool hands over a connection
		acquisition.complete(sqlConnection);

		verify(sqlConnection).close();
	}

	@Test
	@DisplayName("Should close a connection whose caller gave up while the schema was being set")
	void should_close_a_connection_cancelled_while_setting_the_schema() {
		// the connection arrives and the tenant schema is still being set
		PreparedQuery<RowSet<Row>> setSchema = mock();
		when(setSchema.execute()).thenReturn(Promise.<RowSet<Row>>promise().future());
		when(sqlConnection.preparedQuery(anyString())).thenReturn(setSchema);
		var request = pool.getConnection(TENANT_ID).toCompletableFuture();
		acquisition.complete(sqlConnection);
		verify(sqlConnection).preparedQuery("SET SESSION SCHEMA '" + TENANT_ID + "'");

		// then the caller gives up
		request.cancel(false);

		verify(sqlConnection).close();
	}

}
