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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import io.openk9.app.manager.grpc.IngressScope;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.SecurityConfiguration;
import io.openk9.tenantmanager.service.dto.CreateTenantRequest;
import io.openk9.tenantmanager.service.dto.OAuth2Settings;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.SupervisorStrategy;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TenantManagerActorSystem {

	@ConfigProperty(name = "openk9.tenant-manager.create-tenant-timeout", defaultValue = "45s")
	Duration requestTimeout;

	private ActorSystem<TenantProvisioningManager.Command> actorSystem;

	@PreDestroy
	public void close() {

		if (actorSystem != null) {
			actorSystem.terminate();
		}
	}

	@PostConstruct
	public void init() {

		actorSystem = ActorSystem.create(
			Behaviors
				.supervise(TenantProvisioningManager.create())
				.onFailure(SupervisorStrategy.resume()),
			"tenant-provisioning-manager"
		);
	}

	public Uni<TenantResponseDTO> startCreateTenant(
		CreateTenantRequest request) {

		String vHost = request.virtualHost();
		String tenantName = request.tenantName();
		SecurityConfiguration secConfiguration =
			request.securityConfiguration();
		OAuth2Settings oAuth2Settings = request.oAuth2Settings();
		List<IngressScope> ingressScopes = request.ingressScopes();

		CompletionStage<TenantProvisioningManager.Response> ask =
			AskPattern.ask(
				actorSystem,
				(ActorRef<TenantProvisioningManager.Response> actorRef) ->
					new TenantProvisioningManager.CreateTenant(
						vHost, tenantName, secConfiguration,
						oAuth2Settings, ingressScopes, actorRef
					),
				requestTimeout,
				actorSystem.scheduler()
			);

		return Uni
			.createFrom()
			.completionStage(ask)
			.runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
			.map((res) -> getTenantResponseDTO(res, vHost));
	}

	private static TenantResponseDTO getTenantResponseDTO(
		TenantProvisioningManager.Response res, String vHost) {

		return switch (res) {
			case TenantProvisioningManager.Error ignore ->
				throw new RuntimeException(
					"Tenant Creation Failed for virtualHost: " + vHost);
			case TenantProvisioningManager.Success(
				TenantResponseDTO tenant) -> tenant;
		};
	}

}
