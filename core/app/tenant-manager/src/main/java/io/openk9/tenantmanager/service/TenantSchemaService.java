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

package io.openk9.tenantmanager.service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.tenantmanager.util.CustomClassLoaderResourceAccessor;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TenantSchemaService {

	@ConfigProperty(name = "openk9.datasource.url")
	String openk9DatasourceUrl;

	@ConfigProperty(name = "quarkus.datasource.username")
	String datasourceUsername;

	@ConfigProperty(name = "quarkus.datasource.password")
	String datasourcePassword;

	@ConfigProperty(name = "openk9.datasource.liquibase.change-log")
	String changeLogLocation;

	@ConfigProperty(
		name = "openk9.datasource.liquibase"
			+ ".database-change-log-lock-table-name")
	String changeLogLockTableName;

	@ConfigProperty(
		name = "openk9.datasource.liquibase"
			+ ".database-change-log-table-name")
	String changeLogTableName;

	/**
	 * Returns the Liquibase changelog location.
	 *
	 * @return the configured changelog path
	 */
	public String getChangeLogLocation() {
		return changeLogLocation;
	}

	/**
	 * Returns the Liquibase change log lock table name.
	 *
	 * @return the configured lock table name
	 */
	public String getChangeLogLockTableName() {
		return changeLogLockTableName;
	}

	/**
	 * Returns the Liquibase change log table name.
	 *
	 * @return the configured change log table name
	 */
	public String getChangeLogTableName() {
		return changeLogTableName;
	}

	/**
	 * Runs schema initialization, always creating the schema
	 * (equivalent to {@code createSchema = true}).
	 *
	 * @param tenantName  the tenant schema name
	 * @param virtualHost the virtual host for the tenant binding
	 * @throws LiquibaseException if migration fails
	 */
	public void runInitialization(String tenantName, String virtualHost)
		throws LiquibaseException {

		runInitialization(tenantName, virtualHost, true);
	}

	/**
	 * Runs schema initialization, optionally creating the schema
	 * and its Liquibase shadow schema first.
	 *
	 * @param tenantName   the tenant schema name
	 * @param virtualHost  the virtual host for the tenant binding
	 * @param createSchema if true, creates the schemas before
	 *                     running Liquibase
	 * @throws LiquibaseException if migration fails
	 */
	public void runInitialization(
		String tenantName, String virtualHost, boolean createSchema)
		throws LiquibaseException {

		CustomClassLoaderResourceAccessor resourceAccessor =
			new CustomClassLoaderResourceAccessor(
				Thread.currentThread().getContextClassLoader());

		String liquibaseSchema = tenantName;

		if (createSchema) {
			liquibaseSchema += "_liquibase";
		}

		DatabaseConnection connection =
			DatabaseFactory.getInstance().openConnection(
				toJdbcUrl(openk9DatasourceUrl),
				datasourceUsername,
				datasourcePassword,
				null,
				resourceAccessor);

		Database database = _createDatabase(
			connection, tenantName, liquibaseSchema);

		try (Liquibase liquibase = new Liquibase(
			changeLogLocation, resourceAccessor, database)) {

			if (createSchema) {
				_createSchemas(
					connection, tenantName, liquibaseSchema);
			}

			liquibase.validate();
			liquibase.update(new Contexts(), new LabelExpression());

			_insertIntoTenantBinding(
				connection, tenantName, virtualHost);

		}
		catch (Exception ex) {
			if (ex instanceof LiquibaseException) {
				throw (LiquibaseException) ex;
			}
			throw new LiquibaseException(ex);
		}
	}

	/**
	 * Runs Liquibase update for all given tenant names.
	 * The Liquibase schema is derived as
	 * {@code tenantName + "_liquibase"}.
	 *
	 * @param tenantNames list of tenant name strings
	 * @throws LiquibaseException if any migration fails
	 */
	public void runUpdate(List<String> tenantNames)
		throws LiquibaseException {

		CustomClassLoaderResourceAccessor resourceAccessor =
			new CustomClassLoaderResourceAccessor(
				Thread.currentThread().getContextClassLoader());

		for (String tenantName : tenantNames) {

			String liquibaseSchema = tenantName + "_liquibase";

			DatabaseConnection connection =
				DatabaseFactory.getInstance().openConnection(
					toJdbcUrl(openk9DatasourceUrl),
					datasourceUsername,
					datasourcePassword,
					null,
					resourceAccessor);

			Database database = _createDatabase(
				connection, tenantName, liquibaseSchema);

			try (Liquibase liquibase =
					new Liquibase(
						changeLogLocation, resourceAccessor,
						database)) {

				liquibase.validate();
				liquibase.update(
					new Contexts(), new LabelExpression());
			}
			catch (Exception ex) {
				if (ex instanceof LiquibaseException) {
					throw (LiquibaseException) ex;
				}
				throw new LiquibaseException(ex);
			}

			logger.info(
				"Liquibase update for schema "
				+ tenantName + " completed");
		}

	}

	/**
	 * Drops the tenant schema and its Liquibase shadow schema.
	 * Derives the liquibase schema name as
	 * {@code schemaName + "_liquibase"}.
	 *
	 * @param schemaName the tenant schema name to drop
	 */
	public void rollbackRunLiquibaseMigration(String schemaName) {
		rollbackRunLiquibaseMigration(
			schemaName, schemaName + "_liquibase");
	}

	/**
	 * Drops the tenant schema and its Liquibase shadow schema.
	 *
	 * @param schemaName      the tenant schema name to drop
	 * @param liquibaseSchema the liquibase shadow schema to drop
	 */
	public void rollbackRunLiquibaseMigration(
		String schemaName, String liquibaseSchema) {

		CustomClassLoaderResourceAccessor resourceAccessor =
			new CustomClassLoaderResourceAccessor(
				Thread.currentThread().getContextClassLoader());

		try (DatabaseConnection connection =
				DatabaseFactory.getInstance().openConnection(
					toJdbcUrl(openk9DatasourceUrl),
					datasourceUsername,
					datasourcePassword,
					null,
					resourceAccessor)) {

			_dropSchemas(connection, schemaName, liquibaseSchema);

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private Database _createDatabase(
		DatabaseConnection connection,
		String schemaName,
		String liquibaseSchema)
		throws DatabaseException {

		Database database = DatabaseFactory.getInstance()
			.findCorrectDatabaseImplementation(connection);

		database.setDatabaseChangeLogLockTableName(
			changeLogLockTableName);
		database.setDatabaseChangeLogTableName(changeLogTableName);
		database.setDefaultSchemaName(schemaName);
		database.setLiquibaseSchemaName(liquibaseSchema);

		return database;

	}

	private void _insertIntoTenantBinding(
		DatabaseConnection connection,
		String schemaName,
		String virtualHost)
		throws Exception {

		JdbcConnection jdbcConnection = (JdbcConnection) connection;

		String query;

		if (jdbcConnection.getDatabaseProductName()
			.equals("Oracle")) {
			query =
				"ALTER SESSION SET CURRENT_SCHEMA = '"
				+ schemaName + "';";
		}
		else {
			query = "SET LOCAL SCHEMA '" + schemaName + "';";
		}

		query += "INSERT INTO tenant_binding"
			+ "(id, virtual_host, create_date, modified_date)"
			+ " VALUES(?, ?, ?, ?);";

		try (PreparedStatement statement2 =
				jdbcConnection.prepareStatement(query)) {

			statement2.setLong(1, 1);
			statement2.setString(2, virtualHost);
			Timestamp now = new Timestamp(System.currentTimeMillis());
			statement2.setTimestamp(3, now);
			statement2.setTimestamp(4, now);

			statement2.executeUpdate();

			jdbcConnection.commit();

		}

	}

	private void _createSchemas(
		DatabaseConnection databaseConnection,
		String schemaName,
		String liquibaseSchema)
		throws Exception {

		JdbcConnection jdbcConnection =
			(JdbcConnection) databaseConnection;

		try (Statement statement1 = jdbcConnection.createStatement();
			Statement statement2 = jdbcConnection.createStatement()) {

			statement1.executeUpdate(
				"CREATE SCHEMA " + schemaName);
			statement2.executeUpdate(
				"CREATE SCHEMA " + liquibaseSchema);

			jdbcConnection.commit();
		}

	}

	private void _dropSchemas(
		DatabaseConnection databaseConnection,
		String schemaName,
		String liquibaseSchema)
		throws Exception {

		JdbcConnection jdbcConnection =
			(JdbcConnection) databaseConnection;

		try (Statement statement1 = jdbcConnection.createStatement();
			Statement statement2 = jdbcConnection.createStatement()) {

			statement1.executeUpdate(
				"DROP SCHEMA " + schemaName + " CASCADE");
			statement2.executeUpdate(
				"DROP SCHEMA " + liquibaseSchema
				+ " CASCADE");

			jdbcConnection.commit();
		}

	}

	/**
	 * Converts a reactive/JDBC URL to a JDBC URL for Liquibase.
	 *
	 * @param datasourceUrl the raw datasource URL
	 * @return the JDBC URL
	 */
	public String toJdbcUrl(String datasourceUrl) {

		if (datasourceUrl.startsWith("vertx-reactive:")) {
			datasourceUrl =
				datasourceUrl.replace("vertx-reactive:", "jdbc:");
		}
		else {
			datasourceUrl = "jdbc:" + datasourceUrl;
		}

		return datasourceUrl;

	}

	/**
	 * Returns the JDBC URL derived from the datasource URL.
	 *
	 * @return the JDBC URL string
	 */
	public String getDatasourceJdbcUrl() {
		return toJdbcUrl(openk9DatasourceUrl);
	}

	@Inject
	Logger logger;

}
