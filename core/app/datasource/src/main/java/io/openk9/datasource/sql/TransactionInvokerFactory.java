package io.openk9.datasource.sql;

import io.openk9.auth.tenant.MultiTenancyConfig;
import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.sql.oracle.OracleTransactionInvoker;
import io.openk9.datasource.sql.pg.PostgresqlTransactionInvoker;
import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.validation.Validator;

@Dependent
@Startup
public class TransactionInvokerFactory {

	@Produces
	@ApplicationScoped
	public TransactionInvoker createTransactionInvoker(
		@ConfigProperty(name = "quarkus.datasource.db-kind") String dbKind,
		Mutiny.SessionFactory em,
		Validator validator,
		TenantResolver tenantResolver,
		MultiTenancyConfig multiTenancyConfig) {

		return switch (dbKind) {
			case "postgresql" -> new PostgresqlTransactionInvoker(
				em, validator, tenantResolver, multiTenancyConfig);
			case "oracle" -> new OracleTransactionInvoker(
				em, validator, tenantResolver, multiTenancyConfig);
			default -> throw new IllegalArgumentException("dbKind not supported: " + dbKind);
		};

	}



}
