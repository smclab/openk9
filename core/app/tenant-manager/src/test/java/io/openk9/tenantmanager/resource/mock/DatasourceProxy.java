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

package io.openk9.tenantmanager.resource.mock;

import io.openk9.datasource.grpc.CreateEnrichItemRequest;
import io.openk9.datasource.grpc.CreateEnrichItemResponse;
import io.openk9.datasource.grpc.CreatePluginDriverRequest;
import io.openk9.datasource.grpc.CreatePluginDriverResponse;
import io.openk9.datasource.grpc.CreatePresetPluginDriverRequest;
import io.openk9.datasource.grpc.Datasource;
import io.openk9.datasource.grpc.InitTenantRequest;
import io.openk9.datasource.grpc.InitTenantResponse;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

@GrpcService
public final class DatasourceProxy
	extends MockedService<MockableDatasource>
	implements Datasource {

	@Override
	public Uni<InitTenantResponse> initTenant(InitTenantRequest request) {
		return getMock().initTenant(request);
	}

	@Override
	public Uni<CreateEnrichItemResponse> createEnrichItem(CreateEnrichItemRequest request) {
		return getMock().createEnrichItem(request);
	}

	@Override
	public Uni<CreatePluginDriverResponse> createPluginDriver(CreatePluginDriverRequest request) {
		return getMock().createPluginDriver(request);
	}

	@Override
	public Uni<CreatePluginDriverResponse> createPresetPluginDriver(CreatePresetPluginDriverRequest request) {
		return getMock().createPresetPluginDriver(request);
	}

}
