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

package io.openk9.tenantmanager.service.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.openk9.app.manager.grpc.IngressScope;
import io.openk9.tenantmanager.model.SecurityConfiguration;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Request payload for creating a new tenant.
 *
 * @param virtualHost           the hostname that identifies
 *                              this tenant (e.g.
 *                              {@code demo.openk9.io}). Used as
 *                              the Ingress host and Keycloak
 *                              realm alias. Required.
 * @param securityConfiguration the gateway-level authorization
 *                              model. Determines which routes
 *                              require OAuth2, API keys, or no
 *                              auth. Required.
 * @param oAuth2Settings        external identity provider
 *                              credentials. When provided,
 *                              Keycloak realm provisioning is
 *                              skipped and these values are
 *                              stored directly. {@code null}
 *                              means auto-provision via
 *                              Keycloak (if available).
 * @param tenantName            the tenant identifier used as
 *                              the database schema name and
 *                              Kubernetes resource prefix.
 *                              Must match
 *                              {@code [a-z][a-z0-9]{0,62}}.
 *                              {@code null} to auto-generate.
 * @param ingressScopes         which route groups to expose on
 *                              the Kubernetes Ingress. Each
 *                              {@link IngressScope} maps to a
 *                              set of paths. {@code null} to
 *                              use the default set (SEARCH,
 *                              ADMINISTRATION, RAG). An empty
 *                              list means no ingress is
 *                              created.
 */
@RegisterForReflection
public record CreateTenantRequest(
	@NotEmpty String virtualHost,
	@NotNull SecurityConfiguration securityConfiguration,
	OAuth2Settings oAuth2Settings,
	String tenantName,
	List<IngressScope> ingressScopes
) {

	/**
	 * Creates a request with Keycloak-managed OAuth2 and
	 * default ingress scopes.
	 */
	public CreateTenantRequest(
		String virtualHost,
		SecurityConfiguration securityConfiguration,
		String tenantName) {

		this(virtualHost, securityConfiguration, null,
			tenantName, null);
	}
}
