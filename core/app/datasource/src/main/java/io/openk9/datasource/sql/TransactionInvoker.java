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

	public <T> Uni<T> withTransaction(Supplier<Uni<T>> fun) {
		return withTransaction(null, (session, transaction) -> fun.get());
	}

	public <T> Uni<T> withTransaction(
		Function<Mutiny.Session, Uni<T>> fun) {
		return withTransaction(null, (session, transaction) -> fun.apply(session));
	}

	public <T> Uni<T> withTransaction(
		BiFunction<Mutiny.Session, Mutiny.Transaction, Uni<T>> fun) {

		return withTransaction(null, fun);

	}

	public <T> Uni<T> withTransaction(String schema, Supplier<Uni<T>> fun) {
		return withTransaction(schema, (session, transaction) -> fun.get());
	}

	public <T> Uni<T> withTransaction(
		String schema, Function<Mutiny.Session, Uni<T>> fun) {
		return withTransaction(schema, (session, transaction) -> fun.apply(session));
	}

	public <T> Uni<T> withTransaction(
		String schema, BiFunction<Mutiny.Session, Mutiny.Transaction, Uni<T>> fun) {

		return withTransaction(
			schema,
			em::withTransaction,
			Mutiny.Session::isOpen,
			Mutiny.Session::createNativeQuery, fun);

	}

	public <T> Uni<T> withStatelessTransaction(Supplier<Uni<T>> fun) {
		return withStatelessTransaction(null, (session, transaction) -> fun.get());

	}

	public <T> Uni<T> withStatelessTransaction(
		Function<Mutiny.StatelessSession, Uni<T>> fun) {

		return withStatelessTransaction(null, (session, transaction) -> fun.apply(session));

	}

	public <T> Uni<T> withStatelessTransaction(String schema, Supplier<Uni<T>> fun) {
		return withStatelessTransaction(schema, (session, transaction) -> fun.get());

	}

	public <T> Uni<T> withStatelessTransaction(
		String schema, Function<Mutiny.StatelessSession, Uni<T>> fun) {

		return withStatelessTransaction(schema, (session, transaction) -> fun.apply(session));

	}

	public <T> Uni<T> withStatelessTransaction(
		BiFunction<Mutiny.StatelessSession, Mutiny.Transaction, Uni<T>> fun) {

		return withStatelessTransaction(null, fun);

	}

	public <T> Uni<T> withStatelessTransaction(
		String schema, BiFunction<Mutiny.StatelessSession, Mutiny.Transaction, Uni<T>> fun) {

		return withTransaction(
			schema,
			em::withStatelessTransaction,
			Mutiny.StatelessSession::isOpen,
			Mutiny.StatelessSession::createNativeQuery, fun);

	}

	private <S, T> Uni<T> withTransaction(
		String schema,
		Function<BiFunction<S, Mutiny.Transaction, Uni<T>>, Uni<T>> fun,
		java.util.function.Predicate<S> isOpen,
		BiFunction<S, String, Mutiny.Query<?>> createNativeQuery,
		BiFunction<S, Mutiny.Transaction, Uni<T>> fun2) {

		return Uni
			.createFrom()
			.deferred(() -> fun.apply((s, t) -> {

				Context context = Vertx.currentContext();

				String currentSchema = context.getLocal("currentSchema");

				if (!multiTenancyConfig.isEnabled() || currentSchema != null) {
					return fun2.apply(s, t);
				}

				if (!isOpen.test(s)) {
					throw new IllegalStateException(
						"unexpected state: Hibernate session is not active in tenant transaction interceptor");
				}

				String sessionTenantId = tenantResolver.getTenantName();

				if (sessionTenantId == null && schema != null) {
					sessionTenantId = schema;
					tenantResolver.setTenant(schema);
				}
				else if (schema != null) {
					sessionTenantId = schema;
				}

				String newSchema = sessionTenantId;

				if (!validator.validate(newSchema).isEmpty()) {
					// double check just in case to avoid potential SQL injection
					// (hint appreciated: how to properly escape postgres string literal here instead?)
					throw new IllegalStateException(
						"unexpected state: Hibernate session is not active in tenant transaction interceptor");
				}

				return createNativeQuery.apply(s, alterSchemaSession(newSchema))
					.executeUpdate()
					.invoke(() -> context.putLocal("currentSchema", newSchema))
					.flatMap((ignore) -> {
						try {
							return fun2.apply(s, t);
						} catch (Exception e) {
							if (e instanceof K9Error) {
								return Uni.createFrom().failure(e);
							}
							return Uni.createFrom().failure(new K9Error(e));
						}
					});
			}));

	}

	protected abstract String alterSchemaSession(String schemaName);

	private final Mutiny.SessionFactory em;

	private final Validator validator;

	private final TenantResolver tenantResolver;

	private final MultiTenancyConfig multiTenancyConfig;

}
