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
import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.tenantmanager.grpc.TenantListResponse;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantResponse;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.eventbus.EventBus;
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

	public void startUp(@Observes StartupEvent event) {
		eventBus.send("initialize_scheduler", "initialize_scheduler");
	}

	@ConsumeEvent(value = "initialize_scheduler")
	@ActivateRequestContext
	public Uni<Void> initScheduler(String testMessage) {

		Uni<TenantListResponse> tenantList =
			tenantManager.findTenantList(Empty.getDefaultInstance());

		logger.info("init scheduler");

		return tenantList
			.map(TenantListResponse::getTenantResponseList)
			.flatMap(list -> {
				List<Uni<Void>> unis = new ArrayList<>(list.size());
				for (TenantResponse tenantResponse : list) {
					String schemaName = tenantResponse.getSchemaName();
					tenantResolver.setTenant(schemaName);
					Uni<List<Datasource>> listDatasource = datasourceService.findAll();

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

	public Uni<List<Long>> triggerJobs(List<Long> datasourceIds) {

		List<Uni<Long>> triggers = new ArrayList<>(datasourceIds.size());

		for (long datasourceId : datasourceIds) {
			triggers.add(
				triggerJob(datasourceId, String.valueOf(datasourceId))
					.map(unused -> datasourceId)
			);
		}

		return Uni
			.combine()
			.all()
			.unis(triggers)
			.combinedWith(Long.class, Function.identity());

	}

	
	public Uni<Void> triggerJob(long datasourceId, String name) {

		return Uni.createFrom().deferred(() -> {
			logger.info("datasourceId: " + datasourceId + " trigger: " + name);
			return performTask(
				tenantResolver.getTenantName(), datasourceId);
		});

	}

	public void createOrUpdateScheduler(String tenantName, Datasource datasource) {

		schedulerInitializerActor.scheduleDataSource(
			tenantName, datasource);

	}

	public Uni<Void> performTask(String schemaName, Long datasourceId) {

		return transactionInvoker.withTransaction(
			schemaName,
			s -> datasourceService
				.findDatasourceByIdWithPluginDriver(datasourceId)
				.invoke(d -> schedulerInitializerActor.triggerDataSource(schemaName, d))
				.replaceWithVoid()
		);

	}

	public void deleteScheduler(Datasource datasource) {
		schedulerInitializerActor.unScheduleDataSource(
			tenantResolver.getTenantName(), datasource);
	}

	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	@Inject
	Logger logger;

	@Inject
	DatasourceService datasourceService;

	@Inject
	TenantResolver tenantResolver;

	@Inject
	EventBus eventBus;

	@Inject
	TransactionInvoker transactionInvoker;

	@Inject
	SchedulerInitializerActor schedulerInitializerActor;

}
