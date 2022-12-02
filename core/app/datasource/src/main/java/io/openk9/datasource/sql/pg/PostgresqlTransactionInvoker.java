package io.openk9.datasource.sql.pg;

import io.openk9.auth.tenant.MultiTenancyConfig;
import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.sql.TransactionInvoker;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.validation.Validator;

public class PostgresqlTransactionInvoker extends TransactionInvoker {

	public PostgresqlTransactionInvoker(
		Mutiny.SessionFactory em,
		Validator validator,
		TenantResolver tenantResolver,
		MultiTenancyConfig multiTenancyConfig) {
		super(em, validator, tenantResolver, multiTenancyConfig);
	}

	@Override
	protected String alterSchemaSession(String schemaName) {
		return "SET SESSION SCHEMA '" + schemaName + "'";
	}
}
