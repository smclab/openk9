package io.openk9.datasource.sql;

import io.openk9.auth.tenant.MultiTenancyConfig;
import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.service.exception.K9Error;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.Validator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class TransactionInvoker {

	protected TransactionInvoker(
		Mutiny.SessionFactory em, Validator validator,
		TenantResolver tenantResolver, MultiTenancyConfig multiTenancyConfig) {
		this.em = em;
		this.validator = validator;
		this.tenantResolver = tenantResolver;
		this.multiTenancyConfig = multiTenancyConfig;
	}

	public CriteriaBuilder getCriteriaBuilder() {
		return em.getCriteriaBuilder();
	}

	public <T> Uni<T> withTransaction(Function<Mutiny.Session, Uni<T>> fun) {
		return em.withTransaction(fun);
	}

	public <T> Uni<T> withTransaction(BiFunction<Mutiny.Session, Mutiny.Transaction, Uni<T>> fun) {
		return em.withTransaction(fun);
	}

	public <T> Uni<T> withTransaction(String schema, Function<Mutiny.Session, Uni<T>> fun) {
		return em.withTransaction(schema, (session, transaction) -> fun.apply(session));
	}

	public <T> Uni<T> withTransaction(
		String schema, BiFunction<Mutiny.Session, Mutiny.Transaction, Uni<T>> fun) {

		return em.withTransaction(schema, fun);
	}

	public <T> Uni<T> withStatelessTransaction(
		Function<Mutiny.StatelessSession, Uni<T>> fun) {

		return em.withStatelessTransaction((session, transaction) -> fun.apply(session));

	}

	public <T> Uni<T> withStatelessTransaction(
		String schema, Function<Mutiny.StatelessSession, Uni<T>> fun) {

		return em.withStatelessTransaction(schema, (session, transaction) -> fun.apply(session));

	}

	public <T> Uni<T> withStatelessTransaction(
		BiFunction<Mutiny.StatelessSession, Mutiny.Transaction, Uni<T>> fun) {

		return em.withStatelessTransaction(fun);

	}

	public <T> Uni<T> withStatelessTransaction(
		String schema, BiFunction<Mutiny.StatelessSession, Mutiny.Transaction, Uni<T>> fun) {

		return em.withStatelessTransaction(schema, fun);

	}

	protected abstract String alterSchemaSession(String schemaName);

	private final Mutiny.SessionFactory em;

	private final Validator validator;

	private final TenantResolver tenantResolver;

	private final MultiTenancyConfig multiTenancyConfig;

}
