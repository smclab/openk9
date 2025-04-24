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

package io.openk9.searcher.resource;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import io.openk9.common.model.dto.Problem;

@OpenAPIDefinition(
        info = @Info(
                title = "Searcher Service",
                version = "3.0.0-SNAPSHOT",
                description = "API to ingest data from external sources inside Openk9.",
                license = @License(
                        name = "GNU Affero General Public License v3.0",
                        url = "https://github.com/smclab/openk9/blob/main/LICENSE"
                ),
                contact = @Contact(
                        name = "OpenK9 Support",
                        email = "dev@openk9.io"
                )

        ),
        components = @Components(
                responses = {
                        @APIResponse(
                                responseCode = "400",
                                name = "bad-request",
                                description = "Bad Request",
                                content = @Content(
                                        mediaType = "application/json+problem",
                                        schema = @Schema(
                                                ref = "#/components/schemas/Problem",
                                                externalDocs = @ExternalDocumentation(
                                                        description = "More Info here",
                                                        url = "https://opensource.zalando.com/problem"
                                                )
                                        )
                                )
                        ),
                        @APIResponse(
                                responseCode = "404",
                                name = "not-found",
                                description = "Not found",
                                content = @Content(
                                        mediaType = "application/json+problem",
                                        schema = @Schema(
                                                ref = "#/components/schemas/Problem",
                                                externalDocs = @ExternalDocumentation(
                                                        description = "More Info here",
                                                        url = "https://opensource.zalando.com/problem"
                                                )
                                        )
                                )
                        ),
                        @APIResponse(
                                responseCode = "500",
                                name = "internal-server-error",
                                description = "Internal Server Error",
                                content = @Content(
                                        mediaType = "application/json+problem",
                                        schema = @Schema(
                                                ref = "#/components/schemas/Problem",
                                                externalDocs = @ExternalDocumentation(
                                                        description = "More Info here",
                                                        url = "https://opensource.zalando.com/problem"
                                                )
                                        )
                                )
                        ),
                },
                schemas = {
                        @Schema(
                                name = "Problem",
                                description = "Problem details for HTTP APIs (rfc7807)",
                                implementation = Problem.class
                        )
                }
        )
)

public class SearcherApplication {
}
