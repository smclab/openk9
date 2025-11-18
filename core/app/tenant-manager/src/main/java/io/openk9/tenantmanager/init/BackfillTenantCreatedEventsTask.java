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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.openk9.common.util.CompactSnowflakeIdGenerator;
import io.openk9.event.tenant.TenantManagementEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * This CustomTaskChange is needed for backfilling the OutboxEvent
 * table, persisting the TenantCreated events,
 * from the pre-existing Tenant rows present in the database.
 * <p>
 * It looks for the oldest event inserted in the {@code outbox_event} table, then it fetches from
 * the {@code tenant} table all the rows inserted before that event's {@code create_date} datetime.
 * A TenantCreated event mapping is done from the
 * fetched {@code tenant} rows, and they are persisted in the {@code outbox_event} table.
 */
public class BackfillTenantCreatedEventsTask implements CustomTaskChange {


	@Override
	public void execute(Database database) throws CustomChangeException {

		JdbcConnection connection = (JdbcConnection) database.getConnection();

		try {
			List<OutboxRow> outboxRows = prepareTenantCreatedEvents(connection);

			if (outboxRows.isEmpty()) {
				// no pre-existing tenants found
				return;
			}

			int eventsCreated = outboxBackfill(connection, outboxRows);

			this.confirmationMessage =
				String.format("Successfully backfilled %d TenantCreated events.", eventsCreated);

		}
		catch (DatabaseException | SQLException e) {
			throw new CustomChangeException("Failed to backfill TenantCreated events", e);
		}

	}

	private List<OutboxRow> prepareTenantCreatedEvents(JdbcConnection connection)
		throws DatabaseException, SQLException {

		List<OutboxRow> events = new ArrayList<>();

		String sql = """
			SELECT
				create_date,
				schema_name,
				virtual_host,
				client_id,
				client_secret,
				realm_name
			FROM tenant
			""";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {

					var createDate = rs.getTimestamp(1);
					var schemaName = rs.getString(2);
					var virtualHost = rs.getString(3);
					var clientId = rs.getString(4);
					var clientSecret = rs.getString(5);
					var realmName = rs.getString(6);

					ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
					objectNode.put("tenantId", schemaName);
					objectNode.put("hostName", virtualHost);
					objectNode.put("schemaName", schemaName);
					objectNode.put("issuerUri", baseIssuerUri + realmName);
					objectNode.put("clientId", clientId);
					objectNode.put("clientSecret", clientSecret);

					var payload = objectNode.toString();

					OutboxRow outboxRow = new OutboxRow(payload, createDate);

					events.add(outboxRow);
				}
			}
		}

		return events;

	}

	private int outboxBackfill(JdbcConnection connection, List<OutboxRow> outboxRows)
		throws DatabaseException, SQLException {

		String insertSql = """
			INSERT
			INTO outbox_event (id, event_type, payload, sent, create_date)
			VALUES (?, ?, ?, ?, ?)
			""";

		int batchCount = 0;

		try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {

			for (OutboxRow outboxRow : outboxRows) {

				var id = ID_GENERATOR.nextId();

				insertStmt.setLong(1, id);
				insertStmt.setString(2, EVENT_TYPE);
				insertStmt.setString(3, outboxRow.payload());
				insertStmt.setBoolean(4, false);
				insertStmt.setTimestamp(5, outboxRow.createDate());

				insertStmt.addBatch();

				batchCount++;
			}

			if (batchCount > 0) {
				insertStmt.executeBatch();
			}

		}

		return batchCount;
	}

	@Override
	public String getConfirmationMessage() {
		return this.confirmationMessage;
	}

	@Override
	public void setUp() {
		String baseIssuerUri = ConfigProvider
			.getConfig()
			.getConfigValue("openk9.tenant-manager.keycloak-base-issuer-uri")
			.getValue();

		Objects.requireNonNull(baseIssuerUri);

		this.baseIssuerUri = baseIssuerUri;
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// Not needed for this change
	}


	@Override
	public ValidationErrors validate(Database database) {
		return new ValidationErrors();
	}

	private String baseIssuerUri;
	private String confirmationMessage = "No tenants found to backfill.";

	private final static CompactSnowflakeIdGenerator ID_GENERATOR =
		new CompactSnowflakeIdGenerator();
	private static final String EVENT_TYPE =
		TenantManagementEvent.TenantCreated.class.getSimpleName();
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private record OutboxRow(String payload, Timestamp createDate) {}

}
