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

import java.util.concurrent.CompletionStage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.CreateIngressRequest;
import io.openk9.app.manager.grpc.CreateIngressResponse;
import io.openk9.app.manager.grpc.DeleteIngressRequest;
import io.openk9.app.manager.grpc.DeleteIngressResponse;
import io.openk9.quarkus.common.EventBusInstanceHolder;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.Tenant;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.vertx.ConsumeEvent;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.Message;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TenantProvisioningService {

	private static final String CREATE_REALM = "CREATE_REALM";
	private static final String CREATE_SCHEMA = "CREATE_SCHEMA";
	private static final String CREATE_INGRESS = "CREATE_INGRESS";
	private static final String CREATE_ENTITY = "CREATE_TENANT";
	private static final String DELETE_REALM = "DELETE_REALM";
	private static final String DELETE_SCHEMA = "DELETE_SCHEMA";
	private static final String DELETE_INGRESS = "DELETE_INGRESS";
	private static final String DELETE_ENTITY = "DELETE_TENANT";
	private static final String FIND_TENANT_BY_VIRTUAL_HOST = "FIND_TENANT_BY_VIRTUAL_HOST";

	private static final Logger log = Logger.getLogger(TenantProvisioningService.class);

	@Inject
	TenantRealmService realmService;
	@Inject
	TenantSchemaService schemaService;
	@Inject
	TenantDbService dbService;
	@Inject
	@GrpcClient("appmanager")
	AppManager appManager;

	// Event Bus requests

	public static CompletionStage<TenantResponseDTO> findTenant(
		String virtualHost) {

		return EventBusInstanceHolder.<TenantResponseDTO>request(
				FIND_TENANT_BY_VIRTUAL_HOST, FindTenantRequest.of(virtualHost))
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

	public static CompletionStage<CreateIngressResponse> createIngress(String virtualHost, String tenantId) {

		return EventBusInstanceHolder
			.<CreateIngressResponse>request(
				CREATE_INGRESS, CreateIngressRequest.newBuilder()
					.setVirtualHost(virtualHost)
					.setSchemaName(tenantId) // todo rename to tenantId
					.build())
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletionStage<TenantResponseDTO> createEntity(Tenant tenant) {

		return EventBusInstanceHolder
			.<TenantResponseDTO>request(
				CREATE_ENTITY, CreateEntityRequest.of(tenant))
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

	public static CompletionStage<DeleteIngressResponse> deleteIngress(
		String virtualHost, String tenantId) {

		return EventBusInstanceHolder
			.<DeleteIngressResponse>request(
				DELETE_INGRESS, DeleteIngressRequest.newBuilder()
					.setVirtualHost(virtualHost)
					.setSchemaName(tenantId)
					.build())
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletionStage<Void> deleteEntity(String tenantId) {

		return EventBusInstanceHolder.request(
			DELETE_ENTITY, DeleteEntityRequest.of(tenantId))
			.replaceWithVoid()
			.subscribeAsCompletionStage();
	}


	// Event Bus Consumers

	@ConsumeEvent(FIND_TENANT_BY_VIRTUAL_HOST)
	Uni<TenantResponseDTO> findTenant(FindTenantRequest request) {
		return dbService.findTenantByVirtualHost(request.virtualHost());
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

	@ConsumeEvent(DELETE_INGRESS)
	Uni<DeleteIngressResponse> deleteIngress(DeleteIngressRequest request) {

		return appManager.deleteIngress(request);
	}

	@ConsumeEvent(DELETE_ENTITY)
	Uni<Void> deleteEntity(DeleteEntityRequest request) {

		return dbService.deleteTenant(Long.parseLong(request.tenantId));
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
					e, "An error occurred while deleting schema %s", schemaName);
			}

			return null;
		});
	}

	@ConsumeEvent(CREATE_REALM)
	Uni<TenantRealmService.CreatedRealm> createRealm(
		CreateRealmRequest request) {

		var realmName = request.realmName();
		var virtualHost = request.virtualHost();

		return realmService.createRealm(realmName, virtualHost);
	}

	@ConsumeEvent(CREATE_INGRESS)
	Uni<CreateIngressResponse> createingress(CreateIngressRequest request) {

		return appManager.createIngress(request);
	}

	@ConsumeEvent(CREATE_ENTITY)
	Uni<TenantResponseDTO> createEntity(CreateEntityRequest request) {

		var tenant = request.tenant();
		return dbService.persist(tenant);
	}

	// Event Bus Request Messages

	private record FindTenantRequest(String virtualHost) {

		public static FindTenantRequest of(String virtualHost) {
			return new FindTenantRequest(virtualHost);
		}
	}

	private record CreateRealmRequest(String virtualHost, String realmName) {

		public static CreateRealmRequest of(String virtualHost, String realmName) {
			return new CreateRealmRequest(virtualHost, realmName);
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

	private record CreateSchemaRequest(String virtualHost, String schemaName) {

		public static CreateSchemaRequest of(String virtualHost, String schemaName) {
			return new CreateSchemaRequest(virtualHost, schemaName);
		}
	}

	private record CreateEntityRequest(Tenant tenant) {

		public static CreateEntityRequest of(Tenant tenant) {
			return new CreateEntityRequest(tenant);
		}
	}


	private record DeleteEntityRequest(String tenantId) {

		public static DeleteEntityRequest of(String tenantId) {
			return new DeleteEntityRequest(tenantId);
		}
	}
}
