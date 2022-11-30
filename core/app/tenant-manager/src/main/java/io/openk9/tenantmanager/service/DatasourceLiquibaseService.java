package io.openk9.tenantmanager.service;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Statement;
import java.util.List;

@ApplicationScoped
public class DatasourceLiquibaseService {

	@ConfigProperty(name = "openk9.datasource.url")
	String openk9DatasourceUrl;

	@ConfigProperty(name = "quarkus.datasource.username")
	String datasourceUsername;

	@ConfigProperty(name = "quarkus.datasource.password")
	String datasourcePassword;

	@ConfigProperty(name = "openk9.datasource.liquibase.change-log")
	String changeLogLocation;

	@ConfigProperty(name = "openk9.datasource.liquibase.database-change-log-lock-table-name")
	String changeLogLockTableName;

	@ConfigProperty(name = "openk9.datasource.liquibase.database-change-log-table-name")
	String changeLogTableName;

	public void runInitialization(String schemaName, String virtualHost) throws LiquibaseException {

		ClassLoaderResourceAccessor resourceAccessor =
			new ClassLoaderResourceAccessor(
				Thread.currentThread().getContextClassLoader());

		String liquibaseSchema = schemaName + "_liquibase";

		DatabaseConnection connection = DatabaseFactory.getInstance().openConnection(
			toJdbcUrl(openk9DatasourceUrl), datasourceUsername, datasourcePassword,
			null, resourceAccessor);

		Database database = _createDatabase(connection, schemaName, liquibaseSchema);

		try(Liquibase liquibase = new Liquibase(changeLogLocation, resourceAccessor, database)) {

			_createSchemas(connection, schemaName, liquibaseSchema);

			liquibase.validate();
			liquibase.update(new Contexts(), new LabelExpression());

			_insertIntoTenantBinding(connection, schemaName, virtualHost);


		} catch (Exception ex) {
			if (ex instanceof LiquibaseException) {
				throw (LiquibaseException) ex;
			}
			throw new LiquibaseException(ex);
		}
	}

	public void runUpdate(List<String> schemaNames) throws LiquibaseException {

		ClassLoaderResourceAccessor resourceAccessor =
			new ClassLoaderResourceAccessor(
				Thread.currentThread().getContextClassLoader());

		for (String schemaName : schemaNames) {

			DatabaseConnection connection = DatabaseFactory.getInstance().openConnection(
				toJdbcUrl(openk9DatasourceUrl), datasourceUsername, datasourcePassword,
				null, resourceAccessor);

			Database database = _createDatabase(
				connection, schemaName, schemaName + "_liquibase");

			try {
				Liquibase liquibase = new Liquibase(changeLogLocation, resourceAccessor, database);
				liquibase.validate();
				liquibase.update(new Contexts(), new LabelExpression());

			} catch (Exception ex) {
				if (ex instanceof LiquibaseException) {
					throw (LiquibaseException) ex;
				}
				throw new LiquibaseException(ex);
			}

			logger.info("Liquibase update for schema " + schemaName + " completed");

		}

	}

	public void rollbackRunLiquibaseMigration(String schemaName) {

		ClassLoaderResourceAccessor resourceAccessor =
			new ClassLoaderResourceAccessor(
				Thread.currentThread().getContextClassLoader());

		String liquibaseSchema = schemaName + "_liquibase";

		try (DatabaseConnection connection =
				 DatabaseFactory.getInstance().openConnection(
					 toJdbcUrl(openk9DatasourceUrl), datasourceUsername,
					 datasourcePassword,
					 null, resourceAccessor);) {

			_dropSchemas(connection, schemaName, liquibaseSchema);

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private Database _createDatabase(
		DatabaseConnection connection, String schemaName, String liquibaseSchema) throws DatabaseException {

		Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);

		database.setDatabaseChangeLogLockTableName(changeLogLockTableName);
		database.setDatabaseChangeLogTableName(changeLogTableName);
		database.setDefaultSchemaName(schemaName);
		database.setLiquibaseSchemaName(liquibaseSchema);

		return database;

	}

	private void _insertIntoTenantBinding(
		DatabaseConnection connection, String schemaName, String virtualHost)
		throws Exception {

		JdbcConnection jdbcConnection = (JdbcConnection)connection;

		try (Statement statement1 = jdbcConnection.createStatement();) {

			statement1.executeUpdate("SET LOCAL SCHEMA '" + schemaName + "'");

			statement1.executeUpdate(
				"INSERT INTO tenant_binding (id, virtual_host, create_date, modified_date) " +
				"VALUES(1, '"  + virtualHost + "', now(), now());");

			jdbcConnection.commit();

		}

	}

	private void _createSchemas(
			DatabaseConnection databaseConnection, String schemaName, String liquibaseSchema)
		throws Exception {

		JdbcConnection jdbcConnection = (JdbcConnection)databaseConnection;

		try (Statement statement1 = jdbcConnection.createStatement();
			 Statement statement2 = jdbcConnection.createStatement();) {

			statement1.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + schemaName);
			statement2.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + liquibaseSchema);

			jdbcConnection.commit();
		}

	}

	private void _dropSchemas(
			DatabaseConnection databaseConnection, String schemaName, String liquibaseSchema)
		throws Exception {

		JdbcConnection jdbcConnection = (JdbcConnection)databaseConnection;

		try (Statement statement1 = jdbcConnection.createStatement();
			 Statement statement2 = jdbcConnection.createStatement();) {

			statement1.executeUpdate("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
			statement2.executeUpdate("DROP SCHEMA IF EXISTS " + liquibaseSchema + " CASCADE");

			jdbcConnection.commit();
		}

	}

	public String toJdbcUrl(String datasourceUrl) {

		if (datasourceUrl.startsWith("vertx-reactive:")) {
			datasourceUrl = datasourceUrl.replace("vertx-reactive:", "jdbc:");
		}
		else {
			datasourceUrl = "jdbc:" + datasourceUrl;
		}

		return datasourceUrl;

	}

	@Inject
	Logger logger;

}
