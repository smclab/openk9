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

import com.google.protobuf.Empty;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.SchedulationKeyUtils;
import io.openk9.datasource.pipeline.actor.MessageGateway;
import io.openk9.datasource.pipeline.actor.Schedulation;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.tenantmanager.grpc.TenantListResponse;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantResponse;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@ApplicationScoped
public class SchedulerInitializer {

	private static final String INITIALIZE_SCHEDULER = "initialize_scheduler";
	private static final String SPAWN_CONSUMERS = "spawn_consumers";

	public void startUp(@Observes StartupEvent event) {
		eventBus.send(INITIALIZE_SCHEDULER, INITIALIZE_SCHEDULER);
		eventBus.send(SPAWN_CONSUMERS, SPAWN_CONSUMERS);
	}

	@ConsumeEvent(value = INITIALIZE_SCHEDULER)
	@ActivateRequestContext
	public Uni<Void> initScheduler(String message) {
		logger.info("init scheduler");

		return getTenantList()
			.flatMap(tenantList -> {
				List<Uni<Void>> unis = new ArrayList<>(tenantList.size());
				for (TenantResponse tenantResponse : tenantList) {
					String schemaName = tenantResponse.getSchemaName();
					Uni<List<Datasource>> listDatasource = datasourceService.findAll(schemaName);

					Uni<Void> voidUni = listDatasource
						.onItemOrFailure()
						.invoke(Unchecked.consumer((tenantDatasourceList, t) -> {

							if (t != null) {
								logger.error(
									"error createOrUpdateScheduler in schema: " + schemaName, t);
								return;
							}

							for (Datasource datasource : tenantDatasourceList) {
								try {
									logger.info("start datasource: " + datasource.getName() + " schema " + schemaName);
									createOrUpdateScheduler(schemaName, datasource);
									logger.info("end   datasource: " + datasource.getName() + " schema " + schemaName);
								}
								catch (Exception e) {
									logger.error(
										"error createOrUpdateScheduler in schema: " +
										schemaName + " datasource.name: " + datasource.getName(), e);
								}
							}
						}))
						.replaceWithVoid();

					unis.add(voidUni);

				}

				return Uni
					.join()
					.all(unis)
					.andCollectFailures()
					.replaceWithVoid();

			});

	}

	public Uni<List<Long>> triggerJobs(String tenantName, List<Long> datasourceIds) {
		return triggerJobs(tenantName, datasourceIds, false);
	}

	public Uni<List<Long>> triggerJobs(String tenantName, List<Long> datasourceIds, Boolean startFromFirst) {

		List<Uni<Long>> triggers = new ArrayList<>(datasourceIds.size());

		for (long datasourceId : datasourceIds) {
			triggers.add(
				triggerJob(tenantName, datasourceId, String.valueOf(datasourceId), startFromFirst)
					.map(unused -> datasourceId)
			);
		}

		return Uni
			.combine()
			.all()
			.unis(triggers)
			.combinedWith(Long.class, Function.identity());

	}

	
	public Uni<Void> triggerJob(
		String tenantName, long datasourceId, String name, Boolean startFromFirst) {

		return Uni.createFrom().deferred(() -> {
			logger.info("datasourceId: " + datasourceId + " trigger: " + name + " startFromFirst: " + startFromFirst);
			return performTask(
				tenantName, datasourceId, startFromFirst);
		});

	}

	public void createOrUpdateScheduler(String tenantName, Datasource datasource) {

		schedulerInitializerActor.scheduleDataSource(
			tenantName, datasource.getId(), datasource.getSchedulable(),
			datasource.getScheduling());

	}

	public Uni<Void> performTask(
		String schemaName, Long datasourceId, Boolean startFromFirst) {

		return sessionFactory.withTransaction(
			schemaName,
			(s, t) -> datasourceService
				.findDatasourceByIdWithPluginDriver(datasourceId)
				.invoke(d -> schedulerInitializerActor.triggerDataSource(schemaName, d.getId(), startFromFirst))
				.replaceWithVoid()
		);

	}

	public void deleteScheduler(String tenantId, Datasource datasource) {
		schedulerInitializerActor.unScheduleDataSource(tenantId, datasource.getId());
	}

	@ConsumeEvent(value = SPAWN_CONSUMERS)
	@ActivateRequestContext
	public Uni<Void> spawnConsumers(String message) {
		return getTenantList()
			.flatMap(tenantList -> {
				List<Uni<List<Scheduler>>> registrations = new ArrayList<>();

				for (TenantResponse tenantResponse : tenantList) {
					Uni<List<Scheduler>> registration = sessionFactory.withTransaction(
							tenantResponse.getSchemaName(),
							(session, transaction) -> session
								.createNamedQuery(Scheduler.FETCH_RUNNING_QUERY, Scheduler.class)
								.getResultList()
						)
						.invoke(schedulers -> {
							for (Scheduler scheduler : schedulers) {
								Schedulation.SchedulationKey schedulationKey =
									SchedulationKeyUtils.getKey(
										tenantResponse.getSchemaName(),
										scheduler.getScheduleId()
									);

								MessageGateway.askRegister(
									actorSystemProvider.getActorSystem(),
									schedulationKey
								);
							}
						});

					registrations.add(registration);
				}

				return Uni.join()
					.all(registrations)
					.andCollectFailures()
					.replaceWithVoid();
			});
	}

	private Uni<List<TenantResponse>> getTenantList() {
		return tenantManager
			.findTenantList(Empty.getDefaultInstance())
			.map(TenantListResponse::getTenantResponseList);
	}

	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	@Inject
	Logger logger;

	@Inject
	DatasourceService datasourceService;

	@Inject
	EventBus eventBus;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	SchedulerInitializerActor schedulerInitializerActor;

	@Inject
	ActorSystemProvider actorSystemProvider;
}
