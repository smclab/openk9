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

import java.time.Duration;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.datasource.grpc.CreatePresetPluginDriverRequest;
import io.openk9.datasource.grpc.Preset;
import io.openk9.datasource.grpc.PresetPluginDrivers;
import io.openk9.tenantmanager.provisioning.plugindriver.CreateConnectorSaga;
import io.openk9.tenantmanager.service.TenantProvisioningService;

import io.smallrye.mutiny.Uni;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/provisioning")
public class ProvisioningResource {

	@Inject
	TenantProvisioningService tenantProvisioningService;

	@Inject
	@ConfigProperty(name = "quarkus.application.version")
	String applicationVersion;

	@Operation(
		operationId = "initTenant",
		summary = "Initialize the default bucket for an existing tenant",
		description = "Triggers the post-provisioning step that creates "
			+ "the default datasource bucket for an existing tenant "
			+ "schema. Returns the identifier of the bucket created "
			+ "in the datasource service."
	)
	@Tag(name = "Tenant Provisioning")
	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Tenant initialization successful",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = InitTenantResponse.class),
					example = TenantManagerRequestExamples.INIT_TENANT_RESPONSE
				)
			}
		),
	})
	@RequestBody(
		content = {
			@Content(
				mediaType = MediaType.APPLICATION_JSON,
				schema = @Schema(implementation = InitTenantRequest.class),
				examples = {
					@ExampleObject(
						name = "init tenant",
						value = TenantManagerRequestExamples.INIT_TENANT_REQUEST
					)
				}
			)
		}
	)
	@POST
	@Path("/initTenant")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<InitTenantResponse> initTenant(@Valid InitTenantRequest request) {
		return tenantProvisioningService
			.initTenant(request.tenantName())
			.map(InitTenantResponse::new);
	}

	@Operation(
		operationId = "createConnector",
		summary = "Create a preset connector plugin driver for a tenant",
		description = "Deploys the Helm chart of the selected preset "
			+ "connector for the target tenant and registers the "
			+ "corresponding plugin driver. The preset is selected "
			+ "from the supported values (YOUTUBE, CRAWLER, EMAIL, "
			+ "GITLAB, SITEMAP, DATABASE, MINIO). The saga runs "
			+ "asynchronously and the response's `result` field "
			+ "carries the saga outcome (SUCCESS, ERROR, COMPENSATION "
			+ "or COMPENSATION_ERROR)."
	)
	@Tag(name = "Tenant Provisioning")
	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Connector creation saga completed",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = CreateConnectorResponse.class),
					example = TenantManagerRequestExamples.CREATE_CONNECTOR_RESPONSE
				)
			}
		),
		@APIResponse(
			responseCode = "500",
			description = "Saga orchestrator failure "
				+ "(e.g. ask timeout, actor system error)"
		),
	})
	@RequestBody(
		content = {
			@Content(
				mediaType = MediaType.APPLICATION_JSON,
				schema = @Schema(implementation = CreateConnectorRequest.class),
				examples = {
					@ExampleObject(
						name = "create connector",
						value = TenantManagerRequestExamples.CREATE_CONNECTOR_REQUEST
					)
				}
			)
		}
	)
	@POST
	@Path("/connector")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<CreateConnectorResponse> createConnector(@Valid CreateConnectorRequest request) {

		var connectorName = String.format(
			"%s-%s-%s",
			request.tenantName,
			request.preset,
			applicationVersion.replace('.', '_')
		);

		var actorSystem = ActorSystem.apply(
			CreateConnectorSaga.create(
				AppManifest.newBuilder()
					.setSchemaName(request.tenantName)
					.setChart(PresetPluginDrivers.getPluginDriver(request.preset))
					.setVersion(applicationVersion)
					.build(),
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
			.map(responses -> new CreateConnectorResponse(responses.name()))
			.onFailure()
			.transform(cause -> new WebApplicationException(
				cause, Response.Status.INTERNAL_SERVER_ERROR));
	}

	public record InitTenantRequest(@NotEmpty String tenantName) {}

	public record InitTenantResponse(long bucketId) {}
	public record CreateConnectorRequest(
		@NotEmpty String tenantName,
		Preset preset
	) {}

	public record CreateConnectorResponse(String result) {}

}
