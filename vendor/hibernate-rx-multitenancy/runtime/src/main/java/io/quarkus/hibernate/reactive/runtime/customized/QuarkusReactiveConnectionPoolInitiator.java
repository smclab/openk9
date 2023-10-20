package io.quarkus.hibernate.reactive.runtime.customized;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.config.ConfigProvider;
import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.reactive.pool.ReactiveConnection;
import org.hibernate.reactive.pool.ReactiveConnectionPool;
import org.hibernate.reactive.pool.impl.SqlClientPool;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import io.vertx.sqlclient.Pool;

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
    public ReactiveConnectionPool initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
        SqlStatementLogger sqlStatementLogger = registry.getService(JdbcServices.class).getSqlStatementLogger();

        return new ExternalSqlClientPool(pool, sqlStatementLogger);
    }

    private static class ExternalSqlClientPool extends SqlClientPool {

        private final Pool pool;
        private final SqlStatementLogger sqlStatementLogger;
        private final String dbKind;

        public ExternalSqlClientPool(Pool pool, SqlStatementLogger sqlStatementLogger) {
            this.pool = pool;
            this.sqlStatementLogger = sqlStatementLogger;
            this.dbKind = ConfigProvider
                    .getConfig()
                    .getValue("quarkus.datasource.db-kind", String.class);
        }

        @Override
        protected Pool getPool() {
            return pool;
        }

        @Override
        protected SqlStatementLogger getSqlStatementLogger() {
            return sqlStatementLogger;
        }

        /**
         * Since this Service implementation does not implement @{@link org.hibernate.service.spi.Stoppable}
         * and we're only adapting an externally provided pool, we will not actually close such provided pool
         * when Hibernate ORM is shutdown (it doesn't own the lifecycle of this external component).
         * Therefore, there is no need to wait for its shutdown and this method returns an already
         * successfully completed CompletionStage.
         *
         * @return
         */
        @Override
        public CompletionStage<Void> getCloseFuture() {
            return CompletionStages.voidFuture();
        }

        @Override
        protected Pool getTenantPool(String tenantId) {
            return pool;
        }

        @Override
        public CompletionStage<ReactiveConnection> getConnection(String tenantId) {
            return super.getConnection(tenantId)
                    .thenCompose(c -> c
                            .execute(alterSessionSchema(dbKind, tenantId))
                            .thenApply(unused -> c));
        }

        private static String alterSessionSchema(String dbKind, String tenantId) {
            return switch (dbKind) {
                case "postgresql" -> "SET SESSION SCHEMA '" + tenantId + "'";
                case "oracle" -> "ALTER SESSION SET CURRENT_SCHEMA = " + tenantId;
                default -> throw new IllegalArgumentException("dbKind not supported: " + dbKind);
            };
        }

    }
}
