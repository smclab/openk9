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

package io.openk9.tenantmanager.pipe.liquibase.validate;

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
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;

public class Liquibase {

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}

	public sealed interface Response {}
	public record Success(String schemaName) implements Response {}

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
			actorRef.tell(new Error(new LiquibaseException(e)));
		}

		return Behaviors.stopped();
	}

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

	public record Error(LiquibaseException exception) implements Response {}

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
