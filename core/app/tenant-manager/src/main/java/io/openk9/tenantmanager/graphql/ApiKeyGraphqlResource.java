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

package io.openk9.tenantmanager.graphql;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.tenantmanager.service.ApiKeyService;
import io.openk9.tenantmanager.service.TenantDbService;
import io.openk9.tenantmanager.service.dto.ApiKeyResponse;
import io.openk9.tenantmanager.service.dto.CreateApiKeyRequest;
import io.openk9.tenantmanager.service.dto.CreateApiKeyResponse;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class ApiKeyGraphqlResource {

	@Inject
	ApiKeyService apiKeyService;

	@Inject
	TenantDbService tenantDbService;

	@Query
	public Uni<List<ApiKeyResponse>> getApiKeys(@NonNull String tenantId) {
		return apiKeyService.findAllByTenantId(Long.parseLong(tenantId));
	}

	@Query
	public Uni<ApiKeyResponse> getApiKey(@NonNull @Id String id) {
		return apiKeyService.findById(Long.parseLong(id));
	}

	@Mutation
	public Uni<CreateApiKeyResponse> createApiKey(@NonNull CreateApiKeyRequest createApiKeyRequest) {
		return apiKeyService.create(createApiKeyRequest);
	}

	@Mutation
	public Uni<Void> revokeApiKey(@NonNull @Id String id) {
		return apiKeyService.revoke(id);
	}

	@Mutation
	public Uni<Boolean> deleteApiKey(@NonNull @Id String id) {
		return apiKeyService.delete(id);
	}
}
