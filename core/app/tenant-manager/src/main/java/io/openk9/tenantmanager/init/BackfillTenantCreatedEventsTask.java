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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.openk9.common.util.CompactSnowflakeIdGenerator;
import io.openk9.event.tenant.Authorization;
import io.openk9.event.tenant.Route;
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
			List<BackfillEvent> backfillEvents = prepareBackfillEvents(connection);

			if (backfillEvents.isEmpty()) {
				// no pre-existing tenants found
				return;
			}

			int eventsCreated = insertBackfillEvents(connection, backfillEvents);

			this.confirmationMessage =
				String.format("Successfully backfilled %d TenantCreated events.", eventsCreated);

		}
		catch (DatabaseException | SQLException e) {
			throw new CustomChangeException("Failed to backfill TenantCreated events", e);
		}

	}

	private List<BackfillEvent> prepareBackfillEvents(JdbcConnection connection)
		throws DatabaseException, SQLException {

		// Get the timestamp until we can search for pre-existent tenants.
		// The fallback value is current timestamp, it will be updated with
		// the oldest TenantCreated event creation date.

		Timestamp cutOffTimestamp = Timestamp.from(Instant.now());

		String minCreateDateSql = """
			SELECT MIN(create_date)
			FROM outbox_event
			WHERE event_type = ?
			""";

		try (PreparedStatement stmt = connection.prepareStatement(minCreateDateSql)) {
			stmt.setString(1, EVENT_TYPE);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					 var oldestEventTimestamp = rs.getTimestamp(1);
					 if (oldestEventTimestamp != null) {
						 cutOffTimestamp = oldestEventTimestamp;
					 }
				}
			}

		}

		// Collect all the pre-existent tenants and prepare a list of
		// object needed to build the backfilled outbox events.

		List<BackfillEvent> events = new ArrayList<>();

		String selectTenantsSql = """
			SELECT
				create_date,
				schema_name,
				virtual_host,
				client_id,
				client_secret,
				realm_name
			FROM tenant
			WHERE create_date < ?
			""";

		try (PreparedStatement stmt = connection.prepareStatement(selectTenantsSql)) {
			stmt.setTimestamp(1, cutOffTimestamp);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {

					var createDate = rs.getTimestamp(1);
					var schemaName = rs.getString(2);
					var virtualHost = rs.getString(3);
					var clientId = rs.getString(4);
					var clientSecret = rs.getString(5);
					var realmName = rs.getString(6);

					ObjectNode objectNode = OBJ_MAPPER.createObjectNode();

					objectNode.put("tenantId", schemaName);
					objectNode.put("hostName", virtualHost);
					objectNode.put("schemaName", schemaName);
					objectNode.put("clientId", clientId);
					objectNode.put("clientSecret", clientSecret);

					// Assuming the previous behavior,
					// we can create an issuer uri from a baseIssuerUri,
					// this would be a keycloak instance holding all
					// the realms created during tenant provisioning.
					objectNode.put("issuerUri", baseIssuerUri + realmName);

					// Regarding the authorization check:
					//  - Datasource APIs were authorized for user with admin roles;
					//  - Searcher APIs were allowed to everyone.
					objectNode.set("routeAuthorizationMap", OBJ_MAPPER.createObjectNode()
						.put(Route.DATASOURCE.name(), Authorization.OAUTH2.name())
						.put(Route.SEARCHER.name(), Authorization.NO_AUTH.name())
					);

					var payload = objectNode.toString();

					BackfillEvent backfillEvent = new BackfillEvent(payload, createDate);

					events.add(backfillEvent);
				}
			}
		}

		return events;

	}

	private int insertBackfillEvents(JdbcConnection connection, List<BackfillEvent> backfillEvents)
		throws DatabaseException, SQLException {

		String insertSql = """
			INSERT
			INTO outbox_event (id, event_type, sent, payload, create_date)
			VALUES (?, ?, ?, ?, ?)
			""";

		int batchCount = 0;

		try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {

			for (BackfillEvent backfillEvent : backfillEvents) {

				long id = ID_GENERATOR.nextId();


				stmt.setLong(1, id);
				stmt.setString(2, EVENT_TYPE);
				stmt.setBoolean(3, false);

				// parameters relative to pre-existing tenant
				stmt.setString(4, backfillEvent.payload());
				stmt.setTimestamp(5, backfillEvent.createDate());

				stmt.addBatch();

				batchCount++;
			}

			if (batchCount > 0) {
				stmt.executeBatch();
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

		Objects.requireNonNull(
			baseIssuerUri, "a baseIssuerUri needs to be specified");

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
	private static final ObjectMapper OBJ_MAPPER = new ObjectMapper();

	private record BackfillEvent(String payload, Timestamp createDate) {}

}
