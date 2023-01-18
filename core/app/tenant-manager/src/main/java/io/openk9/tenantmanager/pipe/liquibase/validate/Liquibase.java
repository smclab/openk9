package io.openk9.tenantmanager.pipe.liquibase.validate;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.tenantmanager.pipe.liquibase.validate.util.Params;
import io.openk9.tenantmanager.util.CustomClassLoaderResourceAccessor;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Scope;
import liquibase.ScopeManager;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;

public class Liquibase {

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}

	public sealed interface Response {}
	public record Success(String schemaName) implements Response {}
	public record Error(Throwable throwable) implements Response {}

	public static Behavior<Command> validate(
		Params params, ActorRef<Response> actorRef) {
		return Behaviors.setup(
			context -> initial(params, actorRef, context));
	}

	private static Behavior<Command> initial(
		Params params, ActorRef<Response> actorRef, ActorContext<Command> context) {

		return Behaviors.receive(Command.class)
			.onMessageEquals(Start.INSTANCE, () -> onStart(
				params, actorRef, context))
			.build();

	}

	private static Behavior<Command> onStart(
		Params params, ActorRef<Response> actorRef, ActorContext<Command> context) {

		try {

			setThreadLocalLiquibaseScopeManager();

			CustomClassLoaderResourceAccessor resourceAccessor =
				new CustomClassLoaderResourceAccessor(
					Thread.currentThread().getContextClassLoader());

			DatabaseConnection connection = DatabaseFactory.getInstance().openConnection(
				params.jdbcUrl(), params.username(), params.password(), null,
				resourceAccessor);

			Database database = _createDatabase(params, connection);

			context.getLog().info("Validating schema: {}", params.schemaName());

			try(liquibase.Liquibase liquibase = new liquibase.Liquibase(params.liquibaseChangeLog(), resourceAccessor, database)) {
				liquibase.validate();
				liquibase.update(new Contexts(), new LabelExpression());
				context.getLog().info("Schema validated: {}", params.schemaName());
				actorRef.tell(new Success(params.schemaName()));
			}

		}
		catch (Exception e) {
			actorRef.tell(new Error(e));
		}

		return Behaviors.stopped();
	}

	private static Database _createDatabase(
		Params params, DatabaseConnection connection) throws DatabaseException {
		Database database = DatabaseFactory
			.getInstance()
			.findCorrectDatabaseImplementation(connection);

		if (params.changeLogLockTableName() != null) {
			database.setDatabaseChangeLogLockTableName(params.changeLogLockTableName());
		}

		if (params.changeLogTableName() != null) {
			database.setDatabaseChangeLogTableName(params.changeLogTableName());
		}

		if (params.schemaName() != null) {
			database.setDefaultSchemaName(params.schemaName());
		}

		if (params.liquibaseSchemaName() != null) {
			database.setLiquibaseSchemaName(params.liquibaseSchemaName());
		}

		return database;
	}

	private static void setThreadLocalLiquibaseScopeManager() {
		Scope.setScopeManager(new ScopeManager() {

			private final ThreadLocal<Scope> currentScope = new ThreadLocal<>();
			private final Scope rootScope = Scope.getCurrentScope();

			@Override
			public synchronized Scope getCurrentScope() {
				Scope returnedScope = currentScope.get();

				if (returnedScope == null) {
					returnedScope = rootScope;
				}

				return returnedScope;
			}

			@Override
			protected Scope init(Scope scope) throws Exception {
				return scope;
			}

			@Override
			protected synchronized void setCurrentScope(Scope scope) {
				this.currentScope.set(scope);
			}
		});
	}

}
