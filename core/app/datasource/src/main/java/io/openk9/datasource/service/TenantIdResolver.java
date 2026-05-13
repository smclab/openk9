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

package io.openk9.datasource.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.searcher.grpc.AutocompleteConfigurationsRequest;
import io.openk9.searcher.grpc.AutocorrectionConfigurationsRequest;
import io.openk9.searcher.grpc.GetEmbeddingModelConfigurationsRequest;
import io.openk9.searcher.grpc.GetLLMConfigurationsRequest;
import io.openk9.searcher.grpc.GetRAGConfigurationsRequest;
import io.openk9.searcher.grpc.QueryAnalysisRequest;
import io.openk9.searcher.grpc.QueryParserRequest;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

/**
 * Resolves the {@code tenantId} for an incoming gRPC request, preferring the
 * {@code tenantId} field (populated by the API Gateway from the {@code X-TENANT-ID}
 * header) and falling back to {@link TenantRegistry#getTenantId(String)} when only
 * the legacy {@code virtualHost} is present.
 * <p>
 * Failure modes:
 * <ul>
 *     <li>{@link Status#INVALID_ARGUMENT} when both inputs are blank.</li>
 *     <li>{@link Status#NOT_FOUND} when the legacy fallback runs and the
 *     {@code virtualHost} doesn't match any tenant. Without this guard the empty
 *     tenantId would propagate to {@code sf.withTransaction("", ...)} and surface
 *     as an opaque Hibernate {@code IllegalArgumentException: Invalid tenant ID:}.</li>
 * </ul>
 */
@ApplicationScoped
public class TenantIdResolver {

	private static final Logger log = Logger.getLogger(TenantIdResolver.class);

	@Inject
	TenantRegistry tenantRegistry;

	/**
	 * gRPC adapter: extracts {@code tenantId} and {@code virtualHost} from the
	 * request and delegates to {@link #resolve(String, String)}.
	 */
	public Uni<String> resolve(AutocompleteConfigurationsRequest request) {
		return resolve(request.getTenantId(), request.getVirtualHost());
	}

	/**
	 * gRPC adapter: extracts {@code tenantId} and {@code virtualHost} from the
	 * request and delegates to {@link #resolve(String, String)}.
	 */
	public Uni<String> resolve(AutocorrectionConfigurationsRequest request) {
		return resolve(request.getTenantId(), request.getVirtualHost());
	}

	/**
	 * gRPC adapter: extracts {@code tenantId} and {@code virtualHost} from the
	 * request and delegates to {@link #resolve(String, String)}.
	 */
	public Uni<String> resolve(GetEmbeddingModelConfigurationsRequest request) {
		return resolve(request.getTenantId(), request.getVirtualHost());
	}

	/**
	 * gRPC adapter: extracts {@code tenantId} and {@code virtualHost} from the
	 * request and delegates to {@link #resolve(String, String)}.
	 */
	public Uni<String> resolve(GetLLMConfigurationsRequest request) {
		return resolve(request.getTenantId(), request.getVirtualHost());
	}

	/**
	 * gRPC adapter: extracts {@code tenantId} and {@code virtualHost} from the
	 * request and delegates to {@link #resolve(String, String)}.
	 */
	public Uni<String> resolve(GetRAGConfigurationsRequest request) {
		return resolve(request.getTenantId(), request.getVirtualHost());
	}

	/**
	 * gRPC adapter: extracts {@code tenantId} and {@code virtualHost} from the
	 * request and delegates to {@link #resolve(String, String)}.
	 */
	public Uni<String> resolve(QueryParserRequest request) {
		return resolve(request.getTenantId(), request.getVirtualHost());
	}

	/**
	 * gRPC adapter: extracts {@code tenantId} and {@code virtualHost} from the
	 * request and delegates to {@link #resolve(String, String)}.
	 */
	public Uni<String> resolve(QueryAnalysisRequest request) {
		return resolve(request.getTenantId(), request.getVirtualHost());
	}

	/**
	 * Resolves the effective {@code tenantId} according to the following rules:
	 * <ol>
	 *   <li>if {@code tenantId} is provided, it is returned as-is;</li>
	 *   <li>otherwise, if {@code virtualHost} is also missing or blank, the
	 *       returned {@link Uni} fails with {@link Status#INVALID_ARGUMENT};</li>
	 *   <li>otherwise, the {@code TenantRegistry} is queried to resolve the
	 *       {@code virtualHost} (legacy path, logs a warning);</li>
	 *   <li>if the registry returns no match, the returned {@link Uni} fails
	 *       with {@link Status#NOT_FOUND}.</li>
	 * </ol>
	 *
	 * @param tenantId    the tenant id, if already known to the caller (may be {@code null} or blank)
	 * @param virtualHost the virtual host used as a fallback (may be {@code null} or blank)
	 * @return a {@link Uni} emitting the resolved {@code tenantId}, or failing with
	 *         a {@link StatusRuntimeException} in any of the error cases listed above
	 */
	protected Uni<String> resolve(String tenantId, String virtualHost) {
		if (tenantId != null && !tenantId.isBlank()) {
			return Uni.createFrom().item(tenantId);
		}
		if (virtualHost == null || virtualHost.isBlank()) {
			return Uni.createFrom().failure(
				new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription(
						"Both tenantId and virtualHost are missing"
					)
				)
			);
		}
		log.warnf(
			"Legacy gRPC request: tenantId missing, resolving via TenantRegistry [virtualHost=%s]",
			virtualHost
		);
		return tenantRegistry.getTenantId(virtualHost)
			.onItem().transformToUni(resolved -> {
				if (resolved == null || resolved.isBlank()) {
					return Uni.createFrom().failure(
						new StatusRuntimeException(
							Status.NOT_FOUND.withDescription(
								"No tenant found for virtualHost: " + virtualHost
							)
						)
					);
				}
				return Uni.createFrom().item(resolved);
			});
	}
}
