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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.util.CompactSnowflakeIdGenerator;
import io.openk9.event.tenant.TenantManagementEvent;
import io.openk9.tenantmanager.model.OutboxEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OutboxEventService {

	public Uni<Void> persist(TenantManagementEvent event) {
		Tuple tuple = asTuple(event);

		return pool.withTransaction(sqlConnection -> sqlConnection
			.preparedQuery(INSERT_NEW_EVENT_SQL)
			.execute(tuple)
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
			.map(rows -> {
				List<OutboxEvent> events = new ArrayList<>();
				for (Row row : rows) {
					events.add(OutboxEventService.fromRow(row));
				}
				return events;
			});
	}

	private static Tuple asTuple(TenantManagementEvent event) {
		try {
			String payload = mapper.writeValueAsString(event);

			return Tuple.of(
				idGenerator.nextId(),
				event.getClass().getSimpleName(),
				payload,
				false,
				OffsetDateTime.now());
		}
		catch (JsonProcessingException e) {
			if (log.isDebugEnabled()) {
				log.errorf(e, "Error while serializing event %s as json string.", event);
			}

			throw new RuntimeException(e);
		}
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
		""";

	private static final String UPDATE_SENT_FLAG_SQL = """
		UPDATE outbox_event
		SET sent = true
		WHERE id = $1
		""";

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final CompactSnowflakeIdGenerator idGenerator = new CompactSnowflakeIdGenerator();
	private static final Logger log = Logger.getLogger(OutboxEventService.class);
}
