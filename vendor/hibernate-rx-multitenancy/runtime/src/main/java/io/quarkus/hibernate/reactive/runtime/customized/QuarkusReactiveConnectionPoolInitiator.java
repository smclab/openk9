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

package io.quarkus.hibernate.reactive.runtime.customized;

import java.util.Map;

import io.vertx.sqlclient.Pool;
import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.reactive.pool.ReactiveConnectionPool;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public final class QuarkusReactiveConnectionPoolInitiator
	implements StandardServiceInitiator<ReactiveConnectionPool> {

    private final Pool pool;

    public QuarkusReactiveConnectionPoolInitiator(Pool pool) {
        this.pool = pool;
    }

    @Override
    public Class<ReactiveConnectionPool> getServiceInitiated() {
        return ReactiveConnectionPool.class;
    }

    @Override
    public ReactiveConnectionPool initiateService(
		Map configurationValues,
		ServiceRegistryImplementor registry) {
        final JdbcServices jdbcService = registry.getService(JdbcServices.class);
        return new MultiSchemaSqlClientPool(
			pool,
			jdbcService.getSqlStatementLogger(),
			jdbcService.getSqlExceptionHelper()
		);
    }

}
