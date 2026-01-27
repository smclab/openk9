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

package io.openk9.tenantmanager.init;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.eclipse.microprofile.config.ConfigProvider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Backfills the 'issuer_uri' column in the 'tenant' table.
 * <p>
 * It fetches the existing 'realm_name', concatenates it with the
 * configured 'baseIssuerUri', and updates the row using the 'id' column.
 */
public class BackfillTenantIssuerUriTask implements CustomTaskChange {

	private String baseIssuerUri;
	private String confirmationMessage = "No tenants found to update.";

	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();

		try {
			int updatedCount = updateIssuerUris(connection);

			if (updatedCount > 0) {
				this.confirmationMessage = String.format(
					"Successfully updated issuer_uri for %d tenants.", updatedCount
				);
			}
		} catch (DatabaseException | SQLException e) {
			throw new CustomChangeException("Failed to backfill issuer_uri", e);
		}
	}

	private int updateIssuerUris(JdbcConnection connection) throws DatabaseException, SQLException {
		// 1. Select tenants that need updating using 'id' and 'realm_name'
		String selectSql = """
            SELECT id, realm_name
            FROM tenant
            WHERE issuer_uri IS NULL
            """;

		String updateSql = """
            UPDATE tenant
            SET issuer_uri = ?
            WHERE id = ?
            """;

		int batchCount = 0;

		try (PreparedStatement selectStmt = connection.prepareStatement(selectSql);
			ResultSet rs = selectStmt.executeQuery();
			PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {

			while (rs.next()) {
				// Using getObject/setObject is safer if we don't know if ID is UUID(String) or Long
				Object tenantId = rs.getObject("id");
				String realmName = rs.getString("realm_name");

				if (realmName != null && !realmName.isEmpty()) {
					String fullIssuerUri = this.baseIssuerUri + realmName;

					updateStmt.setString(1, fullIssuerUri);
					updateStmt.setObject(2, tenantId); // Bind the ID for the WHERE clause
					updateStmt.addBatch();

					batchCount++;
				}
			}

			if (batchCount > 0) {
				updateStmt.executeBatch();
			}
		}

		return batchCount;
	}

	@Override
	public void setUp() throws SetupException {
		try {
			// Reusing the config retrieval logic from your existing tasks
			String baseIssuerUri = ConfigProvider
				.getConfig()
				.getConfigValue("openk9.tenant-manager.keycloak-base-issuer-uri")
				.getValue();

			Objects.requireNonNull(baseIssuerUri, "a baseIssuerUri needs to be specified");

			this.baseIssuerUri = baseIssuerUri;

		} catch (Exception e) {
			throw new SetupException("Failed to load configuration for baseIssuerUri", e);
		}
	}

	@Override
	public String getConfirmationMessage() {
		return this.confirmationMessage;
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// Not needed
	}

	@Override
	public ValidationErrors validate(Database database) {
		return new ValidationErrors();
	}
}
