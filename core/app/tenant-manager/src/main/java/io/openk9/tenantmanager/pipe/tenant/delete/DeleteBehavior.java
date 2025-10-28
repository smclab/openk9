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

package io.openk9.tenantmanager.pipe.tenant.delete;

import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.app.manager.grpc.AppManifestList;
import io.openk9.app.manager.grpc.DeleteAllResourcesResponse;
import io.openk9.app.manager.grpc.DeleteIngressRequest;
import io.openk9.datasource.grpc.PresetPluginDrivers;
import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteGroupMessage;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteMessage;
import io.openk9.tenantmanager.service.DeleteService;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.UUID;

import static io.openk9.tenantmanager.actor.TypedActor.Become;
import static io.openk9.tenantmanager.actor.TypedActor.Die;
import static io.openk9.tenantmanager.actor.TypedActor.Stay;

public class DeleteBehavior implements TypedActor.Behavior<DeleteMessage> {

	private static final Logger LOGGER = Logger.getLogger(
		DeleteBehavior.class);
	private final EventBus eventBus;
	private final TypedActor.Address<DeleteMessage> self;
	private TypedActor.Address<DeleteGroupMessage> deleteGroupActor;
	private String token;
	private String virtualHost;
	public DeleteBehavior(
		EventBus eventBus, TypedActor.Address<DeleteMessage> self) {

		this.eventBus = eventBus;
		this.self = self;

	}

	@Override
	public TypedActor.Effect<DeleteMessage> apply(DeleteMessage timeoutMessage) {

		if (timeoutMessage instanceof DeleteMessage.Start start) {
			this.virtualHost = start.virtualHost();
			this.token = UUID.randomUUID().toString();
			this.deleteGroupActor = start.deleteGroupActor();
			LOGGER.info("for virtualHost: " + virtualHost + " token: " + token);
		}
		else if (timeoutMessage instanceof DeleteMessage.Delete delete) {

			if (this.token.equals(delete.token())) {
				LOGGER.infof("Start Delete tenant for virtualHost %s ", virtualHost);

				eventBus.<Tenant>request(
						DeleteService.FIND_TENANT_BY_VIRTUAL_HOST, virtualHost)
					.flatMap((Message<Tenant> message) -> {

						var tenant = message.body();
						var appManager = delete.appManager();

						var unis = new ArrayList<Uni<Void>>();

						LOGGER.infof(
							"Tenant with id %s found for virtualHost %s",
							tenant.getId(),
							virtualHost
						);

						Uni<Void> deleteSchema = eventBus.request(
							DeleteService.DELETE_SCHEMA,
							tenant.getSchemaName()
						).invoke(() -> LOGGER.infof(
								"Schema %s for virtualHost %s deleted.",
								tenant.getSchemaName(),
								virtualHost
							)
						).replaceWithVoid();

						unis.add(deleteSchema);

						Uni<Void> deleteRealm = eventBus.request(
							DeleteService.DELETE_REALM,
							tenant.getRealmName()
						).invoke(() -> LOGGER.infof(
								"Realm for %s virtualHost %s deleted.",
								tenant.getRealmName(),
								virtualHost
							)
						).replaceWithVoid();

						unis.add(deleteRealm);

						Uni<Void> deleteTenant = eventBus.request(
							DeleteService.DELETE_TENANT,
							tenant.getId()
						).invoke(() -> LOGGER.infof(
								"Tenant with id %s for virtualHost %s deleted.",
								tenant.getId(),
								virtualHost
							)
						).replaceWithVoid();

						unis.add(deleteTenant);

						Uni<Void> deleteIngress =
						appManager.deleteIngress(
							DeleteIngressRequest.newBuilder()
								.setSchemaName(tenant.getSchemaName())
								.setVirtualHost(virtualHost)
								.build()
						)
						.invoke(() -> LOGGER.infof(
							"Ingress for schemaName %s and virtualHost %s deleted.",
							tenant.getSchemaName(),
							virtualHost
						)).replaceWithVoid();

						unis.add(deleteIngress);

						var appManifestList = PresetPluginDrivers.getAllPluginDrivers().stream()
							.map(preset ->
								AppManifest.newBuilder()
									.setSchemaName(tenant.getSchemaName())
									.setChart(preset)
									.setVersion(delete.applicationVersion())
									.build()
							)
							.toList();

						var deletedAllResources =
							appManager.deleteAllResources(
								AppManifestList.newBuilder()
									.addAllAppManifests(appManifestList)
									.build()
							)
							.invoke(this::_logDeleteAllResourcesResult)
							.replaceWithVoid();

						unis.add(deletedAllResources);

						return Uni.join()
							.all(unis)
							.andCollectFailures()
							.onItemOrFailure()
							.invoke((__, t) -> {
								if (t != null) {
									LOGGER.error(t.getMessage(), t);
								}
								self.tell(new DeleteMessage.Finished());
							});
					})
					.await()
					.indefinitely();

			}
			else {
				LOGGER.warn("Invalid token");
			}

		}
		else if (timeoutMessage instanceof DeleteMessage.Stop) {
			_tellStop();
			LOGGER.warn("token expired: " + token);
			return Become(nextMsg -> {
				LOGGER.warn("token expired: " + token);
				return Stay();
			});
		}
		else if (timeoutMessage instanceof DeleteMessage.Finished) {
			_tellStop();
			return Die();
		}

		return Stay();

	}

	private void _logDeleteAllResourcesResult(
			DeleteAllResourcesResponse response) {

		response.getDeleteResourceStatusList()
			.forEach(deleteResourcesStatus -> {
				switch (deleteResourcesStatus.getStatus()) {
					case SUCCESS -> LOGGER.infof(
						"Resource %s for virtualHost %s deleted.",
						deleteResourcesStatus.getResourceName(),
						virtualHost);
					case ERROR -> LOGGER.warnf(
						"Failed to delete resource %s for virtualHost %s.",
						deleteResourcesStatus.getResourceName(),
						virtualHost
					);
				}
			});
	}

	private void _tellStop() {
		if (this.deleteGroupActor != null) {
			this.deleteGroupActor.tell(new DeleteGroupMessage.RemoveDeleteRequest(this.virtualHost));
		}
	}
	
}
