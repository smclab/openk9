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

package io.openk9.tenantmanager.pipe.tenant.create;


import io.openk9.app.manager.grpc.AppManager;
import io.openk9.tenantmanager.config.KeycloakContext;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.SupervisorStrategy;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class TenantManagerActorSystem {

	@PostConstruct
	public void init() {
		_actorSystem = ActorSystem.create(
			Behaviors
				.supervise(Supervisor.create())
				.onFailure(SupervisorStrategy.resume()),
			"tenant-manager-creator"
		);
	}

	public Uni<Tenant> startCreateTenant(String virtualHost, String realmName) {

		CompletionStage<Supervisor.Response> ask =
			AskPattern.ask(
				_actorSystem,
				(ActorRef<Supervisor.Response> actorRef) ->
					new Supervisor.Start(
						virtualHost,
						realmName,
						liquibaseService,
						tenantService,
						appManager,
						config,
						actorRef
					),
				requestTimeout,
				_actorSystem.scheduler()
			);

		return Uni
			.createFrom()
			.completionStage(ask)
			.runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
			.onItem()
			.transform(Unchecked.function((res) -> {

				if (res instanceof Supervisor.Success success) {
					return success.tenant();
				}
				else if (res == Supervisor.Error.INSTANCE) {
					throw new RuntimeException(
						"Tenant Creation Failed for virtualHost: " + virtualHost);
				}
				else {
					throw new IllegalStateException("unknown response");
				}

			}));

	}

	public Uni<Void> populateSchema(String schemaName, String virtualHost) {
		return Uni
			.createFrom()
			.<Void>emitter(sink -> {

			try {
				liquibaseService.runInitialization(schemaName, virtualHost, false);
				sink.complete(null);
			}
			catch (Exception e) {
				sink.fail(e);
			}

		})
			.runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
	}

	private ActorSystem<Supervisor.Command> _actorSystem;

	@Inject
	@GrpcClient("appmanager")
	AppManager appManager;

	@Inject
	TenantService tenantService;

	@Inject
	DatasourceLiquibaseService liquibaseService;

	@ConfigProperty(
		name = "openk9.tenant-manager.create-tenant-timeout",
		defaultValue = "45s"
	)
	Duration requestTimeout;

	@Inject
	KeycloakContext config;

}
