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

package io.openk9.datasource.enricher;

import io.openk9.datasource.client.HttpDatasourceServiceClient;
import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.web.dto.EnricherInputDTO;
import io.openk9.datasource.web.dto.PluginDriverHealthDTO;
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


    @Override
    public Uni<FormTemplate> getForm(HttpEnricherInfo enricherInfo) {
        String uri = enricherInfo.getServiceName();
        return webClient
                .requestAbs(HttpMethod.GET, uri)
                .send()
                .map(res -> res.bodyAsJson(FormTemplate.class));
    }

    @Override
    public Uni<PluginDriverHealthDTO> getHealth(HttpEnricherInfo enricherInfo) {
        String uri = enricherInfo.getServiceName();
        return webClient
                .requestAbs(HttpMethod.GET, uri)
                .send()
                .map(res -> res.bodyAsJson(PluginDriverHealthDTO.class))
                .onFailure()
                .recoverWithItem(PluginDriverHealthDTO
                        .builder()
                        .status(PluginDriverHealthDTO.Status.UNKOWN)
                        .build()
                );
    }

    public Uni<HttpResponse<Buffer>> process(HttpEnricherInfo enricherInfo, EnricherInputDTO enricherInputDTO) {
        String uri = enricherInfo.getServiceName();
        return webClient
                .requestAbs(HttpMethod.POST, uri)
                .sendJson(enricherInputDTO)
                .flatMap(this::validateResponse);
    }
}
