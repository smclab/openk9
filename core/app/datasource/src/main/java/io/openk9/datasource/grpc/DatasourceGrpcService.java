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

import io.openk9.client.grpc.common.StructUtils;
import io.openk9.datasource.grpc.mapper.EnrichItemMapper;
import io.openk9.datasource.grpc.mapper.PluginDriverMapper;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.model.init.PluginDrivers;
import io.openk9.datasource.service.EnrichItemService;
import io.openk9.datasource.service.LargeLanguageModelService;
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
	@Inject
	LargeLanguageModelService largeLanguageModelService;

	@Override
	public Uni<InitTenantResponse> initTenant(InitTenantRequest request) {

		return tenantInitializerService.createDefault(request.getSchemaName())
			.map(bucketId -> InitTenantResponse.newBuilder()
				.setBucketId(bucketId)
				.build());
	}

	@Override
	public Uni<CreateEnrichItemResponse> createEnrichItem(CreateEnrichItemRequest request) {

		var enrichItemDTO = enrichItemMapper.map(request);

		return enrichItemService.upsert(request.getSchemaName(), enrichItemDTO)
			.map(enrichItem -> CreateEnrichItemResponse.newBuilder()
				.setEnrichItemId(enrichItem.getId())
				.build()
			);
	}

	@Override
	public Uni<CreatePluginDriverResponse> createPluginDriver(
		CreatePluginDriverRequest request) {

		var tenantId = request.getSchemaName();
		var pluginDriverDTO = pluginDriverMapper.map(request);

		return upsertPluginDriver(tenantId, pluginDriverDTO);
	}

	@Override
	public Uni<CreatePluginDriverResponse> createPresetPluginDriver(
		CreatePresetPluginDriverRequest request) {

		var tenantId = request.getSchemaName();
		var pluginDriverDTO = PluginDrivers.getPluginDriverDTO(
			request.getSchemaName(),
			request.getPreset()
		);

		return upsertPluginDriver(tenantId, pluginDriverDTO);
	}

	@Override
	public Uni<GetLLMConfigurationsResponse> getLLMConfigurations(GetLLMConfigurationsRequest request) {
		return largeLanguageModelService
			.fetchCurrent(request.getSchemaName())
			.map(llm -> {
				var responseBuilder = GetLLMConfigurationsResponse.newBuilder()
					.setApiUrl(llm.getApiUrl())
					.setJsonConfig(StructUtils.fromJson(llm.getJsonConfig()));

				if (llm.getApiKey() != null) {
					responseBuilder.setApiKey(llm.getApiKey());
				}

				return responseBuilder.build();
			});
	}

	private Uni<CreatePluginDriverResponse> upsertPluginDriver(
		String tenantId, PluginDriverDTO pluginDriverDTO) {

		return pluginDriverService.upsert(tenantId, pluginDriverDTO)
			.map(pluginDriver -> CreatePluginDriverResponse.newBuilder()
				.setPluginDriverId(pluginDriver.getId())
				.build()
			);
	}

}
