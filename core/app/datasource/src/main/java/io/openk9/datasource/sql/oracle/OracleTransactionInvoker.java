package io.openk9.datasource.sql.oracle;

import io.openk9.auth.tenant.MultiTenancyConfig;
import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.sql.TransactionInvoker;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.validation.Validator;

public class OracleTransactionInvoker extends TransactionInvoker {

	public OracleTransactionInvoker(
		Mutiny.SessionFactory em,
		Validator validator,
		TenantResolver tenantResolver,
		MultiTenancyConfig multiTenancyConfig) {
		super(em, validator, tenantResolver, multiTenancyConfig);
	}

	@Override
	protected String alterSchemaSession(String schemaName) {
		return "ALTER SESSION SET CURRENT_SCHEMA = '" + schemaName + "'";
	}
}
