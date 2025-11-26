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

package io.openk9.datasource.client;

import io.openk9.datasource.model.ResourceUri;
import io.openk9.datasource.web.dto.EnricherInputDTO;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.vertx.mutiny.core.buffer.Buffer;

@ApplicationScoped
public class HttpEnricherClient extends HttpDatasourceServiceClient {

	@Inject
	WebClient webClient;

	public static final String PROCESS_PATH = "/process";

	public Uni<HttpResponse<Buffer>> process(ResourceUri resourceUri, EnricherInputDTO enricherInputDTO) {
		String path = resourceUri.getPath();
		if (path == null || path.isEmpty()) {
			path = PROCESS_PATH;
		}

		return webClient
			.requestAbs(HttpMethod.POST, resourceUri.getBaseUri() + path)
			.sendJson(enricherInputDTO)
			.flatMap(this::validateResponse);
	}
}
