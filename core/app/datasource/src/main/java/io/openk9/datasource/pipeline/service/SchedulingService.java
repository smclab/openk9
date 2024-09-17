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

package io.openk9.datasource.pipeline.service;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.actor.EventBusInstanceHolder;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.service.mapper.SchedulerMapper;
import io.openk9.datasource.service.SchedulerService;
import io.quarkus.runtime.util.ExceptionUtil;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SchedulingService {

	private final static String FETCH_SCHEDULER = "SchedulingService#fetchScheduler";
	private final static String PERSIST_STATUS = "SchedulingService#persistStatus";
	private final static String PERSIST_LAST_INGESTION_DATE =
		"SchedulingService#persistLastIngestionDate";
	private final static String PERSIST_ERROR_DESCRIPTION =
		"SchedulingService#persistErrorDescription";
	private static final String GET_DELETED_CONTENT_ID = "SchedulingService#getDeletedContentId";

	@Inject
	Mutiny.SessionFactory sessionFactory;
	@Inject
	SchedulerMapper schedulerMapper;
	@Inject
	SchedulerService schedulerService;

	public static CompletableFuture<SchedulerDTO> fetchScheduler(ShardingKey shardingKey) {

		return EventBusInstanceHolder.getEventBus()
			.request(FETCH_SCHEDULER, new FetchRequest(shardingKey))
			.map(message -> (SchedulerDTO) message.body())
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<SchedulerDTO> persistStatus(
		ShardingKey shardingKey, Scheduler.SchedulerStatus status) {

		return EventBusInstanceHolder.getEventBus()
			.request(PERSIST_STATUS, new PersistStatusRequest(shardingKey, status))
			.map(message -> (SchedulerDTO) message.body())
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<SchedulerDTO> persistLastIngestionDate(
		ShardingKey shardingKey, OffsetDateTime lastIngestionDate) {

		return EventBusInstanceHolder.getEventBus()
			.request(
				PERSIST_LAST_INGESTION_DATE,
				new PersistLastIngestionDateRequest(shardingKey, lastIngestionDate)
			)
			.map(message -> (SchedulerDTO) message.body())
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<SchedulerDTO> persistErrorDescription(
		ShardingKey shardingKey, Exception exception) {

		return EventBusInstanceHolder.getEventBus()
			.request(
				PERSIST_ERROR_DESCRIPTION,
				new PersistErrorDescription(shardingKey, exception)
			)
			.map(message -> (SchedulerDTO) message.body())
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<List<String>> getDeletedContentIds(ShardingKey shardingKey) {

		return EventBusInstanceHolder.getEventBus()
			.request(
				GET_DELETED_CONTENT_ID,
				new GetDeletedContentIdRequest(shardingKey)
			)
			.map(message -> (List<String>) message.body())
			.subscribeAsCompletionStage();
	}

	@ConsumeEvent(FETCH_SCHEDULER)
	Uni<SchedulerDTO> fetchScheduler(FetchRequest request) {

		var schedulingKey = request.shardingKey();
		var tenantId = schedulingKey.tenantId();
		var scheduleId = schedulingKey.scheduleId();

		return sessionFactory.withSession(tenantId, (s) -> {
				s.setDefaultReadOnly(true);
				return doFetchScheduler(s, scheduleId);
			})
			.map(schedulerMapper::map);
	}

	@ConsumeEvent(PERSIST_STATUS)
	Uni<SchedulerDTO> persistStatus(PersistStatusRequest request) {

		var schedulingKey = request.shardingKey();
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

	@ConsumeEvent(PERSIST_LAST_INGESTION_DATE)
	Uni<SchedulerDTO> persistLastIngestionDate(
		PersistLastIngestionDateRequest request) {

		var schedulingKey = request.shardingKey();
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

	@ConsumeEvent(PERSIST_ERROR_DESCRIPTION)
	Uni<SchedulerDTO> persistErrorDescription(PersistErrorDescription request) {

		var schedulingKey = request.shardingKey();
		var tenantId = schedulingKey.tenantId();
		var scheduleId = schedulingKey.scheduleId();
		var exception = request.exception();

		return sessionFactory.withTransaction(tenantId, (s, tx) -> doFetchScheduler(
				s, scheduleId)
				.flatMap(entity -> {
					var errorDescription = ExceptionUtil.generateStackTrace(exception);
					entity.setErrorDescription(errorDescription);

					return s.merge(entity);
				})
			)
			.map(schedulerMapper::map);
	}

	@ConsumeEvent(GET_DELETED_CONTENT_ID)
	Uni<List<String>> getDeletedContentId(GetDeletedContentIdRequest request) {
		var schedulingKey = request.shardingKey();

		return schedulerService.getDeletedContentIds(
			schedulingKey.tenantId(), schedulingKey.scheduleId());
	}

	private Uni<Scheduler> doFetchScheduler(Mutiny.Session s, String scheduleId) {

		return s
			.createNamedQuery(Scheduler.FETCH_BY_SCHEDULE_ID, Scheduler.class)
			.setParameter("scheduleId", scheduleId)
			.setPlan(s.getEntityGraph(Scheduler.class, Scheduler.ENRICH_ITEMS_ENTITY_GRAPH))
			.getSingleResult();
	}

	private record FetchRequest(ShardingKey shardingKey) {}

	private record PersistStatusRequest(
		ShardingKey shardingKey,
		Scheduler.SchedulerStatus status
	) {}

	private record PersistLastIngestionDateRequest(
		ShardingKey shardingKey,
		OffsetDateTime lastIngestionDate
	) {}

	private record PersistErrorDescription(
		ShardingKey shardingKey,
		Exception exception
	) {}

	private record GetDeletedContentIdRequest(ShardingKey shardingKey) {}

}