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

package io.openk9.datasource.web;

import io.openk9.common.util.web.InternalHeaders;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

/**
 * Makes the datasource API usable from Swagger UI when it is called directly
 * rather than through the API Gateway. The Gateway normally supplies the
 * credentials and the {@code X-TENANT-ID} header; this filter documents both
 * so Swagger UI can send them:
 *
 * <ul>
 *     <li>an HTTP Basic security scheme with a global requirement, so the
 *     Authorize dialog appears and credentials are attached to requests;</li>
 *     <li>an optional {@code X-TENANT-ID} header parameter on every operation,
 *     read by {@code RouteFilters} to select the tenant schema.</li>
 * </ul>
 *
 */
public class DatasourceOASFilter implements OASFilter {

	private static final String SCHEME_NAME = "SecurityScheme";
	private static final String TENANT_PARAM = "XTenantId";
	private static final String TENANT_PARAM_REF =
		"#/components/parameters/" + TENANT_PARAM;

	/**
	 * Registers the Basic security scheme, a global security requirement and a
	 * reusable {@code X-TENANT-ID} header parameter.
	 *
	 * @param openAPI the OpenAPI model being built
	 */
	@Override
	public void filterOpenAPI(OpenAPI openAPI) {
		Components components = openAPI.getComponents();

		if (components == null) {
			components = OASFactory.createComponents();
			openAPI.setComponents(components);
		}

		components.addSecurityScheme(
			SCHEME_NAME,
			OASFactory.createSecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("basic")
				.description(
					"Basic authentication with the admin credentials")
		);

		components.addParameter(
			TENANT_PARAM,
			OASFactory.createParameter()
				.name(InternalHeaders.TENANT_ID)
				.in(Parameter.In.HEADER)
				.required(false)
				.description(
					"Tenant id, normally set by the API Gateway. Set it "
						+ "manually when calling the service directly, e.g. "
						+ "from Swagger UI.")
				.schema(OASFactory.createSchema().addType(Schema.SchemaType.STRING))
		);

		openAPI.addSecurityRequirement(
			OASFactory.createSecurityRequirement()
				.addScheme(SCHEME_NAME)
		);
	}

	/**
	 * Adds the shared {@code X-TENANT-ID} header parameter to every operation.
	 *
	 * @param operation the operation being built
	 * @return the operation, with the tenant header parameter added
	 */
	@Override
	public Operation filterOperation(Operation operation) {
		operation.addParameter(
			OASFactory.createParameter().ref(TENANT_PARAM_REF));

		return operation;
	}

}
