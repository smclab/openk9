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

package io.openk9.datasource.grpc;

import io.openk9.datasource.grpc.mapper.EnrichItemMapper;
import io.openk9.datasource.grpc.mapper.PluginDriverMapper;
import io.openk9.datasource.service.EnrichItemService;
import io.openk9.datasource.service.PluginDriverService;
import io.openk9.datasource.service.TenantInitializerService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;

@GrpcService
public class DatasourceGrpcService implements Datasource {
	@Inject
	TenantInitializerService tenantInitializerService;
	@Inject
	EnrichItemService enrichItemService;
	@Inject
	PluginDriverService pluginDriverService;
	@Inject
	EnrichItemMapper enrichItemMapper;
	@Inject
	PluginDriverMapper pluginDriverMapper;

	@Override
	public Uni<InitTenantResponse> initTenant(InitTenantRequest request) {

		return tenantInitializerService.createDefault(request.getSchemaName())
			.map(integer -> InitTenantResponse.newBuilder()
				.setItemsCreated(integer)
				.build());
	}

	@Override
	public Uni<CreateEnrichItemResponse> createEnrichItem(CreateEnrichItemRequest request) {

		var enrichItemDTO = enrichItemMapper.map(request);

		return enrichItemService
			.findByName(request.getSchemaName(), enrichItemDTO.getName())
			.onItem()
			.transformToUni((item) -> enrichItemService.update(
				request.getSchemaName(), item.id, enrichItemDTO))
			.onFailure()
			.recoverWithUni(() -> enrichItemService.create(request.getSchemaName(), enrichItemDTO))
			.map(enrichItem -> CreateEnrichItemResponse.newBuilder()
				.setEnrichItemId(enrichItem.getId())
				.build()
			);
	}

	@Override
	public Uni<CreatePluginDriverResponse> createPluginDriver(CreatePluginDriverRequest request) {

		var pluginDriverDTO = pluginDriverMapper.map(request);

		return pluginDriverService
			.findByName(request.getSchemaName(), pluginDriverDTO.getName())
			.onItem()
			.transformToUni((item) -> pluginDriverService.update(
				request.getSchemaName(), item.getId(), pluginDriverDTO
			))
			.onFailure()
			.recoverWithUni(() -> pluginDriverService.create(
				request.getSchemaName(),
				pluginDriverDTO
			))
			.map(pluginDriver -> CreatePluginDriverResponse.newBuilder()
				.setPluginDriverId(pluginDriver.getId())
				.build()
			);
	}

	@Override
	public Uni<CreatePluginDriverResponse> createPresetPluginDriver(CreatePresetPluginDriverRequest request) {
		return null;
	}

}
