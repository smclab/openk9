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

package io.openk9.tenantmanager.resource;

import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.datasource.grpc.CreatePresetPluginDriverRequest;
import io.openk9.datasource.grpc.Datasource;
import io.openk9.datasource.grpc.Preset;
import io.openk9.datasource.grpc.PresetPluginDrivers;
import io.openk9.tenantmanager.provisioning.plugindriver.CreateConnectorSaga;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@Path("/provisioning")
@RolesAllowed("admin")
public class ProvisioningResource {

	@GrpcClient
	Datasource datasource;
	@GrpcClient("appmanager")
	AppManager appManager;

	@Inject
	@ConfigProperty(name = "quarkus.application.version")
	String applicationVersion;

	@POST
	@Path("/initTenant")
	public Uni<InitTenantResponse> initTenant(@Valid InitTenantRequest request) {
		return datasource.initTenant(io.openk9.datasource.grpc.InitTenantRequest.newBuilder()
			.setSchemaName(request.tenantName)
			.build()
		).map(initTenantResponse -> new InitTenantResponse(initTenantResponse.getBucketId()));
	}

	@POST
	@Path("/connector")
	public Uni<CreateConnectorResponse> createConnector(@Valid CreateConnectorRequest request) {

		var connectorName = String.format(
			"%s-%s-%s",
			request.tenantName,
			request.preset,
			applicationVersion.replace('.', '_')
		);

		var actorSystem = ActorSystem.apply(
			CreateConnectorSaga.create(
				appManager,
				AppManifest.newBuilder()
					.setSchemaName(request.tenantName)
					.setChart(PresetPluginDrivers.getPluginDriver(request.preset))
					.setVersion(applicationVersion)
					.build(),
				datasource,
				CreatePresetPluginDriverRequest.newBuilder()
					.setSchemaName(request.tenantName)
					.setPreset(request.preset)
					.build()
			),
			connectorName
		);

		return Uni.createFrom()
			.completionStage(AskPattern.ask(
				actorSystem,
				CreateConnectorSaga.Exec::new,
				Duration.ofMinutes(2),
				actorSystem.scheduler()
			))
			.onItemOrFailure()
			.transform((responses, throwable) -> {
				if (throwable != null) {
					return new CreateConnectorResponse(throwable.getMessage());
				}
				else {
					return new CreateConnectorResponse(responses.name());
				}
			});
	}

	public record InitTenantRequest(@NotEmpty String tenantName) {}

	public record InitTenantResponse(long bucketId) {}
	public record CreateConnectorRequest(
		@NotEmpty String tenantName,
		Preset preset
	) {}

	public record CreateConnectorResponse(String result) {}

}
