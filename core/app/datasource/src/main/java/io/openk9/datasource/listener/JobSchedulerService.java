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

package io.openk9.datasource.listener;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.quarkus.common.EventBusInstanceHolder;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.client.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverContext;
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.SchedulerService;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.Message;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@ApplicationScoped
public class JobSchedulerService {

	private static final String CALL_PLUGIN_DRIVER =
		"JobSchedulerService#callPluginDriver";
	private static final String CANCEL_SCHEDULER =
		"JobSchedulerService#cancelScheduler";
	private static final String FETCH_DATASOURCE_CONNECTION =
		"JobSchedulerService#fetchDatasourceConnection";
	private static final String PERSIST_SCHEDULER =
		"JobSchedulerService#persistScheduler";
	private static final String TRIGGER_DATASOURCE =
		"JobScheduler#triggerDatasource";
	private static final Logger log =
		Logger.getLogger(JobSchedulerService.class);
	@Inject
	DataIndexService dataIndexService;
	@Inject
	HttpPluginDriverClient httpPluginDriverClient;
	@Inject
	SchedulerService schedulerService;
	@Inject
	Mutiny.SessionFactory sessionFactory;

	public static CompletableFuture<Void> callHttpPluginDriverClient(
		String tenantName, Scheduler scheduler,
		OffsetDateTime lastIngestionDate) {

		var request = new CallHttpPluginDriverRequest(
			tenantName,
			scheduler,
			lastIngestionDate
		);

		return EventBusInstanceHolder
			.<Void>request(CALL_PLUGIN_DRIVER, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<Void> cancelScheduler(
		String tenantId, long schedulerId) {

		var request = new CancelSchedulerRequest(tenantId, schedulerId);

		return EventBusInstanceHolder
			.<Void>request(CANCEL_SCHEDULER, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<Datasource> fetchDatasourceConnection(
		String tenantId, long datasourceId) {

		var request = new FetchDatasourceConnectionRequest(
			tenantId,
			datasourceId
		);

		return EventBusInstanceHolder
			.<Datasource>request(FETCH_DATASOURCE_CONNECTION, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<TriggerType> getTriggerType(
		String tenantId, Datasource datasource, boolean reindex) {

		var request = new TriggerDatasourceRequest(
			tenantId, datasource, reindex);

		return EventBusInstanceHolder
			.<TriggerType>request(TRIGGER_DATASOURCE, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<Scheduler> persistScheduler(
		String tenantId, Scheduler scheduler) {

		var request = new PersistSchedulerRequest(tenantId, scheduler);

		return EventBusInstanceHolder
			.<Scheduler>request(PERSIST_SCHEDULER, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	@ConsumeEvent(CALL_PLUGIN_DRIVER)
	Uni<Void> callPluginDriver(CallHttpPluginDriverRequest request) {

		var tenantName = request.tenantName();
		var scheduler = request.scheduler();
		var lastIngestionDate = request.lastIngestionDate();
		var datasource = scheduler.getDatasource();
		var pluginDriver = datasource.getPluginDriver();

		var resourceUri = pluginDriver.getResourceUri();

		return httpPluginDriverClient.invoke(
			resourceUri,
			HttpPluginDriverContext
				.builder()
				.timestamp(lastIngestionDate)
				.tenantId(tenantName)
				.datasourceId(datasource.getId())
				.scheduleId(scheduler.getScheduleId())
				.datasourceConfig(new JsonObject(datasource.getJsonConfig()).getMap())
				.build()
		).replaceWithVoid();

	}

	@ConsumeEvent(CANCEL_SCHEDULER)
	Uni<Void> cancelScheduler(CancelSchedulerRequest request) {

		var tenantId = request.tenantId();
		var schedulerId = request.schedulerId();

		log.warnf(
			"Trying to cancel the Scheduler with id %s to start Reindex",
			schedulerId);

		return schedulerService.cancelScheduling(tenantId, schedulerId);
	}

	@ConsumeEvent(FETCH_DATASOURCE_CONNECTION)
	Uni<Datasource> fetchDatasourceConnection(
		FetchDatasourceConnectionRequest request) {

		return sessionFactory.withTransaction(
			request.tenantId(), (s, t) ->
				s.createQuery(
						"select d " +
						"from Datasource d " +
						"join fetch d.pluginDriver " +
						"left join fetch d.dataIndex di " +
						"left join fetch di.embeddingDocTypeField edtf " +
						"left join fetch di.docTypes " +
						"where d.id = :id", Datasource.class)
					.setParameter("id", request.datasourceId())
					.getSingleResult()
		);
	}

	@ConsumeEvent(PERSIST_SCHEDULER)
	Uni<Scheduler> persistScheduler(PersistSchedulerRequest request) {

		return sessionFactory.withTransaction(request.tenantId(), (s, t) -> {

			var scheduler = request.scheduler();
			var newDataIndex = scheduler.getNewDataIndex();

			if (newDataIndex == null) {
				return s.persist(scheduler).map(unused -> scheduler);
			}

			// the dataIndex service owns the creation of every dataIndex, and
			// it must happen in the session the Scheduler is persisted in
			return dataIndexService.create(s, newDataIndex)
				.invoke(scheduler::setNewDataIndex)
				.call(() -> s.persist(scheduler))
				.map(unused -> scheduler);
		});
	}

	@ConsumeEvent(TRIGGER_DATASOURCE)
	Uni<TriggerType> triggerDatasource(TriggerDatasourceRequest request) {

		var tenantId = request.tenantId();
		var datasource = request.datasource();
		var reindex = request.reindex();

		return sessionFactory.withTransaction(
			tenantId,
			(s, t) -> s.createQuery(
					"select s " +
					"from Scheduler s " +
					"where s.datasource.id = :datasourceId " +
					"and s.status in :runningStates",
					Scheduler.class
				)
				.setParameter("datasourceId", datasource.getId())
				.setParameter("runningStates", Scheduler.RUNNING_STATES_SET)
				.getSingleResultOrNull()
				.flatMap(runningScheduler -> {

					if (runningScheduler != null) {

						if (reindex && !runningScheduler.isReindex()) {

							return Uni.createFrom().item(
								new TriggerType.TriggerTypeReindex(
									TriggerType.SimpleTriggerType.REINDEX, runningScheduler));
						}
						else {
							log.warnf(
								"A Scheduler with id %s for datasource %s is %s",
								runningScheduler.getId(),
								datasource.getId(),
								runningScheduler.getStatus()
							);

							return Uni.createFrom().item(TriggerType.SimpleTriggerType.IGNORE);
						}
					}

					if (reindex != null) {
						return Uni.createFrom().item(reindex
							? TriggerType.SimpleTriggerType.REINDEX
							: TriggerType.SimpleTriggerType.TRIGGER);
					}
					else {
						return Uni.createFrom().item(TriggerType.SimpleTriggerType.TRIGGER);
					}

				})
		);

	}

	private record CallHttpPluginDriverRequest(
		String tenantName, Scheduler scheduler, OffsetDateTime lastIngestionDate
	) {}

	private record CancelSchedulerRequest(String tenantId, long schedulerId) {}

	private record FetchDatasourceConnectionRequest(
		String tenantId, long datasourceId
	) {}

	private record PersistSchedulerRequest(
		String tenantId, Scheduler scheduler
	) {}

	private record TriggerDatasourceRequest(
		String tenantId, Datasource datasource, Boolean reindex
	) {}

}