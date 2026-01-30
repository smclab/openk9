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

package io.openk9.tenantmanager.resource;

import jakarta.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@OpenAPIDefinition(
	info = @Info(
		title = "Tenant Manager Service",
		version = "3.1.1-SNAPSHOT",
		description = "Create and manage the tenants within this OpenK9 installation",
		license = @License(
			name = "GNU Affero General Public License v3.0",
			url = "https://github.com/smclab/openk9/blob/main/LICENSE"
		),
		contact = @Contact(
			name = "OpenK9 Support",
			email = "dev@openk9.io"
		)
	),
	security = @SecurityRequirement(name = "http"),
	components = @Components(
		securitySchemes = {
			@SecurityScheme(
				securitySchemeName = "http",
				type = SecuritySchemeType.HTTP,
				scheme = "Basic"
			)
		}
	)
)
public class TenantManagerApplication  extends Application {
}
