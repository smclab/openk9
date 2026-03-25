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

package io.openk9.tenantmanager.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.app.manager.grpc.AppManifestList;
import io.openk9.app.manager.grpc.CreateIngressRequest;
import io.openk9.app.manager.grpc.CreateIngressResponse;
import io.openk9.app.manager.grpc.DeleteIngressRequest;
import io.openk9.app.manager.grpc.DeleteIngressResponse;
import io.openk9.common.util.RandomGenerator;
import io.openk9.datasource.grpc.Datasource;
import io.openk9.datasource.grpc.InitTenantRequest;
import io.openk9.datasource.grpc.InitTenantResponse;
import io.openk9.datasource.grpc.PresetPluginDrivers;
import io.openk9.quarkus.common.EventBusInstanceHolder;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.pipe.tenant.create.TenantManagerActorSystem;
import io.openk9.tenantmanager.service.dto.CreateTablesResponse;
import io.openk9.tenantmanager.service.dto.CreateTenantRequest;
import io.openk9.tenantmanager.service.dto.DeleteTenantRequest;
import io.openk9.tenantmanager.service.dto.DeleteTenantResponse;
import io.openk9.tenantmanager.service.dto.EffectiveDeleteTenantRequest;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.vertx.ConsumeEvent;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.Message;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TenantProvisioningService {

	// ========================================================================
	// Event Bus addresses.
	// ========================================================================

	public static final String CREATE_REALM = "TenantProvisioningService#createRealm";
	public static final String CREATE_SCHEMA = "TenantProvisioningService#createSchema";
	public static final String CREATE_INGRESS = "TenantProvisioningService#createIngress";
	public static final String CREATE_ENTITY = "TenantProvisioningService#createEntity";
	public static final String DELETE_REALM = "TenantProvisioningService#deleteRealm";
	public static final String DELETE_SCHEMA = "TenantProvisioningService#deleteSchema";
	public static final String DELETE_INGRESS = "TenantProvisioningService#deleteIngress";
	public static final String EXECUTE_DELETION =
		"TenantProvisioningService#executeDeletion";
	public static final String GENERATE_RANDOM_TENANT_NAME =
		"TenantProvisioningService#generateRandomTenantName";

	private static final Logger log = Logger.getLogger(TenantProvisioningService.class);

	@Inject
	@ConfigProperty(name = "quarkus.application.version")
	String applicationVersion;

	@Inject
	@ConfigProperty(name = "quarkus.kubernetes.namespace")
	Optional<String> k8sNamespace;

	// ========================================================================
	// Aggregated services.
	// These are the services invoked from Event Bus consumers.
	// ========================================================================

	@Inject
	TenantRealmService realmService;
	@Inject
	TenantSchemaService schemaService;
	@Inject
	TenantDbService dbService;
	@GrpcClient("appmanager")
	AppManager appManagerService;
	@GrpcClient("datasource")
	Datasource datasourceService;

	// ========================================================================
	// Provisioning Actor system.
	// ========================================================================

	@Inject
	TenantManagerActorSystem tenantManagerActorSystem;

	// ========================================================================
	// Deletion token storage.
	// ========================================================================

	private final ConcurrentMap<String, String> deletionTokens =
		new ConcurrentHashMap<>();

	// ========================================================================
	// Event Bus requests.
	// Messages are produced and handled from Pekko Actors that orchestrate the
	// provisioning/deprovisioning of a Tenant.
	// ========================================================================

	public static CompletionStage<TenantResponseDTO> createEntity(Tenant tenant) {

		return EventBusInstanceHolder
			.<TenantResponseDTO>request(
				CREATE_ENTITY, CreateEntityRequest.of(tenant))
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletionStage<CreateIngressResponse> createIngress(
		String virtualHost,
		String tenantId) {

		return EventBusInstanceHolder
			.<CreateIngressResponse>request(
				CREATE_INGRESS, CreateIngressRequest.newBuilder()
					.setVirtualHost(virtualHost)
					.setSchemaName(tenantId) // todo rename to tenantId
					.build()
			)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletionStage<TenantRealmService.CreatedRealm> createRealm(
		String virtualHost, String realmName) {

		return EventBusInstanceHolder
			.<TenantRealmService.CreatedRealm>request(
				CREATE_REALM, CreateRealmRequest.of(virtualHost, realmName))
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletionStage<Void> createSchema(
		String virtualHost, String schemaName) {

		return EventBusInstanceHolder
			.request(
				CREATE_SCHEMA, CreateSchemaRequest.of(virtualHost, schemaName))
			.replaceWithVoid()
			.subscribeAsCompletionStage();
	}

	public static CompletionStage<DeleteIngressResponse> deleteIngress(
		String virtualHost, String tenantId) {

		return EventBusInstanceHolder
			.<DeleteIngressResponse>request(
				DELETE_INGRESS, DeleteIngressRequest.newBuilder()
					.setVirtualHost(virtualHost)
					.setSchemaName(tenantId)
					.build()
			)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletionStage<Void> deleteRealm(String realmName) {

		return EventBusInstanceHolder.request(
				DELETE_REALM, DeleteRealmRequest.of(realmName))
			.replaceWithVoid()
			.subscribeAsCompletionStage();
	}

	public static CompletionStage<Void> deleteSchema(String schemaName) {

		return EventBusInstanceHolder.request(
				DELETE_SCHEMA, DeleteSchemaRequest.of(schemaName))
			.replaceWithVoid().subscribeAsCompletionStage();
	}

	public static CompletionStage<String> generateRandomSchemaName() {
		return EventBusInstanceHolder.getEventBus()
			.<String>request(GENERATE_RANDOM_TENANT_NAME, null)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}


	// ========================================================================
	// Provisioning / Deprovisioning.
	// ========================================================================

	public Uni<TenantResponseDTO> create(CreateTenantRequest request) {

		String virtualHost = request.virtualHost();

		return dbService.findByVirtualHost(virtualHost)
			.onItem().ifNotNull()
			.failWith(() -> new DuplicateVirtualHostException(virtualHost))
			.flatMap(n -> tenantManagerActorSystem.startCreateTenant(request));
	}

	/**
	 * Confirms and executes a pending tenant deletion.
	 * <p>
	 * Validates the token against the stored value for the given
	 * virtualHost. If valid, removes the token and runs the
	 * deletion asynchronously.
	 *
	 * @param request the confirmation request with virtualHost
	 *        and token
	 * @return a response confirming the deletion has started
	 */
	public Uni<DeleteTenantResponse> delete(
		EffectiveDeleteTenantRequest request) {

		String virtualHost = request.virtualHost();
		String token = request.token();

		String stored = deletionTokens.remove(virtualHost);

		if (stored == null || !stored.equals(token)) {
			return Uni.createFrom().failure(
				new InvalidDeletionTokenException(virtualHost)
			);
		}

		EventBusInstanceHolder.getEventBus()
			.send(EXECUTE_DELETION, virtualHost);

		return Uni.createFrom()
			.item(new DeleteTenantResponse(
				"Tenant deletion started."));
	}

	public Uni<CreateTablesResponse> populateSchema(long tenantId) {

		return dbService.findById(tenantId)
			.flatMap(t -> {
				if (t == null) {
					String message = String.format(
						"Tenant not found with id: %s", tenantId);

					throw new NoSuchElementException(message);
				}
				else {
					return VertxContextSupport.executeBlocking(() -> {
						schemaService.runInitialization(
							t.tenantName(), t.virtualHost(), false);
						String message = String.format(
							"Tables for schema %s created",
							t.tenantName());

						return new CreateTablesResponse(message);
					});
				}
			});

	}

	// ========================================================================
	// Event Bus Consumers.
	// ========================================================================


	@ConsumeEvent(CREATE_ENTITY)
	Uni<TenantResponseDTO> createEntity(CreateEntityRequest request) {

		var tenant = request.tenant();
		return dbService.persist(tenant);
	}

	@ConsumeEvent(CREATE_REALM)
	Uni<TenantRealmService.CreatedRealm> createRealm(
		CreateRealmRequest request) {

		var realmName = request.realmName();
		var virtualHost = request.virtualHost();

		return realmService.createRealm(realmName, virtualHost);
	}

	@ConsumeEvent(CREATE_SCHEMA)
	Uni<Void> createSchema(CreateSchemaRequest request) {

		var schemaName = request.schemaName();
		var virtualHost = request.virtualHost();

		return VertxContextSupport.executeBlocking(() -> {
			try {
				schemaService.runInitialization(schemaName, virtualHost, true);
			}
			catch (Exception e) {
				log.errorf(
					e, "Schema creation failed for %s", schemaName);
				throw e;
			}

			return null;
		});
	}

	@ConsumeEvent(CREATE_INGRESS)
	Uni<CreateIngressResponse> createingress(CreateIngressRequest request) {

		return appManagerService.createIngress(request);
	}

	@ConsumeEvent(DELETE_INGRESS)
	Uni<DeleteIngressResponse> deleteIngress(DeleteIngressRequest request) {

		return appManagerService.deleteIngress(request);
	}

	@ConsumeEvent(DELETE_REALM)
	Uni<Void> deleteRealm(DeleteRealmRequest request) {

		var realmName = request.realmName();
		return realmService.deleteRealm(realmName);
	}

	@ConsumeEvent(DELETE_SCHEMA)
	Uni<Void> deleteSchema(DeleteSchemaRequest request) {

		var schemaName = request.schemaName();
		return VertxContextSupport.executeBlocking(() -> {
			try {
				schemaService.rollbackRunLiquibaseMigration(schemaName);
			}
			catch (Exception e) {
				log.errorf(
					e, "An error occurred while deleting schema %s", schemaName);
			}

			return null;
		});
	}

	@ConsumeEvent(GENERATE_RANDOM_TENANT_NAME)
	Uni<String> generateRandomTenantName(Object none) {

		return dbService.findAllTenantName()
			.map(names ->
				RandomGenerator.generate(names.toArray(String[]::new)));
	}


	/**
	 * Executes all tenant deletion operations in parallel.
	 * <p>
	 * Finds the tenant by virtualHost, then runs deleteSchema,
	 * deleteRealm (if provisioned), deleteEntity, deleteIngress,
	 * and deleteAllResources concurrently. Failures from
	 * individual operations are collected, not short-circuited.
	 */
	@ConsumeEvent(value = EXECUTE_DELETION, blocking = true)
	void executeDeletion(String virtualHost) {
		dbService.findByVirtualHost(virtualHost)
			.flatMap(tenant -> {
				List<Uni<Void>> ops = new ArrayList<>();

				ops.add(Uni.createFrom()
					.<Void>item(() -> {
						schemaService
							.rollbackRunLiquibaseMigration(
								tenant.tenantName());
						log.infof(
							"Schema %s deleted for %s",
							tenant.tenantName(),
							virtualHost);
						return null;
					}));

				if (tenant.realmProvisioned()) {
					ops.add(realmService
						.deleteRealm(tenant.tenantName())
						.invoke(() -> log.infof(
							"Realm deleted for %s",
							virtualHost)));
				}
				else {
					log.infof(
						"Skipping realm deletion for %s"
							+ " (not self-provisioned)",
						virtualHost);
				}

				ops.add(dbService
					.deleteTenant(
						Long.parseLong(tenant.id()))
					.invoke(() -> log.infof(
						"Entity %s deleted for %s",
						tenant.id(), virtualHost)));

				if (k8sNamespace.isPresent()) {
					ops.add(appManagerService
						.deleteIngress(
							DeleteIngressRequest.newBuilder()
								.setVirtualHost(virtualHost)
								.setSchemaName(
									tenant.tenantName())
								.build())
						.replaceWithVoid()
						.invoke(() -> log.infof(
							"Ingress deleted for %s",
							virtualHost)));

					var manifests = PresetPluginDrivers
						.getAllPluginDrivers()
						.stream()
						.map(preset -> AppManifest.newBuilder()
							.setSchemaName(tenant.tenantName())
							.setChart(preset)
							.setVersion(applicationVersion)
							.build())
						.toList();

					ops.add(appManagerService
						.deleteAllResources(
							AppManifestList.newBuilder()
								.addAllAppManifests(manifests)
								.build())
						.invoke(resp -> resp
							.getDeleteResourceStatusList()
							.forEach(s -> log.infof(
								"Resource %s: %s for %s",
								s.getResourceName(),
								s.getStatus(),
								virtualHost)))
						.replaceWithVoid());
				}
				else {
					log.infof(
						"Skipping ingress and resource deletion"
						+ " for %s (no Kubernetes namespace"
						+ " configured)", virtualHost);
				}

				return Uni.join()
					.all(ops)
					.andCollectFailures()
					.replaceWithVoid();
			})
			.onItem().invoke(() -> log.infof(
				"Tenant deletion completed: %s",
				virtualHost))
			.onFailure().recoverWithUni(t -> {
				log.errorf(t,
					"Tenant deletion failed: %s",
					virtualHost);
				return Uni.createFrom().voidItem();
			})
			.await()
			.indefinitely();
	}

	// ========================================================================
	// Async APIs.
	// ========================================================================

	public Uni<Long> initTenant(String schemaName) {

		return datasourceService.initTenant(InitTenantRequest
				.newBuilder()
				.setSchemaName(schemaName)
				.build()
			)
			.map(InitTenantResponse::getBucketId);
	}

	/**
	 * Requests a tenant deletion by generating a confirmation
	 * token. The token is returned to the client and must be
	 * sent back via {@link #delete(EffectiveDeleteTenantRequest)}
	 * to confirm the operation.
	 *
	 * @param request the deletion request with virtualHost
	 * @return a response containing the deletion token
	 */
	public Uni<DeleteTenantResponse> requestDeletion(
		DeleteTenantRequest request) {

		String virtualHost = request.virtualHost();

		return dbService
			.findByVirtualHost(virtualHost)
			.flatMap(tenant -> {
				if (tenant == null) {
					return Uni.createFrom().failure(
						new TenantNotFoundException(
							virtualHost)
					);
				}

				String token = UUID.randomUUID().toString();
				deletionTokens.put(virtualHost, token);

				log.infof(
					"Deletion token generated for %s",
					virtualHost);

				return Uni.createFrom().item(
					new DeleteTenantResponse(
						"Deletion token: " + token
						+ ". Send it back with a DELETE"
						+ " request to confirm."));
			});
	}

	// ========================================================================
	// Event Bus Request Messages.
	// =======================================================================

	public record CreateEntityRequest(Tenant tenant) {

		public static CreateEntityRequest of(Tenant tenant) {
			return new CreateEntityRequest(tenant);
		}
	}

	private record CreateRealmRequest(String virtualHost, String realmName) {

		public static CreateRealmRequest of(String virtualHost, String realmName) {
			return new CreateRealmRequest(virtualHost, realmName);
		}
	}

	private record CreateSchemaRequest(String virtualHost, String schemaName) {

		public static CreateSchemaRequest of(String virtualHost, String schemaName) {
			return new CreateSchemaRequest(virtualHost, schemaName);
		}
	}

	private record DeleteRealmRequest(String realmName) {

		public static DeleteRealmRequest of(String realmName) {
			return new DeleteRealmRequest(realmName);
		}
	}

	private record DeleteSchemaRequest(String schemaName) {

		public static DeleteSchemaRequest of(String schemaName) {
			return new DeleteSchemaRequest(schemaName);
		}
	}

}
