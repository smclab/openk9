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

package io.openk9.datasource.pipeline.actor;

import io.openk9.common.util.SchedulingKey;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.actor.mapper.SchedulerMapper;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

@ApplicationScoped
public class SchedulingService {

	private final static String ADDRESS = SchedulingService.class.getName();
	private final static Logger log = Logger.getLogger(SchedulingService.class);
	@Inject
	Mutiny.SessionFactory sessionFactory;
	@Inject
	SchedulerMapper schedulerMapper;

	public static CompletableFuture<SchedulerDTO> fetchScheduler(SchedulingKey schedulingKey) {

		var eventBus = CDI.current().select(EventBus.class).get();

		return eventBus.request(ADDRESS, new FetchRequest(schedulingKey))
			.map(message -> (SchedulerDTO) message.body())
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<SchedulerDTO> persistStatus(
		SchedulingKey schedulingKey, Scheduler.SchedulerStatus status) {

		var eventBus = CDI.current().select(EventBus.class).get();

		return eventBus.request(ADDRESS, new PersistStatusRequest(schedulingKey, status))
			.map(message -> (SchedulerDTO) message.body())
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<SchedulerDTO> persistLastIngestionDate(
		SchedulingKey schedulingKey, OffsetDateTime lastIngestionDate) {

		var eventBus = CDI.current().select(EventBus.class).get();

		return eventBus.request(
				ADDRESS,
				new PersistLastIngestionDateRequest(schedulingKey, lastIngestionDate)
			)
			.map(message -> (SchedulerDTO) message.body())
			.subscribeAsCompletionStage();
	}

	@ConsumeEvent
	Uni<SchedulerDTO> _fetchScheduler(FetchRequest request) {

		var schedulingKey = request.schedulingKey();
		var tenantId = schedulingKey.tenantId();
		var scheduleId = schedulingKey.scheduleId();

		return sessionFactory.withSession(tenantId, (s) -> {
				s.setDefaultReadOnly(true);
				return doFetchScheduler(s, scheduleId);
			})
			.map(schedulerMapper::map);
	}

	@ConsumeEvent
	Uni<SchedulerDTO> _persistStatus(
		PersistStatusRequest request) {

		var schedulingKey = request.schedulingKey();
		var tenantId = schedulingKey.tenantId();
		var scheduleId = schedulingKey.scheduleId();
		var status = request.status();

		return sessionFactory.withTransaction(tenantId, (s, tx) -> doFetchScheduler(
				s, scheduleId)
				.flatMap(entity -> {
					entity.setStatus(status);
					return s.merge(entity);
				})
			)
			.map(schedulerMapper::map);
	}

	@ConsumeEvent
	Uni<SchedulerDTO> _persistLastIngestionDate(
		PersistLastIngestionDateRequest request) {

		var schedulingKey = request.schedulingKey();
		var tenantId = schedulingKey.tenantId();
		var scheduleId = schedulingKey.scheduleId();
		var lastIngestionDate = request.lastIngestionDate();

		return sessionFactory.withTransaction(tenantId, (s, tx) -> doFetchScheduler(
				s, scheduleId)
				.flatMap(entity -> {
					entity.setLastIngestionDate(lastIngestionDate);
					return s.merge(entity);
				})
			)
			.map(schedulerMapper::map);
	}

	private Uni<Scheduler> doFetchScheduler(Mutiny.Session s, String scheduleId) {

		return s
			.createNamedQuery(Scheduler.FETCH_BY_SCHEDULE_ID, Scheduler.class)
			.setParameter("scheduleId", scheduleId)
			.setPlan(s.getEntityGraph(Scheduler.class, Scheduler.ENRICH_ITEMS_ENTITY_GRAPH))
			.getSingleResult();
	}

	public record FetchRequest(SchedulingKey schedulingKey) {}

	public record PersistStatusRequest(
		SchedulingKey schedulingKey,
		Scheduler.SchedulerStatus status
	) {}

	public record PersistLastIngestionDateRequest(
		SchedulingKey schedulingKey,
		OffsetDateTime lastIngestionDate
	) {}

}