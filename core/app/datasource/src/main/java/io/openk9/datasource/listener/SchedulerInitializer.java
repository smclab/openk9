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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.MessageGateway;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.web.dto.TriggerWithDateResourceDTO;
import io.openk9.tenantmanager.grpc.TenantListResponse;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantResponse;

import com.google.protobuf.Empty;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SchedulerInitializer {

	public static final String DELETE_SCHEDULER = "delete_scheduler";
	public static final String UPDATE_SCHEDULER = "update_scheduler";
	private static final String INITIALIZE_SCHEDULER = "initialize_scheduler";
	private static final String SPAWN_CONSUMERS = "spawn_consumers";
	@Inject
	ActorSystemProvider actorSystemProvider;
	@Inject
	DatasourceService datasourceService;
	@Inject
	EventBus eventBus;
	@Inject
	Logger logger;
	@Inject
	SchedulerInitializerActor schedulerInitializerActor;
	@Inject
	Mutiny.SessionFactory sessionFactory;
	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	@ConsumeEvent(UPDATE_SCHEDULER)
	public Uni<Void> createOrUpdateScheduler(Datasource datasource) {

		return schedulerInitializerActor
			.scheduleDataSource(
				datasource.getTenant(),
				datasource.getId(),
				datasource.getSchedulable(),
				datasource.getScheduling(),
				datasource.getReindexable(),
				datasource.getReindexing(),
				datasource.getPurgeable(),
				datasource.getPurging(),
				datasource.getPurgeMaxAge()
			);
	}

	@ConsumeEvent(DELETE_SCHEDULER)
	public Uni<Void> deleteScheduler(Datasource datasource) {

		return schedulerInitializerActor.unScheduleDataSource(
			datasource.getTenant(),
			datasource.getId()
		);
	}

	@ConsumeEvent(value = INITIALIZE_SCHEDULER)
	@ActivateRequestContext
	public Uni<Void> initScheduler(String ignore) {

		return getTenantList()
			.flatMap(this::getScheduleDatasourceCommands)
			.flatMap(schedulerInitializerActor::initJobScheduler);

	}

	public Uni<Void> performTask(
			String schemaName, Long datasourceId, Boolean reindex,
			OffsetDateTime startIngestionDate) {

		return sessionFactory.withTransaction(
			schemaName,
			(s, t) -> datasourceService
				.findByIdWithPluginDriver(datasourceId)
				.flatMap(d -> schedulerInitializerActor.triggerDataSource(
					schemaName, d.getId(), reindex, startIngestionDate))
		);

	}

	@ConsumeEvent(value = SPAWN_CONSUMERS)
	@ActivateRequestContext
	public Uni<Void> spawnConsumers(String ignore) {

		return getTenantList()
			.flatMap(tenantList -> {
				List<Uni<List<Scheduler>>> registrations = new ArrayList<>();

				for (TenantResponse tenantResponse : tenantList) {
					Uni<List<Scheduler>> registration = sessionFactory.withTransaction(
							tenantResponse.getSchemaName(),
							(session, transaction) -> session
								.createNamedQuery(Scheduler.FETCH_RUNNING, Scheduler.class)
								.getResultList()
						)
						.invoke(schedulers -> {
							for (Scheduler scheduler : schedulers) {
								ShardingKey shardingKey =
									ShardingKey.fromStrings(
										tenantResponse.getSchemaName(),
										scheduler.getScheduleId()
									);

								MessageGateway.askRegister(
									actorSystemProvider.getActorSystem(),
									shardingKey
								);
							}
						});

					registrations.add(registration);
				}

				return Uni.join()
					.all(registrations)
					.usingConcurrencyOf(1)
					.andCollectFailures()
					.replaceWithVoid();
			});
	}

	@ConsumeEvent(value = ActorSystemProvider.INITIALIZED)
	public void startUp(String ignore) {
		eventBus.send(INITIALIZE_SCHEDULER, INITIALIZE_SCHEDULER);
		eventBus.send(SPAWN_CONSUMERS, SPAWN_CONSUMERS);
	}

	public Uni<Void> triggerJob(
			String tenantName, long datasourceId, String name, Boolean reindex,
			OffsetDateTime startIngestionDate) {

		return Uni.createFrom().deferred(() -> {
			logger.info("datasourceId: " + datasourceId + " trigger: " + name + " reindex: " + reindex);
			return performTask(
				tenantName, datasourceId, reindex, startIngestionDate);
		});

	}

	public Uni<List<Long>> triggerJobs(String tenantName, TriggerWithDateResourceDTO dto) {

		var datasourceIds = dto.getDatasourceIds();
		var reindex = dto.isReindex();
		var startIngestionDate = dto.getStartIngestionDate();
		List<Uni<Long>> triggers = new ArrayList<>(datasourceIds.size());

		for (long datasourceId : datasourceIds) {
			triggers.add(
				triggerJob(tenantName, datasourceId, String.valueOf(datasourceId), reindex,
						startIngestionDate)
					.map(unused -> datasourceId)
			);
		}

		return Uni.combine()
			.all()
			.unis(triggers)
			.usingConcurrencyOf(1)
			.with(Long.class, Function.identity());

	}

	private Uni<? extends List<JobScheduler.ScheduleDatasource>> getScheduleDatasourceCommands(
		List<TenantResponse> tenantResponses) {

		logger.info("fetching datasources...");

		return Uni.join()
			.all(tenantResponses
				.stream()
				.map(TenantResponse::getSchemaName)
				.map(datasourceService::findAll)
				.collect(Collectors.toList())
			)
			.usingConcurrencyOf(1)
			.andCollectFailures()
			.map(resultSets -> resultSets
				.stream()
				.flatMap(Collection::stream)
				.map(this::mapScheduleDatasource)
				.collect(Collectors.toList())
			);
	}

	private Uni<List<TenantResponse>> getTenantList() {
		return tenantManager
			.findTenantList(Empty.getDefaultInstance())
			.map(TenantListResponse::getTenantResponseList);
	}

	private JobScheduler.ScheduleDatasource mapScheduleDatasource(Datasource datasource) {
		return new JobScheduler.ScheduleDatasource(
			datasource.getTenant(),
			datasource.getId(),
			datasource.getSchedulable(),
			datasource.getScheduling(),
			datasource.getReindexable(),
			datasource.getReindexing(),
			datasource.getPurgeable(),
			datasource.getPurging(),
			datasource.getPurgeMaxAge());
	}

}
