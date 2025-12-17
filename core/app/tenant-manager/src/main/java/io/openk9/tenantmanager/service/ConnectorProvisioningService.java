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

import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.datasource.grpc.CreatePluginDriverResponse;
import io.openk9.datasource.grpc.CreatePresetPluginDriverRequest;
import io.openk9.datasource.grpc.Datasource;
import io.openk9.quarkus.common.EventBusInstanceHolder;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.Message;

@ApplicationScoped
public class ConnectorProvisioningService {

	private static final String INSTALL = "ConnectorProvisioningService#install";
	private static final String UNINSTALL = "ConnectorProvisioningService#uninstall";
	private static final String PERSIST = "ConnectorProvisioningService#persist";

	@GrpcClient("datasource")
	Datasource datasourceService;
	@GrpcClient("appmanager")
	AppManager appManagerService;

	public static CompletionStage<Void> install(AppManifest manifest) {

		return EventBusInstanceHolder
			.<Void>request(INSTALL, new InstallRequest(manifest))
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletionStage<Long> persist(
		CreatePresetPluginDriverRequest createPreset) {

		return EventBusInstanceHolder
			.<Long>request(PERSIST, new PersistRequest(createPreset))
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletionStage<Void> uninstall(AppManifest manifest) {

		return EventBusInstanceHolder
			.<Void>request(UNINSTALL, new UninstallRequest(manifest))
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	@ConsumeEvent(INSTALL)
	Uni<Void> install(InstallRequest request) {

		return appManagerService.applyResource(request.manifest())
			.replaceWithVoid();
	}

	@ConsumeEvent(PERSIST)
	Uni<Long> persist(PersistRequest request) {

		return datasourceService.createPresetPluginDriver(request.createPreset())
			.map(CreatePluginDriverResponse::getPluginDriverId);
	}

	@ConsumeEvent(UNINSTALL)
	Uni<Void> uninstall(UninstallRequest request) {

		return appManagerService.deleteResource(request.manifest())
			.replaceWithVoid();
	}

	private record InstallRequest(AppManifest manifest) {}
	private record PersistRequest(CreatePresetPluginDriverRequest createPreset) {}
	private record UninstallRequest(AppManifest manifest) {}

}
