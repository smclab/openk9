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

import java.util.NoSuchElementException;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.service.DuplicateVirtualHostException;
import io.openk9.tenantmanager.service.InvalidDeletionTokenException;
import io.openk9.tenantmanager.service.InvalidTenantNameException;
import io.openk9.tenantmanager.service.TenantNotFoundException;
import io.openk9.tenantmanager.service.TenantProvisioningService;
import io.openk9.tenantmanager.service.dto.CreateTablesResponse;
import io.openk9.tenantmanager.service.dto.CreateTenantRequest;
import io.openk9.tenantmanager.service.dto.DeleteTenantRequest;
import io.openk9.tenantmanager.service.dto.DeleteTenantResponse;
import io.openk9.tenantmanager.service.dto.EffectiveDeleteTenantRequest;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/tenant-manager/tenant")
public class TenantManagerResource {

	@Operation(
		operationId = "createTenant",
		summary = "Create and provision a new tenant",
		description = "Provisions a new tenant: assigns a virtual host, "
			+ "creates the database schema, configures the security "
			+ "model, optionally bootstraps a Keycloak realm, and "
			+ "exposes the configured ingress routes. Returns the "
			+ "descriptor of the created tenant."
	)
	@Tag(name = "Tenant Provisioning")
	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Tenant created",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = TenantResponseDTO.class),
					example = TenantManagerRequestExamples.CREATE_TENANT_RESPONSE
				)
			}
		),
	})
	@RequestBody(
		content = {
			@Content(
				mediaType = MediaType.APPLICATION_JSON,
				schema = @Schema(implementation = CreateTenantRequest.class),
				examples = {
					@ExampleObject(
						name = "minimal",
						summary = "Minimal tenant with Keycloak auto-provisioning",
						description = "Only the required fields. OpenK9 "
							+ "provisions a Keycloak realm and exposes "
							+ "the default ingress (SEARCH, "
							+ "ADMINISTRATION, RAG). The tenant name "
							+ "is auto-generated.",
						value = TenantManagerRequestExamples.CREATE_TENANT_MINIMAL_REQUEST
					),
					@ExampleObject(
						name = "external-idp",
						summary = "Tenant authenticated by an external "
							+ "OAuth2 identity provider",
						description = "Provide oAuth2Settings to skip "
							+ "Keycloak provisioning and reuse an "
							+ "existing IdP. The clientId/clientSecret "
							+ "pair authenticates against the issuer.",
						value = TenantManagerRequestExamples.CREATE_TENANT_EXTERNAL_IDP_REQUEST
					),
					@ExampleObject(
						name = "search-only-public",
						summary = "Anonymous public search-only "
							+ "deployment",
						description = "**WARNING — development-only "
							+ "configuration.** Use only in local "
							+ "development environments, or in "
							+ "deployments shipped via Docker Compose "
							+ "for evaluation / PoC. Do not enable in "
							+ "production. "
							+ "No gateway authentication: only the "
							+ "SEARCH ingress is exposed and search "
							+ "is reachable anonymously. "
							+ "Administration, RAG and ingestion "
							+ "routes are not configured.",
						value = TenantManagerRequestExamples.CREATE_TENANT_SEARCH_ONLY_REQUEST
					),
					@ExampleObject(
						name = "api-key-ingestion",
						summary = "OAuth2 admin/search, API key on "
							+ "data and ingestion paths",
						description = "Admin and search routes use "
							+ "OAuth2; data and ingestion routes are "
							+ "protected by API keys. Full ingress "
							+ "(SEARCH, ADMINISTRATION, RAG, "
							+ "INGESTION) exposed.",
						value = TenantManagerRequestExamples.CREATE_TENANT_API_KEY_REQUEST
					)
				}
			)
		}
	)
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<TenantResponseDTO> createTenant(
		@Valid CreateTenantRequest createTenantRequest) {

		return provisioningService.create(createTenantRequest)
			.onFailure(DuplicateVirtualHostException.class)
			.transform(cause -> new WebApplicationException(cause, Response.Status.CONFLICT))
			.onFailure(InvalidTenantNameException.class)
			.transform(cause -> new WebApplicationException(cause, Response.Status.BAD_REQUEST));
	}

	@Operation(
		operationId = "createTables",
		summary = "Populate the database schema of an existing tenant",
		description = "Creates the application tables for the tenant "
			+ "identified by the path parameter. The tenant's "
			+ "database schema must already exist."
	)
	@Tag(name = "Tenant Management")
	@Parameter(
		name = "id",
		in = ParameterIn.PATH,
		required = true,
		description = "Identifier of the tenant whose tables must be "
			+ "created.",
		schema = @Schema(implementation = Long.class)
	)
	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Tables created",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = CreateTablesResponse.class),
					example = TenantManagerRequestExamples.CREATE_TABLES_RESPONSE
				)
			}
		),
	})
	@POST
	@Path("/{id}/tables")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<CreateTablesResponse> createTables(@PathParam("id") Long id) {

		return provisioningService.populateSchema(id)
			.onFailure(NoSuchElementException.class)
			.transform(cause -> new WebApplicationException(cause, Response.Status.NOT_FOUND))
			.onFailure()
			.transform(WebApplicationException::new);
	}

	/**
	 * Requests a tenant deletion and returns a confirmation
	 * token.
	 */
	@Operation(
		operationId = "requestDeleteTenant",
		summary = "Request the deletion of a tenant",
		description = "Issues a deletion request for the tenant "
			+ "identified by virtual host. Returns a confirmation "
			+ "token that must be supplied to the DELETE endpoint to "
			+ "actually remove the tenant."
	)
	@Tag(name = "Tenant Provisioning")
	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Deletion token issued",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = DeleteTenantResponse.class),
					example = TenantManagerRequestExamples.REQUEST_DELETE_TENANT_RESPONSE
				)
			}
		),
	})
	@RequestBody(
		content = {
			@Content(
				mediaType = MediaType.APPLICATION_JSON,
				schema = @Schema(implementation = DeleteTenantRequest.class),
				examples = {
					@ExampleObject(
						name = "request delete tenant",
						value = TenantManagerRequestExamples.REQUEST_DELETE_TENANT_REQUEST
					)
				}
			)
		}
	)
	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DeleteTenantResponse> requestDeleteTenant(
		@Valid DeleteTenantRequest deleteTenantRequest) {

		return provisioningService
			.requestDeletion(deleteTenantRequest)
			.onFailure(TenantNotFoundException.class)
			.transform(cause ->
				new WebApplicationException(
					cause, Response.Status.BAD_REQUEST));
	}

	/**
	 * Confirms and executes a tenant deletion using the
	 * previously issued token.
	 */
	@Operation(
		operationId = "deleteTenant",
		summary = "Confirm and execute a tenant deletion",
		description = "Permanently removes the tenant identified by "
			+ "virtual host, using the confirmation token previously "
			+ "issued by the POST /delete endpoint."
	)
	@Tag(name = "Tenant Provisioning")
	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Tenant deleted",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = DeleteTenantResponse.class),
					example = TenantManagerRequestExamples.DELETE_TENANT_RESPONSE
				)
			}
		),
	})
	@RequestBody(
		content = {
			@Content(
				mediaType = MediaType.APPLICATION_JSON,
				schema = @Schema(implementation = EffectiveDeleteTenantRequest.class),
				examples = {
					@ExampleObject(
						name = "delete tenant",
						value = TenantManagerRequestExamples.DELETE_TENANT_REQUEST
					)
				}
			)
		}
	)
	@DELETE
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DeleteTenantResponse> deleteTenant(
		@Valid EffectiveDeleteTenantRequest request) {

		return provisioningService.delete(request)
			.onFailure(InvalidDeletionTokenException.class)
			.transform(cause ->
				new WebApplicationException(
					cause, Response.Status.FORBIDDEN));
	}

	@Inject
	TenantProvisioningService provisioningService;

}
