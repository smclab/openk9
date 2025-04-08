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
import java.util.List;
import java.util.function.Supplier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.datasource.actor.ActorSystemProvider;

import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.cluster.typed.ClusterSingleton;
import org.apache.pekko.cluster.typed.SingletonActor;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SchedulerInitializerActor {

	private static final Logger log = Logger.getLogger(SchedulerInitializerActor.class);
	@Inject
	ActorSystemProvider actorSystemProvider;

	private List<JobScheduler.ScheduleDatasource> schedulatedJobs;

	public Uni<Void> initJobScheduler(List<JobScheduler.ScheduleDatasource> schedulatedJobs) {
		this.schedulatedJobs = schedulatedJobs;
		return getScheduleRef(() -> null);
	}

	public Uni<Void> scheduleDataSource(
			String tenantName, long datasourceId, boolean schedulable, String schedulingCron,
			boolean reindexable, String reindexingCron, boolean purgeable, String purging,
			String purgeMaxAge) {

		return getScheduleRef(() ->
			new JobScheduler.ScheduleDatasource(tenantName, datasourceId, schedulable,
				schedulingCron, reindexable, reindexingCron, purgeable, purging, purgeMaxAge));
	}

	public Uni<Void> triggerDataSource(
			String tenantName, long datasourceId, Boolean reindex,
			OffsetDateTime startIngestionDate) {

		return getScheduleRef(() ->
			new JobScheduler.TriggerDatasource(
				tenantName, datasourceId, reindex, startIngestionDate));
	}
	
	public Uni<Void> unScheduleDataSource(String tenantName, long datasourceId) {
		return getScheduleRef(() ->
			new JobScheduler.UnScheduleDatasource(tenantName, datasourceId));
	}

	private Uni<Void> getScheduleRef(Supplier<JobScheduler.Command> commandSupplier) {

		return VertxContextSupport.executeBlocking(() -> {
				try {
					ActorRef<JobScheduler.Command> actorRef = ClusterSingleton
						.get(actorSystemProvider.getActorSystem())
						.init(
							SingletonActor.of(
								JobScheduler.create(
									schedulatedJobs != null
										? schedulatedJobs
										: List.of()
								),
								"job-scheduler"
							)
						);

					JobScheduler.Command command = commandSupplier.get();

					if (command != null) {
						actorRef.tell(command);
					}
				}
				catch (Exception e) {
					log.error("error getting job-scheduler", e);

					throw new JobSchedulerException();
				}

			return null;
		});

	}
}
