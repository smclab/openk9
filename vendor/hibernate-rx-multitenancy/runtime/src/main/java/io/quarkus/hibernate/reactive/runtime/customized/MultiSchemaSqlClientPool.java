/*
 * Copyright (C) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

import java.util.concurrent.CompletionStage;

import io.vertx.sqlclient.Pool;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.reactive.pool.ReactiveConnection;
import org.hibernate.reactive.pool.impl.SqlClientPool;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class MultiSchemaSqlClientPool extends SqlClientPool
        implements ServiceRegistryAwareService {

    private final Pool pool;
    private SqlStatementLogger sqlStatementLogger;
    private SqlExceptionHelper sqlExceptionHelper;
    private ServiceRegistryImplementor serviceRegistry;
    private final String dbKind;

    public MultiSchemaSqlClientPool(Pool pool) {
        this.pool = pool;
        this.dbKind = ConfigProvider
            .getConfig()
            .getValue("quarkus.datasource.db-kind", String.class);
    }

    @Override
    public void injectServices(ServiceRegistryImplementor serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.sqlStatementLogger = serviceRegistry.getService(SqlStatementLogger.class);
    }

    @Override
    public CompletionStage<Void> getCloseFuture() {
        return CompletionStages.voidFuture();
    }

    @Override
    public CompletionStage<ReactiveConnection> getConnection(String tenantId) {
        return super.getConnection(tenantId)
            .thenCompose(c -> c
                .execute(alterSessionSchema(dbKind, tenantId))
                .thenApply(unused -> c));
    }

    @Override
    protected Pool getPool() {
        return pool;
    }

    @Override
    protected SqlStatementLogger getSqlStatementLogger() {
        return sqlStatementLogger;
    }

    @Override
    protected SqlExceptionHelper getSqlExceptionHelper() {
        if (sqlExceptionHelper == null) {
            sqlExceptionHelper = serviceRegistry
                .getService(JdbcServices.class)
                .getSqlExceptionHelper();
        }
        return sqlExceptionHelper;
    }

    @Override
    protected Pool getTenantPool(String tenantId) {
        return pool;
    }

    private static String alterSessionSchema(String dbKind, String tenantId) {
        // tenantId validation
        if (!tenantId.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Invalid tenant ID: " + tenantId);
        }

        switch (dbKind) {
            case "postgresql":
                return "SET SESSION SCHEMA '" + tenantId + "'";
            case "oracle":
                return "ALTER SESSION SET CURRENT_SCHEMA = " + tenantId;
            default:
                throw new IllegalArgumentException(
                        "dbKind not supported: " + dbKind);
        }
    }

}
