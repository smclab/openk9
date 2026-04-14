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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * External OAuth2 identity provider credentials.
 * <p>
 * When supplied in a {@link CreateTenantRequest}, these values
 * are stored directly on the tenant and Keycloak realm
 * auto-provisioning is skipped.
 *
 * @param issuerUri    the OpenID Connect issuer URI
 * @param clientId     the OAuth2 client identifier
 * @param clientSecret the OAuth2 client secret, or
 *                     {@code null} for public clients
 */
@RegisterForReflection
public record OAuth2Settings(
	@Nonnull String issuerUri,
	@Nonnull String clientId,
	@Nullable String clientSecret
) {}
