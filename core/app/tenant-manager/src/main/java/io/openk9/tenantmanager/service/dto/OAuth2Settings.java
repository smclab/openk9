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
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "External OAuth2 identity provider "
	+ "credentials. When provided, Keycloak realm "
	+ "auto-provisioning is skipped.")
@RegisterForReflection
public record OAuth2Settings(

	@Nonnull
	@Schema(description = "OpenID Connect issuer URI.",
		required = true,
		example = "https://idp.example.com/realms/myrealm")
	String issuerUri,

	@Nonnull
	@Schema(description = "OAuth2 client identifier.",
		required = true)
	String clientId,

	@Nullable
	@Schema(description = "OAuth2 client secret. "
		+ "Omit for public clients.")
	String clientSecret

) {}
