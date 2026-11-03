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

package io.openk9.tenantmanager.dto;

import io.openk9.tenantmanager.model.SecurityConfiguration;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Builder
@RegisterForReflection
public record TenantResponseDTO(
	String id,
	String tenantName,
	String virtualHost,
	String clientId,
	@Description(Desc.CLIENT_SECRET)
	@Schema(description = Desc.CLIENT_SECRET)
	String clientSecret,
	String issuerUri,
	SecurityConfiguration securityConfiguration,
	boolean realmProvisioned) {

	interface Desc {
		String CLIENT_SECRET =
			"Always null: the client secret is never returned.";
	}

}
