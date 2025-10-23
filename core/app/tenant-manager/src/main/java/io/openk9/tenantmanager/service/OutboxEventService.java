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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.event.tenant.TenantManagementEvent;
import io.openk9.tenantmanager.messaging.Producer;
import io.openk9.tenantmanager.model.OutboxEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.reactivestreams.FlowAdapters;

@ApplicationScoped
public class OutboxEventService {

	private final ObjectMapper mapper = new ObjectMapper();

	public Uni<Void> persist(TenantManagementEvent event) {
		return sessionFactory.withTransaction((s) -> persist(s, event));
	}

	public Uni<Void> persist(Mutiny.Session session, TenantManagementEvent event) {
		String payload = null;
		try {
			payload = mapper.writeValueAsString(event);
		}
		catch (JsonProcessingException e) {
			return Uni.createFrom().failure(e);
		}

		String eventType = event.getClass().getSimpleName();

		OutboxEvent outboxEvent = new OutboxEvent();
		outboxEvent.setEventType(eventType);
		outboxEvent.setPayload(payload);
		outboxEvent.setSent(false);
		outboxEvent.setCreateDate(Instant.now());

		return persist(session, outboxEvent);
	}

	public Uni<Void> persist(OutboxEvent outboxEvent) {
		return sessionFactory.withTransaction((s) -> persist(s, outboxEvent));
	}

	public Uni<Void> persist(Mutiny.Session session, OutboxEvent outboxEvent) {
		return session.persist(outboxEvent);
	}

	public Uni<Void> merge(OutboxEvent outboxEvent) {
		return sessionFactory.withTransaction(session ->
				session.merge(outboxEvent)
			).replaceWithVoid();
	}

	public Uni<List<OutboxEvent>> unsentEvents() {
		return sessionFactory.withSession(this::unsentEvents);
	}

	public Uni<List<OutboxEvent>> unsentEvents(Mutiny.Session session) {
		log.info("polling...");
		return session
			.createQuery(
				"from OutboxEvent e where e.sent is null or e.sent = false",
				OutboxEvent.class
			).getResultList()
			.invoke((events) -> log.infof("# events: %d", events.size()));
	}

	@Scheduled(every = "5s")
	public Uni<Void> emits() {

		return unsentEvents()
			.chain(outboxEvents -> {
				log.infof("sending %d events...", outboxEvents.size());
				List<Uni<Void>> unis = new ArrayList<>();

				for(OutboxEvent event : outboxEvents) {
					unis.add(
						Uni.createFrom().publisher(
							FlowAdapters.toFlowPublisher(
								producer.send(
									event.getEventType(),
									event.getPayload().getBytes(StandardCharsets.UTF_8)
								)
							)
						).chain(() -> {
							event.setSent(true);
							return merge(event);
						})
					);
				}

				return Uni.join().all(unis).andCollectFailures();

			}).replaceWithVoid();

	}

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	Producer producer;

	private static final Logger log = Logger.getLogger(OutboxEventService.class);
}
