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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.util.CompactSnowflakeIdGenerator;
import io.openk9.event.tenant.TenantManagementEvent;
import io.openk9.tenantmanager.model.OutboxEvent;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OutboxEventService {

	public Uni<Void> persist(TenantManagementEvent event) {
		Objects.requireNonNull(event);

		long eventId = idGenerator.nextId();
		String eventType = event.getClass().getSimpleName();
		String payload = Json.encode(event);

		return persist(eventId, eventType, payload, false, OffsetDateTime.now());
	}

	public Uni<Void> persist(
		long id,
		String eventType,
		String payload,
		boolean sent,
		OffsetDateTime createDate) {

		return pool.withTransaction(sqlConnection -> sqlConnection
			.preparedQuery(INSERT_NEW_EVENT_SQL)
			.execute(Tuple.of(id, eventType, payload, sent, createDate))
			.replaceWithVoid()
		);
	}

	public Uni<Void> flagAsSent(long id) {
		return pool.withTransaction(sqlConnection -> sqlConnection
			.preparedQuery(UPDATE_SENT_FLAG_SQL)
			.execute(Tuple.of(id))
		).replaceWithVoid();
	}

	public Uni<Void> flagAsSent(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return Uni.createFrom().voidItem();
		}

		List<Tuple> tuples = new ArrayList<>();
		for (long id : ids) {
			tuples.add(Tuple.of(id));
		}

		return pool.withTransaction(sqlConnection -> sqlConnection
			.preparedQuery(UPDATE_SENT_FLAG_SQL)
			.executeBatch(tuples)
		).replaceWithVoid();
	}

	public Uni<List<OutboxEvent>> unsentEvents() {
		return pool.preparedQuery(FETCH_UNSENT_SQL)
			.execute()
			.map(OutboxEventService::fromRowSet);
	}

	public Uni<List<OutboxEvent>> window(OffsetDateTime from, OffsetDateTime to) {
		return pool.preparedQuery(FETCH_WINDOW_SQL)
			.execute(Tuple.of(from, to))
			.map(OutboxEventService::fromRowSet);
	}

	public Uni<List<OutboxEvent>> window(OffsetDateTime from) {
		return window(from, OffsetDateTime.now());
	}

	public Uni<List<OutboxEvent>> lastEvents(int n) {
		return pool.preparedQuery(FETCH_LAST_SQL)
			.execute(Tuple.of(n))
			.map(OutboxEventService::fromRowSet);
	}

	private static List<OutboxEvent> fromRowSet(RowSet<Row> rows) {
		List<OutboxEvent> events = new ArrayList<>();
		for (Row row : rows) {
			events.add(OutboxEventService.fromRow(row));
		}
		return events;
	}

	private static OutboxEvent fromRow(Row row) {
		return OutboxEvent.builder()
			.id(row.getLong("id"))
			.eventType(row.getString("event_type"))
			.payload(row.getString("payload"))
			.sent(row.getBoolean("sent"))
			.createDate(row.getLocalDateTime("create_date"))
			.build();
	}

	@Inject
	Pool pool;

	private static final String INSERT_NEW_EVENT_SQL = """
		INSERT
		INTO outbox_event (id, event_type, payload, sent, create_date)
		VALUES ($1, $2, $3, $4, $5)
		""";

	private static final String FETCH_UNSENT_SQL = """
		SELECT *
		FROM outbox_event
		WHERE sent = false
		ORDER BY create_date ASC
		""";

	private static final String FETCH_WINDOW_SQL = """
		SELECT *
		FROM outbox_event
		WHERE create_date >= $1 and create_date <= $2
		ORDER BY create_date ASC
		""";

	private static final String FETCH_LAST_SQL = """
		SELECT *
		FROM outbox_event
		ORDER BY create_date DESC
		LIMIT $1
		""";

	private static final String UPDATE_SENT_FLAG_SQL = """
		UPDATE outbox_event
		SET sent = true
		WHERE id = $1
		""";

	private static final CompactSnowflakeIdGenerator idGenerator = new CompactSnowflakeIdGenerator();
	private static final Logger log = Logger.getLogger(OutboxEventService.class);
}
